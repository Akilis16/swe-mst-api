package com.project.mst.infra.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.web.ApiPathPrefixConfig;
import com.project.mst.module.games.GameController;
import com.project.mst.module.hangman.WordController;
import com.project.mst.module.hangman.WordService;
import com.project.mst.module.games.GameModel;
import com.project.mst.module.games.GameService;
import com.project.mst.module.profiles.ProfileController;
import com.project.mst.module.profiles.ProfileService;
import com.project.mst.module.users.UserController;
import com.project.mst.module.users.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest({GameController.class, UserController.class, ProfileController.class, WordController.class})
@Import({SecurityConfig.class, ProblemDetailsEntryPoint.class, ApiPathPrefixConfig.class})
@TestPropertySource(properties = {
    "security.jwt.secret=chave-de-teste-com-pelo-menos-32-bytes-ok",
    "security.jwt.expiration-seconds=3600"
})
class SecurityRulesTest {

    private static final String GAME_JSON = "{\"slug\":\"velha\",\"title\":\"Jogo da Velha\"}";

    @Autowired MockMvc mvc;
    @MockitoBean GameService gameService;
    @MockitoBean UserService userService;
    @MockitoBean ProfileService profileService;
    @MockitoBean WordService wordService;

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/api/v1/games"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void semOrigensConfiguradasNaoLiberaCors() throws Exception {
        mvc.perform(options("/api/v1/games")
                .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void playerLeCatalogo() throws Exception {
        when(gameService.catalog(anyBoolean())).thenReturn(List.of());

        mvc.perform(get("/api/v1/games").with(player())).andExpect(status().isOk());
    }

    @Test
    void playerNaoEscreveNemAtivaJogo() throws Exception {
        mvc.perform(post("/api/v1/games").with(player()).contentType(MediaType.APPLICATION_JSON).content(GAME_JSON))
            .andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/games/" + UUID.randomUUID() + "/active").with(player())
                .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
            .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/games/" + UUID.randomUUID()).with(player()))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCriaJogo() throws Exception {
        when(gameService.create(any(GameModel.class))).thenAnswer(invocation -> {
            GameModel game = invocation.getArgument(0);
            game.setId(UUID.randomUUID());
            return game;
        });

        mvc.perform(post("/api/v1/games").with(admin()).contentType(MediaType.APPLICATION_JSON).content(GAME_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void playerNaoAcessaUsuariosNemPapeis() throws Exception {
        mvc.perform(get("/api/v1/users").with(player()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mvc.perform(get("/api/v1/profiles").with(player())).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/words").with(player())).andExpect(status().isForbidden());
    }

    @Test
    void adminAcessaUsuariosEPapeis() throws Exception {
        when(userService.findAll()).thenReturn(List.of());
        when(profileService.findAll()).thenReturn(List.of());

        when(wordService.findAll()).thenReturn(List.of());

        mvc.perform(get("/api/v1/users").with(admin())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/profiles").with(admin())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/words").with(admin())).andExpect(status().isOk());
    }

    private static RequestPostProcessor player() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + Roles.PLAYER));
    }

    private static RequestPostProcessor admin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + Roles.ADMIN));
    }
}
