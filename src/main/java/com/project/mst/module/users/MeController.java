package com.project.mst.module.users;

import com.project.mst.module.users.dto.Preferences;
import com.project.mst.module.users.dto.UserResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
public class MeController {

    private final UserService service;

    public MeController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public UserResponse me() {
        return service.me();
    }

    @PatchMapping("/preferences")
    public Preferences updatePreferences(@RequestBody Preferences patch) {
        return service.updatePreferences(patch);
    }
}
