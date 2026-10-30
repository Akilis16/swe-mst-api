package com.project.mst.module.memory;

import com.project.mst.infra.database.model.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "card_theme")
@Entity(name = "card_theme")
@Getter
@Setter
@NoArgsConstructor
public class CardThemeModel extends Model {

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", unique = true, nullable = false, length = 100)
    protected String name;

    @NotBlank
    @Size(max = 500)
    @Column(name = "back_image_path", nullable = false, length = 500)
    protected String backImagePath;

    @Column(name = "active", nullable = false)
    protected boolean active = true;
}
