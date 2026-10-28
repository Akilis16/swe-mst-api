package com.project.mst.module.routes;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.profiles.ProfileModel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table (name="route")
@Entity (name="route")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class RouteModel extends Model {

    @Column (name = "name", unique = true, nullable = false)
    protected String name;
    
    @Column (name = "url", unique = true, nullable = false)
    protected String url;

    @JsonIgnoreProperties({"users", "routes"})
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "route_profile",
        joinColumns = @JoinColumn(
            name = "route_id",
            referencedColumnName = "id"
        ),
        inverseJoinColumns = @JoinColumn(
            name = "profile_id",
            referencedColumnName = "id"
        )
    )
    protected List<ProfileModel> profiles;
}
