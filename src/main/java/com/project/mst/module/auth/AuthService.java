package com.project.mst.module.auth;

import java.util.LinkedHashSet;

import com.project.mst.infra.security.JwtService;
import com.project.mst.infra.security.Roles;
import com.project.mst.module.auth.dto.LoginRequest;
import com.project.mst.module.auth.dto.RegisterRequest;
import com.project.mst.module.auth.dto.TokenResponse;
import com.project.mst.module.profiles.ProfileRepository;
import com.project.mst.module.users.UserModel;
import com.project.mst.module.users.UserRepository;
import com.project.mst.module.users.dto.UserResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.mst.infra.controller.ApiException;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Credenciais inválidas";

    private final UserRepository users;
    private final ProfileRepository profiles;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, ProfileRepository profiles,
            PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.profiles = profiles;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = UserModel.normalizeEmail(request.email());

        if (users.findByEmail(email).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_USED", "E-mail já cadastrado");
        }

        UserModel user = new UserModel();
        user.setDisplayName(request.displayName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setProfiles(new LinkedHashSet<>());
        profiles.findByCode(Roles.PLAYER).ifPresent(user.getProfiles()::add);

        user = users.save(user);
        return token(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        UserModel user = users.findByEmail(UserModel.normalizeEmail(request.email()))
            .filter(found -> found.getDeleted_at() == null)
            .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));

        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }

        return token(user);
    }

    private TokenResponse token(UserModel user) {
        return TokenResponse.bearer(jwtService.generate(user), jwtService.expiresInSeconds(), UserResponse.from(user));
    }
}
