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
        assertThat(games.findAllActive()).hasSize(6);
        assertThat(games.findAllActiveAndEnabled()).hasSize(5);
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
        String token = JsonMapper.builder().build().readTree(body).get("accessToken").asString();

        mvc.perform(get("/api/v1/games").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(5))
            .andExpect(jsonPath("$[?(@.slug=='velha')].title").value("Jogo da Velha"));

        mvc.perform(get("/api/v1/games/2048").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));

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

    private static EmbeddedPostgres startPostgres() {
        try {
            return EmbeddedPostgres.builder().start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
