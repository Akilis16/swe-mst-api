package com.project.mst.infra.security;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import com.project.mst.module.profiles.ProfileModel;
import com.project.mst.module.users.UserModel;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public JwtService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public String generate(UserModel user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("mst")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(properties.expirationSeconds()))
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("name", user.getName())
            .claim(SecurityConfig.ROLES_CLAIM, roles(user))
            .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expiresInSeconds() {
        return properties.expirationSeconds();
    }

    /** Nome do perfil normalizado como papel: "Administrador Geral" -> ADMINISTRADOR_GERAL. */
    public static List<String> roles(UserModel user) {
        if (user.getProfiles() == null) {
            return List.of();
        }
        return user.getProfiles().stream()
            .map(ProfileModel::getName)
            .filter(name -> name != null && !name.isBlank())
            .map(name -> name.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "_"))
            .distinct()
            .toList();
    }
}
