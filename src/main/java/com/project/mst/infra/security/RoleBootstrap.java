package com.project.mst.infra.security;

import java.util.LinkedHashSet;
import java.util.Set;

import com.project.mst.module.profiles.ProfileModel;
import com.project.mst.module.profiles.ProfileRepository;
import com.project.mst.module.users.UserModel;
import com.project.mst.module.users.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleBootstrap.class);

    private final ProfileRepository profiles;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapProperties properties;

    public RoleBootstrap(ProfileRepository profiles, UserRepository users,
            PasswordEncoder passwordEncoder, BootstrapProperties properties) {
        this.profiles = profiles;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ProfileModel admin = ensureProfile(Roles.ADMIN);
        ensureProfile(Roles.PLAYER);

        if (!properties.adminConfigured()) {
            return;
        }
        if (admin.getUsers() != null && admin.getUsers().stream().anyMatch(u -> u.getDeleted_at() == null)) {
            return;
        }

        String email = UserModel.normalizeEmail(properties.adminEmail());
        if (users.findByEmail(email).isPresent()) {
            log.warn("Usuário {} já existe; não foi promovido a ADMIN automaticamente", email);
            return;
        }

        UserModel user = new UserModel();
        user.setName("admin");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(properties.adminPassword()));
        Set<ProfileModel> set = new LinkedHashSet<>();
        set.add(admin);
        user.setProfiles(set);
        users.save(user);
        log.info("Primeiro administrador criado: {}", email);
    }

    private ProfileModel ensureProfile(String name) {
        return profiles.findByName(name).orElseGet(() -> {
            ProfileModel profile = new ProfileModel();
            profile.setName(name);
            log.info("Perfil {} criado", name);
            return profiles.save(profile);
        });
    }
}
