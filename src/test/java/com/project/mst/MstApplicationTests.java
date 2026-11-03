package com.project.mst;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UncheckedIOException;

import com.project.mst.infra.security.Roles;
import com.project.mst.module.games.GameRepository;
import com.project.mst.module.hangman.WordRepository;
import com.project.mst.module.memory.CardTemplateRepository;
import com.project.mst.module.memory.CardThemeRepository;
import com.project.mst.module.profiles.ProfileRepository;
import com.project.mst.module.quizzes.OptionRepository;
import com.project.mst.module.quizzes.QuestionRepository;
import com.project.mst.module.sudoku.SudokuPuzzleRepository;
import com.project.mst.module.users.UserRepository;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "security.jwt.secret=chave-de-teste-com-pelo-menos-32-bytes-ok",
    "security.bootstrap.admin-email=admin@mst.test",
    "security.bootstrap.admin-password=admin12345"
})
class MstApplicationTests {

    static final EmbeddedPostgres POSTGRES = startPostgres();
    static final JsonMapper JSON = JsonMapper.builder().build();

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired MockMvc mvc;
    @Autowired GameRepository games;
    @Autowired QuestionRepository questions;
    @Autowired OptionRepository options;
    @Autowired WordRepository words;
    @Autowired SudokuPuzzleRepository puzzles;
    @Autowired CardThemeRepository themes;
    @Autowired CardTemplateRepository cards;
    @Autowired ProfileRepository profiles;
    @Autowired UserRepository users;

    @Test
    void migracoesAplicadasESchemaValidado() {
        assertThat(games.findAllActive()).hasSize(5);
        assertThat(games.findAllActiveAndEnabled()).hasSize(5);
        assertThat(games.findBySlug("2048")).isEmpty();
        assertThat(questions.findAllActive()).hasSize(28);
        assertThat(options.findAllActive()).hasSize(112);
        assertThat(words.findAllActive()).hasSize(40);
        assertThat(puzzles.findAllActive()).hasSize(15);
        assertThat(themes.findAllActive()).hasSize(3);
        assertThat(cards.findAllActive()).hasSize(27);
    }

    @Test
    void perfisEPrimeiroAdminCriadosNaInicializacao() {
        assertThat(profiles.findByCode(Roles.ADMIN)).isPresent();
        assertThat(profiles.findByCode(Roles.PLAYER)).isPresent();
        assertThat(users.findByEmail("admin@mst.test")).isPresent();
    }

    @Test
    void loginRealEAcessoAoCatalogoComPrefixoV1() throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@mst.test\",\"password\":\"admin12345\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String token = JSON.readTree(body).get("accessToken").asString();

        mvc.perform(get("/api/v1/games").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(5))
            .andExpect(jsonPath("$[?(@.slug=='velha')].title").value("Jogo da Velha"));

        mvc.perform(get("/api/v1/games/2048").header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        mvc.perform(get("/api/v1/admin/words").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(40));
    }

