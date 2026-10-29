package com.project.mst.module.scores;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.project.mst.infra.service.CrudService;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.scores.dto.ScoreResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.mst.infra.controller.ApiException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ScoreService extends CrudService<ScoreModel> {

    private static final ObjectMapper JSON = JsonMapper.builder().build();

    private final ScoreRepository scores;
    private final Map<String, ScoreCalculator> calculators;

    public ScoreService(ScoreRepository scores, List<ScoreCalculator> calculators) {
        super(scores);
        this.scores = scores;
        this.calculators = calculators.stream()
            .collect(Collectors.toUnmodifiableMap(ScoreCalculator::gameSlug, Function.identity()));
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
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_ALREADY_SCORED", "Sessão já pontuada");
        }

        ScoreCalculator calculator = calculators.get(session.getGame().getSlug());
        if (calculator == null) {
            return Optional.empty();
        }

        ScoreResult result = calculator.calculate(session);
        ScoreModel score = new ScoreModel();
        score.setPlaySession(session);
        score.setValue(result.value());
        score.setBreakdown(result.breakdown() == null || result.breakdown().isEmpty()
            ? null : JSON.writeValueAsString(result.breakdown()));
        return Optional.of(create(score));
    }
}
