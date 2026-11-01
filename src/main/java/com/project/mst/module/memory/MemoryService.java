package com.project.mst.module.memory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.module.memory.dto.CardView;
import com.project.mst.module.memory.dto.MemoryMoveRequest;
import com.project.mst.module.memory.dto.MemoryMoveResponse;
import com.project.mst.module.memory.dto.MemoryRoundResponse;
import com.project.mst.module.memory.dto.StartRoundRequest;
import com.project.mst.module.memory.dto.ThemeResponse;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.scores.dto.ScoreResponse;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Service
public class MemoryService {

    public static final String GAME_SLUG = "memoria";
    public static final int PAIRS = 9;

    private static final ObjectMapper JSON = JsonMapper.builder().build();
    private static final TypeReference<List<UUID>> LAYOUT = new TypeReference<>() { };

    private final CardThemeRepository themes;
    private final CardTemplateRepository cards;
    private final MemoryRoundRepository rounds;
    private final MemoryMoveRepository moves;
    private final PlaySessionService sessions;
    private final ScoreService scores;
    private final Random random;

    @Autowired
    public MemoryService(CardThemeRepository themes, CardTemplateRepository cards, MemoryRoundRepository rounds,
            MemoryMoveRepository moves, PlaySessionService sessions, ScoreService scores) {
        this(themes, cards, rounds, moves, sessions, scores, new SecureRandom());
    }

    MemoryService(CardThemeRepository themes, CardTemplateRepository cards, MemoryRoundRepository rounds,
            MemoryMoveRepository moves, PlaySessionService sessions, ScoreService scores, Random random) {
        this.themes = themes;
        this.cards = cards;
        this.rounds = rounds;
        this.moves = moves;
        this.sessions = sessions;
        this.scores = scores;
        this.random = random;
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> themes() {
        return themes.findAllActiveAndEnabled().stream().map(ThemeResponse::from).toList();
    }

    @Transactional
    public MemoryRoundResponse start(StartRoundRequest request) {
        CardThemeModel theme = themes.findEnabledById(request.themeId())
            .orElseThrow(() -> new EntityNotFoundException("Tema " + request.themeId() + " não encontrado"));
        List<CardTemplateModel> available = new ArrayList<>(cards.findEnabledByThemeId(theme.getId()));
        if (available.size() < 2) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "MEMORY_THEME_WITHOUT_CARDS", "Tema sem cartas suficientes");
        }
        Collections.shuffle(available, random);
        List<CardTemplateModel> chosen = available.subList(0, Math.min(PAIRS, available.size()));

        List<UUID> layout = new ArrayList<>(chosen.size() * 2);
        for (CardTemplateModel card : chosen) {
            layout.add(card.getId());
            layout.add(card.getId());
        }
        Collections.shuffle(layout, random);

        PlaySessionModel session = sessions.open(GAME_SLUG, null, request.timed());
        MemoryRoundModel round = new MemoryRoundModel();
        round.setPlaySession(session);
        round.setTheme(theme);
        round.setPairs(chosen.size());
        round.setErrors(0);
        round.setLayout(JSON.writeValueAsString(layout));
        round.setId_user_created(session.getUser().getId());
        round = rounds.save(round);