    @Test
    void openApiPublicadoSemAutenticacao() throws Exception {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/api/v1/auth/login']").exists())
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    @Test
    void semTokenRespondeProblemDetailsComCodigo() throws Exception {
        mvc.perform(get("/api/v1/games"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void jogoDaVelhaContraOComputadorAteOFimEPontua() throws Exception {
        String token = registerPlayer("velha");

        String match = mvc.perform(post("/api/v1/tic-tac-toe/matches").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"mode\":\"VS_COMPUTER\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nextMark").value("X"))
            .andExpect(jsonPath("$.board.length()").value(3))
            .andReturn().getResponse().getContentAsString();
        String matchId = JSON.readTree(match).get("matchId").asString();

        boolean finished = false;
        for (int cell = 0; cell < 9 && !finished; cell++) {
            var result = mvc.perform(post("/api/v1/tic-tac-toe/matches/" + matchId + "/moves")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"row\":" + cell / 3 + ",\"col\":" + cell % 3 + "}"))
                .andReturn().getResponse();
            if (result.getStatus() == 409) {
                assertThat(JSON.readTree(result.getContentAsString()).get("code").asString()).isEqualTo("TICTACTOE_CELL_OCCUPIED");
                continue;
            }
            assertThat(result.getStatus()).isEqualTo(200);
            finished = JSON.readTree(result.getContentAsString()).get("finished").asBoolean();
        }
        assertThat(finished).isTrue();

        mvc.perform(post("/api/v1/tic-tac-toe/matches/" + matchId + "/moves").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"row\":0,\"col\":0}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("TICTACTOE_MATCH_FINISHED"));

        mvc.perform(get("/api/v1/tic-tac-toe/matches/" + matchId).header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").isNotEmpty())
            .andExpect(jsonPath("$.score.value").isNumber())
            .andExpect(jsonPath("$.score.breakdown.mode").value("VS_COMPUTER"));

        mvc.perform(get("/api/v1/me/progress?game=velha").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.series.length()").value(1))
            .andExpect(jsonPath("$.series[0].gameSlug").value("velha"))
            .andExpect(jsonPath("$.series[0].finished").value(1));
    }

    @Test
    void quizNaoExpoeGabaritoECorrigeNoServidor() throws Exception {
        String token = registerPlayer("quiz");

        String attempt = mvc.perform(post("/api/v1/quiz/attempts").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.totalQuestions").value(28))
            .andReturn().getResponse().getContentAsString();
        String attemptId = JSON.readTree(attempt).get("attemptId").asString();

        String next = mvc.perform(get("/api/v1/quiz/attempts/" + attemptId + "/questions/next").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.index").value(1))
            .andExpect(jsonPath("$.text").value("Empatia é..."))
            .andExpect(jsonPath("$.options.length()").value(4))
            .andReturn().getResponse().getContentAsString();
        assertThat(next).doesNotContain("isCorrect").doesNotContain("correct");
        var question = JSON.readTree(next);

        mvc.perform(post("/api/v1/quiz/attempts/" + attemptId + "/answers").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"questionId\":\"" + question.get("questionId").asString() + "\",\"optionId\":\"" + question.get("options").get(0).get("id").asString() + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correctOptionId").isNotEmpty())
            .andExpect(jsonPath("$.answered").value(1))
            .andExpect(jsonPath("$.total").value(28));

        mvc.perform(post("/api/v1/quiz/attempts/" + attemptId + "/answers").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"questionId\":\"" + question.get("questionId").asString() + "\",\"optionId\":\"" + question.get("options").get(1).get("id").asString() + "\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("QUIZ_QUESTION_ALREADY_ANSWERED"));

        mvc.perform(post("/api/v1/quiz/attempts/" + attemptId + "/finish").header("Authorization", "Bearer " + token))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("QUIZ_QUESTIONS_PENDING"));

        mvc.perform(get("/api/v1/quiz/attempts/" + attemptId + "/questions/next").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.index").value(2));
    }

    @Test
    void sudokuNaoExpoeSolucaoERevelarAbandonaSemPontuar() throws Exception {
        String token = registerPlayer("sudoku");

        String attempt = mvc.perform(post("/api/v1/sudoku/attempts").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"difficulty\":\"EASY\",\"timeLimitSeconds\":300}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.mistakesAllowed").value(3))
            .andExpect(jsonPath("$.timeLimitSeconds").value(300))
            .andReturn().getResponse().getContentAsString();
        assertThat(attempt).doesNotContain("solution");
        var body = JSON.readTree(attempt);
        String attemptId = body.get("attemptId").asString();
        String givens = body.get("givens").asString();
        assertThat(givens).hasSize(81);
        int given = givens.indexOf(givens.chars().filter(c -> c != '0').findFirst().orElseThrow());

        mvc.perform(post("/api/v1/sudoku/attempts/" + attemptId + "/entries").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"cellIdx\":" + given + ",\"value\":5}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("SUDOKU_CELL_IS_GIVEN"));

        mvc.perform(post("/api/v1/sudoku/attempts/" + attemptId + "/reveal").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.solution").value(org.hamcrest.Matchers.hasLength(81)));

        mvc.perform(post("/api/v1/sudoku/attempts/" + attemptId + "/entries").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"cellIdx\":0,\"value\":1}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("SUDOKU_ATTEMPT_FINISHED"));

        mvc.perform(get("/api/v1/sudoku/attempts/" + attemptId).header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.finished").value(true))
            .andExpect(jsonPath("$.result").value("REVEALED"))
            .andExpect(jsonPath("$.score").isEmpty());
    }

    @Test
    void forcaSoRevelaAPalavraAoFim() throws Exception {
        String token = registerPlayer("forca");

        mvc.perform(get("/api/v1/hangman/categories").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.code=='FRUTAS')].wordCount").value(10));

        String round = mvc.perform(post("/api/v1/hangman/rounds").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"category\":\"frutas\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.maxErrors").value(6))
            .andExpect(jsonPath("$.word").isEmpty())
            .andReturn().getResponse().getContentAsString();
        var body = JSON.readTree(round);
        String roundId = body.get("roundId").asString();
        assertThat(body.get("maskedWord").asString()).matches("(_ )*_");

        mvc.perform(post("/api/v1/hangman/rounds/" + roundId + "/guesses").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"letter\":\"a\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.usedLetters[0]").value("A"));

        mvc.perform(post("/api/v1/hangman/rounds/" + roundId + "/guesses").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"letter\":\"A\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("HANGMAN_LETTER_ALREADY_USED"));

        mvc.perform(post("/api/v1/hangman/rounds/" + roundId + "/guesses").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"letter\":\"ç\"}"))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.code").value("HANGMAN_LETTER_INVALID"));
    }

    @Test
    void memoriaNaoIdentificaAsCartasAntesDeVirar() throws Exception {
        String token = registerPlayer("memoria");

        String themes = mvc.perform(get("/api/v1/memory/themes").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andReturn().getResponse().getContentAsString();
        String themeId = JSON.readTree(themes).get(0).get("id").asString();

        String round = mvc.perform(post("/api/v1/memory/rounds").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"themeId\":\"" + themeId + "\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.pairs").value(9))
            .andExpect(jsonPath("$.cards.length()").value(18))
            .andExpect(jsonPath("$.cards[*].label").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.nullValue())))
            .andReturn().getResponse().getContentAsString();
        String roundId = JSON.readTree(round).get("roundId").asString();

        mvc.perform(post("/api/v1/memory/rounds/" + roundId + "/moves").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"position\":0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.card.label").isNotEmpty())
            .andExpect(jsonPath("$.pairCompleted").value(false))
            .andExpect(jsonPath("$.revealedPositions[0]").value(0));

        mvc.perform(post("/api/v1/memory/rounds/" + roundId + "/moves").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"position\":0}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("MEMORY_POSITION_ALREADY_REVEALED"));

        mvc.perform(post("/api/v1/memory/rounds/" + roundId + "/moves").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"position\":18}"))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.code").value("MEMORY_POSITION_INVALID"));
    }

    private String registerPlayer(String suffix) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Jogador\",\"email\":\"jogador-" + suffix + "@mst.test\",\"password\":\"senha12345\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JSON.readTree(body).get("accessToken").asString();
    }

    private static EmbeddedPostgres startPostgres() {
        try {
            return EmbeddedPostgres.builder().start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
