package com.project.mst.module.scores;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.project.mst.infra.security.CurrentUser;
import com.project.mst.infra.service.CrudService;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.scores.dto.ScoreResponse;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ScoreService extends CrudService<ScoreModel> {

    private final ScoreRepository scores;
    private final Map<String, ScoreCalculator> calculators;

    public ScoreService(ScoreRepository scores, List<ScoreCalculator> calculators) {
        super(scores);
        this.scores = scores;
        this.calculators = calculators.stream()
            .collect(Collectors.toUnmodifiableMap(ScoreCalculator::gameSlug, Function.identity()));
    }

    /** Lista as próprias pontuações; ADMIN vê todas. */
    @Transactional(readOnly = true)
    public List<ScoreResponse> findVisible() {
        List<ScoreModel> result = CurrentUser.isAdmin()
            ? scores.findAllActive()
            : scores.findAllActiveByUserId(currentUserId());
        return result.stream().map(ScoreResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ScoreResponse findVisibleById(UUID id) {
        ScoreModel score = findById(id);
        if (!CurrentUser.owns(score.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pontuação de outro usuário");
        }
        return ScoreResponse.from(score);
    }

    @Transactional(readOnly = true)
    public Optional<ScoreResponse> findByPlaySession(UUID playSessionId) {
        return scores.findActiveByPlaySessionId(playSessionId).map(ScoreResponse::from);
    }

    @Transactional
    public Optional<ScoreModel> scoreFor(PlaySessionModel session) {
        if (session.isOpen()) {
            throw new IllegalStateException("Sessão ainda em andamento não pontua");
        }
        if (scores.findActiveByPlaySessionId(session.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sessão já pontuada");
        }

        ScoreCalculator calculator = calculators.get(session.getGame().getSlug());
        if (calculator == null) {
            return Optional.empty();
        }

        ScoreModel score = new ScoreModel();
        score.setUser(session.getUser());
        score.setGame(session.getGame());
        score.setPlay_session(session);
        score.setScore_value(calculator.calculate(session));
        return Optional.of(create(score));
    }

    private static UUID currentUserId() {
        return CurrentUser.id()
            .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não identificado"));
    }
}
