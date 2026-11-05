package com.project.mst.infra.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.project.mst.infra.web.ApiPathPrefixConfig;
import com.project.mst.module.games.GameController;
import com.project.mst.module.games.GameService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameController.class)
@Import({SecurityConfig.class, ProblemDetailsEntryPoint.class, ApiPathPrefixConfig.class})
@TestPropertySource(properties = {
    "security.jwt.secret=chave-de-teste-com-pelo-menos-32-bytes-ok",
    "security.jwt.expiration-seconds=3600",
    "security.cors.allowed-origins=http://localhost:4200,http://127.0.0.1:4200"
})
class CorsRulesTest {

    private static final String WEB = "http://localhost:4200";
    private static final String OUTRA = "http://outro-site.example";

    @Autowired MockMvc mvc;
    @MockitoBean GameService gameService;

    @Test
    void preflightDeOrigemListadaEhLiberado() throws Exception {
        mvc.perform(options("/api/v1/games")
                .header(HttpHeaders.ORIGIN, WEB)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, WEB))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,PATCH,DELETE,OPTIONS"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization"));
    }

    @Test
    void preflightDeOrigemForaDaListaEhRecusado() throws Exception {
        mvc.perform(options("/api/v1/games")
                .header(HttpHeaders.ORIGIN, OUTRA)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
            .andExpect(status().isForbidden())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void respostaDeOrigemListadaLevaOCabecalho() throws Exception {
        Mockito.when(gameService.catalog(Mockito.anyBoolean())).thenReturn(List.of());

        mvc.perform(get("/api/v1/games")
                .header(HttpHeaders.ORIGIN, WEB)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_" + Roles.PLAYER))))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, WEB));
    }

    @Test
    void preflightNaoExigeToken() throws Exception {
        mvc.perform(options("/api/v1/users")
                .header(HttpHeaders.ORIGIN, WEB)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isOk());
    }
}
