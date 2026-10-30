package com.project.mst.module.memory;

import com.project.mst.infra.database.model.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "card_template")
@Entity(name = "card_template")
@Getter
@Setter
@NoArgsConstructor
public class CardTemplateModel extends Model {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    protected CardThemeModel theme;

    @NotBlank
    @Size(max = 500)
    @Column(name = "image_path", nullable = false, length = 500)
    protected String imagePath;

    @NotBlank
    @Size(max = 100)
    @Column(name = "label", nullable = false, length = 100)
    protected String label;

    @Column(name = "active", nullable = false)
    protected boolean active = true;
}
