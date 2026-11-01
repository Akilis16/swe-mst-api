package com.project.mst.module.quizzes;

import java.util.UUID;

import com.project.mst.module.quizzes.dto.AnswerRequest;
import com.project.mst.module.quizzes.dto.AnswerResponse;
import com.project.mst.module.quizzes.dto.NextQuestionResponse;
import com.project.mst.module.quizzes.dto.QuizAttemptResponse;
import com.project.mst.module.quizzes.dto.QuizResultResponse;
import com.project.mst.module.quizzes.dto.StartAttemptRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quiz/attempts")
@Tag(name = "Quiz", description = "Tentativas, perguntas sem gabarito e correção no servidor")
public class QuizPlayController {

    private final QuizPlayService service;

    public QuizPlayController(QuizPlayService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inicia uma tentativa", description = "quizId opcional; sem ele usa o primeiro quiz ativo.")
    public QuizAttemptResponse start(@RequestBody(required = false) StartAttemptRequest request) {
        return service.start(request != null ? request : new StartAttemptRequest(null, null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Estado da tentativa")
    public QuizAttemptResponse find(@PathVariable UUID id) {
        return service.find(id);
    }

    @GetMapping("/{id}/questions/next")
    @Operation(summary = "Próxima pergunta sem resposta", description = "Alternativas sem isCorrect. 404 QUIZ_NO_MORE_QUESTIONS quando todas foram respondidas.")
    public NextQuestionResponse next(@PathVariable UUID id) {
        return service.next(id);
    }

    @PostMapping("/{id}/answers")
    @Operation(summary = "Responde uma pergunta", description = "Devolve se acertou e qual era a alternativa correta. 409 QUIZ_QUESTION_ALREADY_ANSWERED; 422 QUIZ_OPTION_INVALID.")
    public AnswerResponse answer(@PathVariable UUID id, @Valid @RequestBody AnswerRequest request) {
        return service.answer(id, request);
    }

    @PostMapping("/{id}/finish")
    @Operation(summary = "Encerra a tentativa", description = "Desempenho = acertos × 100 / total; mensagem por faixa (90, 70, 50). 409 QUIZ_QUESTIONS_PENDING se faltar resposta.")
    public QuizResultResponse finish(@PathVariable UUID id) {
        return service.finish(id);
    }
}
