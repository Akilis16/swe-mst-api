package com.project.mst.module.playsessions;

import java.util.Date;
import java.util.UUID;

import com.project.mst.infra.security.CurrentUser;
import com.project.mst.infra.service.CrudService;
import com.project.mst.module.games.GameModel;
import com.project.mst.module.games.GameService;
import com.project.mst.module.playsessions.dto.PlaySessionResponse;
import com.project.mst.module.playsessions.dto.StartSessionRequest;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.scores.dto.ScoreResponse;
import com.project.mst.module.users.UserModel;
import com.project.mst.module.users.UserRepository;
import com.project.mst.module.users.dto.Preferences;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.mst.infra.controller.ApiException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Service
public class PlaySessionService extends CrudService<PlaySessionModel> {

    private static final ObjectMapper JSON = JsonMapper.builder().build();

    private final PlaySessionRepository sessions;
    private final UserRepository users;
    private final GameService games;
    private final ScoreService scores;

    public PlaySessionService(PlaySessionRepository sessions, UserRepository users,
            GameService games, ScoreService scores) {
        super(sessions);
        this.sessions = sessions;
        this.users = users;
        this.games = games;
        this.scores = scores;
    }

    @Transactional
    public PlaySessionResponse start(StartSessionRequest request) {
        PlaySessionModel session = open(request.gameSlug(), request.difficulty(), request.timed(),
            validJsonOrNull(request.clientMeta(), "clientMeta"));
        return PlaySessionResponse.from(session, null);
    }

    @Transactional
    public PlaySessionModel open(String gameSlug, Difficulty difficulty, Boolean timed) {
        return open(gameSlug, difficulty, timed, null);
    }

    private PlaySessionModel open(String gameSlug, Difficulty difficulty, Boolean timed, String clientMeta) {
        UserModel user = currentUser();
        GameModel game = games.requireEnabledBySlug(gameSlug);

        PlaySessionModel session = new PlaySessionModel();
        session.setUser(user);
        session.setGame(game);
        session.setStatus(PlaySessionStatus.IN_PROGRESS);
        session.setDifficulty(difficulty);
        session.setTimed(timed != null ? timed : Preferences.fromJson(user.getPreferences()).timed());
        session.setStartedAt(new Date());
        session.setClientMeta(clientMeta);

        return create(session);
    }

    public void requireOwner(PlaySessionModel session) {
        if (!CurrentUser.owns(session.getUser().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "SESSION_FORBIDDEN", "Sessão de outro usuário");
        }
    }

    public static long elapsedSeconds(PlaySessionModel session) {
        Date end = session.getEndedAt() != null ? session.getEndedAt() : new Date();
        return Math.max(0, (end.getTime() - session.getStartedAt().getTime()) / 1000);
    }

    @Transactional
    public PlaySessionResponse finish(UUID id) {
        PlaySessionModel session = ownedSession(id);

        if (session.getStatus() == PlaySessionStatus.FINISHED) {
            return toResponse(session);
        }
        if (!session.isOpen()) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_ABANDONED", "Sessão abandonada não pode ser encerrada");
        }

        session.setStatus(PlaySessionStatus.FINISHED);
        session.setEndedAt(now(session));
        session = sessions.save(session);

        ScoreResponse score = scores.scoreFor(session).map(ScoreResponse::from).orElse(null);
        return PlaySessionResponse.from(session, score);
    }

    @Transactional
    public PlaySessionResponse abandon(UUID id) {
        PlaySessionModel session = ownedSession(id);
        if (!session.isOpen()) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_ALREADY_ENDED", "Sessão já encerrada");
        }

        session.setStatus(PlaySessionStatus.ABANDONED);
        session.setEndedAt(now(session));

        return PlaySessionResponse.from(sessions.save(session), null);
    }

    @Transactional(readOnly = true)
    public Page<PlaySessionResponse> pageForCurrentUser(String gameSlug, Pageable pageable) {
        UUID userId = currentUserId();
        String slug = GameModel.normalizeSlug(gameSlug);
        Page<PlaySessionModel> page = slug == null || slug.isBlank()
            ? sessions.findPageByUserId(userId, pageable)
            : sessions.findPageByUserIdAndGameSlug(userId, slug, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlaySessionResponse> pageAll(String gameSlug, Pageable pageable) {
        String slug = GameModel.normalizeSlug(gameSlug);
        Page<PlaySessionModel> page = slug == null || slug.isBlank()
            ? sessions.findPageAll(pageable)
            : sessions.findPageByGameSlug(slug, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PlaySessionResponse findVisibleById(UUID id) {
        return toResponse(ownedSession(id));
    }

    private PlaySessionResponse toResponse(PlaySessionModel session) {
        return PlaySessionResponse.from(session, scores.findByPlaySession(session.getId()).orElse(null));
    }

    private PlaySessionModel ownedSession(UUID id) {
        PlaySessionModel session = findById(id);
        requireOwner(session);
        return session;
    }

    private static Date now(PlaySessionModel session) {
        Date now = new Date();
        Date started = session.getStartedAt();
        return (started != null && now.before(started)) ? started : now;
    }

    private static String validJsonOrNull(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            JSON.readTree(value);
            return value;
        } catch (JacksonException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_JSON", field + " precisa ser um JSON válido");
        }
    }

    private UserModel currentUser() {
        return users.findActiveById(currentUserId())
            .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado"));
    }

    private static UUID currentUserId() {
        return CurrentUser.id()
            .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Usuário não autenticado"));
    }
}
