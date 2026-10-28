package com.project.mst.module.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;

import com.project.mst.infra.controller.ExceptionHandlerController;
import com.project.mst.infra.security.JwtService;
import com.project.mst.infra.security.SecurityConfig;
import com.project.mst.module.auth.dto.LoginRequest;
import com.project.mst.module.auth.dto.TokenResponse;
import com.project.mst.module.users.MeController;
import com.project.mst.module.users.UserModel;
import com.project.mst.module.users.UserService;
import com.project.mst.module.users.dto.UserResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AuthController.class, MeController.class})
@Import({SecurityConfig.class, JwtService.class, ExceptionHandlerController.class})
@TestPropertySource(properties = {
    "security.jwt.secret=chave-de-teste-com-pelo-menos-32-bytes-ok",
    "security.jwt.expiration-seconds=3600"
})
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @MockitoBean AuthService authService;
    @MockitoBean UserService userService;

    @Test
    void endpointProtegidoSemTokenRetorna401() throws Exception {
        mvc.perform(get("/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenAdulteradoRetorna401() throws Exception {
        mvc.perform(get("/me").header("Authorization", "Bearer " + jwtService.generate(user()) + "x"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenValidoAcessaMeSemExporSenha() throws Exception {
        UserModel user = user();
        when(userService.me()).thenReturn(UserResponse.from(user));

        mvc.perform(get("/me").header("Authorization", "Bearer " + jwtService.generate(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("ana@example.com"))
            .andExpect(jsonPath("$.displayName").value("Ana"))
            .andExpect(jsonPath("$.preferences.timed").value(true))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void loginPublicoRetornaToken() throws Exception {
        when(authService.login(any(LoginRequest.class)))
            .thenReturn(TokenResponse.bearer("abc", 3600, UserResponse.from(user())));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@example.com\",\"password\":\"segredo123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("abc"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginComCredenciaisInvalidasRetorna401() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("Credenciais inválidas"));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@example.com\",\"password\":\"errada\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void registroComDadosInvalidosRetorna400ComCampos() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"\",\"email\":\"nao-e-email\",\"password\":\"123\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.displayName").exists())
            .andExpect(jsonPath("$.errors.email").exists())
            .andExpect(jsonPath("$.errors.password").exists());
    }

    private static UserModel user() {
        UserModel user = new UserModel();
        user.setId(UUID.randomUUID());
        user.setDisplayName("Ana");
        user.setEmail("ana@example.com");
        user.setProfiles(Set.of());
        return user;
    }
}
