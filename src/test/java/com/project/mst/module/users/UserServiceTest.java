package com.project.mst.module.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.project.mst.module.profiles.ProfileModel;
import com.project.mst.module.profiles.ProfileRepository;
import com.project.mst.module.users.dto.Preferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository users;
    @Mock ProfileRepository profiles;
    @Mock PasswordEncoder passwordEncoder;

    UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(users, profiles, passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void emailEhNormalizadoAoGravar() {
        when(users.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(users.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserModel saved = service.create(user("  Ana ", "  Ana@Example.COM "));

        assertThat(saved.getEmail()).isEqualTo("ana@example.com");
        assertThat(saved.getDisplayName()).isEqualTo("Ana");
    }

    @Test
    void emailDuplicadoEhRejeitadoMesmoComCaixaDiferente() {
        UserModel existing = user("Outra", "ana@example.com");
        existing.setId(UUID.randomUUID());
        when(users.findByEmail("ana@example.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.create(user("Ana", "ANA@example.com")))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void atualizarMantemSenhaEPreferenciasQuandoNaoInformadas() {
        UUID id = UUID.randomUUID();
        UserModel current = user("Ana", "ana@example.com");
        current.setId(id);
        current.setPassword("{bcrypt}hash");
        current.setPreferences("{\"timed\":false}");

        when(users.findActiveById(id)).thenReturn(Optional.of(current));
        when(users.findByEmail("ana@example.com")).thenReturn(Optional.of(current));
        when(users.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserModel updated = service.update(id, user("Ana", "ANA@example.com"));

        assertThat(updated.getPassword()).isEqualTo("{bcrypt}hash");
        assertThat(updated.getPreferences()).isEqualTo("{\"timed\":false}");
    }

    @Test
    void papelPodeSerInformadoPorCodigoERepetidoEntraUmaVez() {
        ProfileModel admin = new ProfileModel();
        admin.setId(UUID.randomUUID());
        admin.setCode("ADMIN");
        when(users.findByEmail(any())).thenReturn(Optional.empty());
        when(profiles.findByCode("ADMIN")).thenReturn(Optional.of(admin));
        when(profiles.findActiveById(admin.getId())).thenReturn(Optional.of(admin));
        when(users.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileModel byCode = new ProfileModel();
        byCode.setCode(" admin ");
        ProfileModel byId = new ProfileModel();
        byId.setId(admin.getId());
        UserModel input = user("Ana", "ana@example.com");
        input.setProfiles(new LinkedHashSet<>(List.of(byCode, byId)));

        assertThat(service.create(input).getProfiles()).containsExactly(admin);
    }

    @Test
    void papelInexistenteEhRejeitado() {
        when(users.findByEmail(any())).thenReturn(Optional.empty());
        when(profiles.findByCode("GESTOR")).thenReturn(Optional.empty());

        ProfileModel unknown = new ProfileModel();
        unknown.setCode("GESTOR");
        UserModel input = user("Ana", "ana@example.com");
        input.setProfiles(new LinkedHashSet<>(List.of(unknown)));

        assertThatThrownBy(() -> service.create(input))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatusCode())
            .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void patchDePreferenciasMantemCamposNaoInformados() {
        UUID id = UUID.randomUUID();
        UserModel current = user("Ana", "ana@example.com");
        current.setId(id);
        current.setPreferences("{\"highContrast\":true}");
        when(users.findActiveById(id)).thenReturn(Optional.of(current));
        when(users.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        authenticateAs(id);

        Preferences result = service.updatePreferences(new Preferences(false, null, null, null));

        assertThat(result).isEqualTo(new Preferences(false, true, false, false));
        assertThat(Preferences.fromJson(current.getPreferences())).isEqualTo(result);
    }

    @Test
    void preferenciasAusentesUsamPadrao() {
        assertThat(Preferences.fromJson(null)).isEqualTo(Preferences.DEFAULTS);
        assertThat(Preferences.fromJson("{}").timed()).isTrue();
    }

    private static UserModel user(String name, String email) {
        UserModel user = new UserModel();
        user.setDisplayName(name);
        user.setEmail(email);
        return user;
    }

    private static void authenticateAs(UUID id) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject(id.toString()).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
    }
}
