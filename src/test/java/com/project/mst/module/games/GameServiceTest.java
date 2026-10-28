package com.project.mst.module.games;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock GameRepository games;

    GameService service;

    @BeforeEach
    void setUp() {
        service = new GameService(games);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void catalogoDoJogadorSoTrazJogosAtivos() {
        when(games.findAllActiveAndEnabled()).thenReturn(List.of(game("velha", true)));

        assertThat(service.catalog(true)).extracting("slug").containsExactly("velha");
        verify(games, never()).findAllActive();
    }

    @Test
    void adminPodeListarInativos() {
        authenticateAs("ADMIN");
        when(games.findAllActive()).thenReturn(List.of(game("velha", true), game("2048", false)));

        assertThat(service.catalog(true)).hasSize(2);
    }

    @Test
    void jogoInativoNaoEhEncontradoPeloJogador() {
        when(games.findEnabledBySlug("2048")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findBySlug(" 2048 ")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void slugEhNormalizadoEDuplicadoRetorna409() {
        when(games.findBySlug("velha")).thenReturn(Optional.of(game("velha", true)));

        assertThatThrownBy(() -> service.create(game(" Velha ", true)))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void slugNaoMudaNaAtualizacao() {
        GameModel current = game("velha", true);
        current.setId(UUID.randomUUID());
        when(games.findActiveById(current.getId())).thenReturn(Optional.of(current));
        when(games.save(any(GameModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameModel updated = service.update(current.getId(), game("outro-slug", true));

        assertThat(updated.getSlug()).isEqualTo("velha");
    }

    @Test
    void ativacaoAlteraSomenteOCampoActive() {
        GameModel current = game("velha", true);
        current.setId(UUID.randomUUID());
        when(games.findActiveById(current.getId())).thenReturn(Optional.of(current));
        when(games.save(any(GameModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.setActive(current.getId(), false).active()).isFalse();
        assertThat(current.getSlug()).isEqualTo("velha");
    }

    private static GameModel game(String slug, boolean active) {
        GameModel game = new GameModel();
        game.setSlug(slug);
        game.setTitle("Jogo");
        game.setActive(active);
        return game;
    }

    private static void authenticateAs(String role) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject(UUID.randomUUID().toString()).build();
        SecurityContextHolder.getContext().setAuthentication(
            new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
