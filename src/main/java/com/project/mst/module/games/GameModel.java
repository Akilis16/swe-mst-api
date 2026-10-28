package com.project.mst.module.games;

import com.project.mst.infra.database.model.Model;

import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "game")
@Entity(name = "game")
@Getter
@Setter
@NoArgsConstructor
public class GameModel extends Model {

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "slug em minúsculas, dígitos e hífens")
    @Column(name = "slug", unique = true, nullable = false, length = 50)
    protected String slug;

    @NotBlank
    @Size(max = 100)
    @Column(name = "title", nullable = false, length = 100)
    protected String title;

    @Column(name = "description", columnDefinition = "text")
    protected String description;

    @Size(max = 500)
    @Column(name = "image_path", length = 500)
    protected String imagePath;

    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    protected boolean active = true;

    public void setSlug(String slug) {
        this.slug = normalizeSlug(slug);
    }

    public static String normalizeSlug(String slug) {
        return slug == null ? null : slug.trim().toLowerCase(Locale.ROOT);
    }
}
