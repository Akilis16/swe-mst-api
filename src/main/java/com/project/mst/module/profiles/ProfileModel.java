package com.project.mst.module.profiles;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.users.UserModel;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "profile")
@Entity(name = "profile")
@Getter
@Setter
@NoArgsConstructor
public class ProfileModel extends Model {

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", unique = true, nullable = false, length = 50)
    protected String code;

    @JsonIgnore
    @ManyToMany(mappedBy = "profiles", fetch = FetchType.LAZY)
    protected Set<UserModel> users;
}
