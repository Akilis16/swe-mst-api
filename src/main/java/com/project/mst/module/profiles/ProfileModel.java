package com.project.mst.module.profiles;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.routes.RouteModel;
import com.project.mst.module.users.UserModel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table (name="profile")
@Entity (name="profile")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class ProfileModel extends Model {
    @Column(name = "name", unique = true, nullable = false)
    protected String name;

    @JsonIgnoreProperties("profiles")
    @ManyToMany(mappedBy = "profiles", fetch = FetchType.LAZY)
    protected List<UserModel> users;

    @JsonIgnoreProperties("profiles")
    @ManyToMany(mappedBy = "profiles", fetch = FetchType.LAZY)
    protected List<RouteModel> routes;
}
