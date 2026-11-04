package com.project.mst.module.profiles;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/profiles")
@Tag(name = "Perfis (ADMIN)", description = "Perfis de acesso PLAYER e ADMIN")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProfileModel> findAll() {
        return service.findAll();
    }
}
