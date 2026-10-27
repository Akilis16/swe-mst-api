package com.project.mst.module.users;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.profiles.ProfileModel;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name="user")
@Entity(name="user")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class UserModel extends Model {
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", unique = true, nullable = false)
    protected String name;
    
    /** RN-D13: e-mail obrigatório, único e sempre armazenado normalizado (ver {@link #setEmail}). */
    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = "email", unique = true, nullable = false)
    protected String email;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash")
    protected String password;

    @Size(max = 500)
    @Column(name = "avatar_url")
    protected String avatar_url;

    /** RN-D01: um perfil só pode ser vinculado uma vez ao mesmo usuário. */
    @JsonIgnoreProperties({"users", "routes"})
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_profile",
        joinColumns = @JoinColumn(
            name = "user_id",
            referencedColumnName = "id"
        ),
        inverseJoinColumns = @JoinColumn(
            name = "profile_id",
            referencedColumnName = "id"
        ),
        uniqueConstraints = @UniqueConstraint(
            name = "uk_user_profile_user_id_profile_id",
            columnNames = {"user_id", "profile_id"}
        )
    ) 
    protected Set<ProfileModel> profiles = new LinkedHashSet<>();

    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
