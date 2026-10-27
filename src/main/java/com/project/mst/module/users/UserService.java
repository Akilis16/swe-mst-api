package com.project.mst.module.users;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.project.mst.infra.service.CrudService;
import com.project.mst.module.profiles.ProfileModel;
import com.project.mst.module.profiles.ProfileRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService extends CrudService<UserModel> {

    private final UserRepository users;
    private final ProfileRepository profiles;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, ProfileRepository profiles, PasswordEncoder passwordEncoder) {
        super(users);
        this.users = users;
        this.profiles = profiles;
        this.passwordEncoder = passwordEncoder;
    }

    /** A senha nunca é gravada em texto puro, mesmo pelo CRUD de /users. */
    @Override
    @Transactional
    public UserModel create(UserModel entity) {
        normalize(entity);
        ensureEmailAvailable(entity.getEmail(), null);
        ensureNameAvailable(entity.getName(), null);
        entity.setProfiles(resolveProfiles(entity.getProfiles()));
        entity.setPassword(hashIfPresent(entity.getPassword()));
        return super.create(entity);
    }

    /** Senha ausente na atualização mantém o hash atual; senha presente é re-hasheada. */
    @Override
    @Transactional
    public UserModel update(UUID id, UserModel entity) {
        UserModel current = findById(id);

        normalize(entity);
        ensureEmailAvailable(entity.getEmail(), id);
        ensureNameAvailable(entity.getName(), id);
        entity.setProfiles(resolveProfiles(entity.getProfiles()));

        String password = entity.getPassword();
        if (password == null || password.isBlank()) {
            entity.setPassword(current.getPassword());
        } else {
            entity.setPassword(passwordEncoder.encode(password));
        }

        return super.update(id, entity);
    }

    /** RN-D13: e-mail único, comparado já normalizado (o setter de UserModel normaliza). */
    public void ensureEmailAvailable(String email, UUID currentUserId) {
        if (email == null) {
            return;
        }
        users.findByEmail(UserModel.normalizeEmail(email))
            .filter(other -> !other.getId().equals(currentUserId))
            .ifPresent(other -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
            });
    }

    public void ensureNameAvailable(String name, UUID currentUserId) {
        if (name == null) {
            return;
        }
        users.findByName(name)
            .filter(other -> !other.getId().equals(currentUserId))
            .ifPresent(other -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Nome de usuário já em uso");
            });
    }

    /**
     * RN-D01: cada perfil entra uma única vez no vínculo. Perfis repetidos na requisição
     * são ignorados; perfil inexistente ou excluído é rejeitado.
     */
    private Set<ProfileModel> resolveProfiles(Collection<ProfileModel> requested) {
        Set<ProfileModel> resolved = new LinkedHashSet<>();
        if (requested == null) {
            return resolved;
        }

        Set<UUID> seen = new HashSet<>();
        for (ProfileModel profile : requested) {
            if (profile == null || profile.getId() == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Perfil sem id");
            }
            UUID profileId = profile.getId();
            if (!seen.add(profileId)) {
                continue;
            }
            resolved.add(profiles.findActiveById(profileId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "Perfil " + profileId + " não encontrado")));
        }
        return resolved;
    }

    private static void normalize(UserModel entity) {
        if (entity.getName() != null) {
            entity.setName(entity.getName().trim());
        }
        entity.setEmail(entity.getEmail());
    }

    private String hashIfPresent(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return null;
        }
        return passwordEncoder.encode(rawPassword);
    }
}
