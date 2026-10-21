package com.project.mst.module.profiles;

import com.project.mst.infra.database.model.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
}