        return roundResponse(round, List.of(), null);
    }

    @Transactional(readOnly = true)
    public MemoryRoundResponse find(UUID roundId) {
        MemoryRoundModel round = ownedRound(roundId);
        List<MemoryMoveModel> played = moves.findByRoundIdOrdered(round.getId());
        ScoreResponse score = round.getPlaySession().isOpen() ? null
            : scores.findByPlaySession(round.getPlaySession().getId()).orElse(null);
        return roundResponse(round, played, score);
    }

    @Transactional
    public MemoryMoveResponse move(UUID roundId, MemoryMoveRequest request) {
        MemoryRoundModel round = ownedRound(roundId);
        if (!round.getPlaySession().isOpen()) {
            throw new ApiException(HttpStatus.CONFLICT, "MEMORY_ROUND_FINISHED", "Partida já encerrada");
        }
        List<UUID> layout = layout(round);
        int position = request.position();
        if (position >= layout.size()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "MEMORY_POSITION_INVALID", "Posição fora do tabuleiro");
        }

        List<MemoryMoveModel> played = moves.findByRoundIdOrdered(round.getId());
        Set<Integer> matched = matchedPositions(played);
        if (matched.contains(position)) {
            throw new ApiException(HttpStatus.CONFLICT, "MEMORY_POSITION_ALREADY_MATCHED", "Carta já fixada");
        }
        MemoryMoveModel pending = played.size() % 2 == 1 ? played.get(played.size() - 1) : null;
        if (pending != null && pending.getPosition() == position) {
            throw new ApiException(HttpStatus.CONFLICT, "MEMORY_POSITION_ALREADY_REVEALED", "Carta já virada nesta tentativa");
        }

        Map<UUID, CardTemplateModel> byId = cardsOf(layout);
        CardTemplateModel card = byId.get(layout.get(position));
        MemoryMoveModel move = new MemoryMoveModel();
        move.setRound(round);
        move.setMoveNumber(played.size() + 1);
        move.setCardTemplate(card);
        move.setPosition(position);
        move.setMatched(false);
        move.setId_user_created(round.getPlaySession().getUser().getId());

        boolean pairCompleted = false;
        CardView previous = null;
        if (pending != null) {
            previous = CardView.revealed(pending.getPosition(), pending.getCardTemplate());
            pairCompleted = pending.getCardTemplate().getId().equals(card.getId());
            if (pairCompleted) {
                pending.setMatched(true);
                move.setMatched(true);
                moves.save(pending);
                matched.add(pending.getPosition());
                matched.add(position);
            } else {
                round.setErrors(round.getErrors() + 1);
                rounds.save(round);
            }
        }
        moves.save(move);

        int matchedPairs = matched.size() / 2;
        boolean finished = matchedPairs >= round.getPairs();
        ScoreResponse score = null;
        if (finished) {
            score = sessions.finish(round.getPlaySession().getId()).score();
        }

        List<Integer> revealed = new ArrayList<>(matched);
        if (pending == null) {
            revealed.add(position);
        }
        Collections.sort(revealed);

        return new MemoryMoveResponse(CardView.revealed(position, card), previous, pairCompleted,
            round.getErrors(), matchedPairs, round.getPairs(), finished, revealed, score);
    }

    private MemoryRoundResponse roundResponse(MemoryRoundModel round, List<MemoryMoveModel> played, ScoreResponse score) {
        List<UUID> layout = layout(round);
        Set<Integer> matched = matchedPositions(played);
        Map<UUID, CardTemplateModel> byId = matched.isEmpty() ? Map.of() : cardsOf(layout);
        List<CardView> views = IntStream.range(0, layout.size())
            .mapToObj(i -> matched.contains(i) ? CardView.revealed(i, byId.get(layout.get(i))) : CardView.hidden(i))
            .toList();
        List<Integer> revealed = new ArrayList<>(matched);
        Collections.sort(revealed);
        return new MemoryRoundResponse(round.getId(), round.getPlaySession().getId(), round.getTheme().getId(),
            round.getTheme().getBackImagePath(), round.getPairs(), matched.size() / 2, round.getErrors(),
            !round.getPlaySession().isOpen(), views, revealed, score);
    }

    private Map<UUID, CardTemplateModel> cardsOf(List<UUID> layout) {
        return cards.findAllByIds(new LinkedHashSet<>(layout)).stream()
            .collect(Collectors.toMap(CardTemplateModel::getId, Function.identity()));
    }

    private static Set<Integer> matchedPositions(List<MemoryMoveModel> played) {
        return played.stream().filter(MemoryMoveModel::isMatched).map(MemoryMoveModel::getPosition)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    static List<UUID> layout(MemoryRoundModel round) {
        return JSON.readValue(round.getLayout(), LAYOUT);
    }

    private MemoryRoundModel ownedRound(UUID id) {
        MemoryRoundModel round = rounds.findActiveById(id)
            .orElseThrow(() -> new EntityNotFoundException("Partida " + id + " não encontrada"));
        sessions.requireOwner(round.getPlaySession());
        return round;
    }
}
