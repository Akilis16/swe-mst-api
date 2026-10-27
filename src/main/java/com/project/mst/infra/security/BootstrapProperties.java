package com.project.mst.infra.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.bootstrap")
public record BootstrapProperties(String adminEmail, String adminPassword) {

    public boolean adminConfigured() {
        return adminEmail != null && !adminEmail.isBlank()
            && adminPassword != null && !adminPassword.isBlank();
    }
}
