package com.project.mst.module.users;

import com.project.mst.module.users.dto.Preferences;
import com.project.mst.module.users.dto.UserResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/me")
@Tag(name = "Conta", description = "Dados e preferências de acessibilidade do próprio usuário")
public class MeController {

    private final UserService service;

    public MeController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Dados do usuário autenticado")
    public UserResponse me() {
        return service.me();
    }

    @PatchMapping("/preferences")
    @Operation(summary = "Atualiza preferências de acessibilidade", description = "Mescla timed, highContrast, largeFont e reduceMotion; campos ausentes são mantidos.")
    public Preferences updatePreferences(@RequestBody Preferences patch) {
        return service.updatePreferences(patch);
    }
}
