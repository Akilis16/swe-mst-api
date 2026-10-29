package com.project.mst.module.users;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.project.mst.infra.security.CurrentUser;
import com.project.mst.infra.service.CrudService;
import com.project.mst.module.profiles.ProfileModel;
import com.project.mst.module.profiles.ProfileRepository;
import com.project.mst.module.users.dto.Preferences;
import com.project.mst.module.users.dto.UserResponse;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.mst.infra.controller.ApiException;

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

    @Override
    @Transactional
    public UserModel create(UserModel entity) {
        normalize(entity);
        ensureEmailAvailable(entity.getEmail(), null);
        entity.setProfiles(resolveProfiles(entity.getProfiles()));
        entity.setPassword(hashIfPresent(entity.getPassword()));
        return super.create(entity);
    }

    @Override
    @Transactional
    public UserModel update(UUID id, UserModel entity) {
        UserModel current = findById(id);

        normalize(entity);
        ensureEmailAvailable(entity.getEmail(), id);
        entity.setProfiles(resolveProfiles(entity.getProfiles()));
        entity.setPreferences(current.getPreferences());

        String password = entity.getPassword();
        if (password == null || password.isBlank()) {
            entity.setPassword(current.getPassword());
        } else {
            entity.setPassword(passwordEncoder.encode(password));
        }

        return super.update(id, entity);
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return UserResponse.from(currentUser());
    }

    @Transactional
    public Preferences updatePreferences(Preferences patch) {
        UserModel user = currentUser();
        Preferences merged = Preferences.fromJson(user.getPreferences()).merge(patch);
        user.setPreferences(merged.toJson());
        users.save(user);
        return merged;
    }

    @Transactional(readOnly = true)
    public Preferences preferencesOf(UserModel user) {
        return Preferences.fromJson(user.getPreferences());
    }

    public void ensureEmailAvailable(String email, UUID currentUserId) {
        if (email == null) {
            return;
        }
        users.findByEmail(UserModel.normalizeEmail(email))
            .filter(other -> currentUserId == null || !currentUserId.equals(other.getId()))
            .ifPresent(other -> {
                throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_USED", "E-mail já cadastrado");
            });
    }

    private Set<ProfileModel> resolveProfiles(Collection<ProfileModel> requested) {
        Set<ProfileModel> resolved = new LinkedHashSet<>();
        if (requested == null) {
            return resolved;
        }

        Set<UUID> seen = new HashSet<>();
        for (ProfileModel role : requested) {
            ProfileModel found = findProfile(role);
            if (seen.add(found.getId())) {
                resolved.add(found);
            }
        }
        return resolved;
    }

    private ProfileModel findProfile(ProfileModel role) {
        if (role != null && role.getId() != null) {
            return profiles.findActiveById(role.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "PROFILE_NOT_FOUND", "Perfil " + role.getId() + " não encontrado"));
        }
        if (role != null && role.getCode() != null && !role.getCode().isBlank()) {
            String code = role.getCode().trim().toUpperCase(Locale.ROOT);
            return profiles.findByCode(code)
                .orElseThrow(() -> new ApiException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "PROFILE_NOT_FOUND", "Perfil " + code + " não encontrado"));
        }
        throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "PROFILE_REFERENCE_INVALID", "Perfil sem id nem código");
    }

    private UserModel currentUser() {
        UUID id = CurrentUser.id()
            .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Usuário não autenticado"));
        return users.findActiveById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado"));
    }

    private static void normalize(UserModel entity) {
        if (entity.getDisplayName() != null) {
            entity.setDisplayName(entity.getDisplayName().trim());
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
