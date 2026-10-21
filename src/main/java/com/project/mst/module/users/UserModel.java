package com.project.mst.module.users;

import com.project.mst.infra.database.model.Model; 

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
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
    @Column(name = "name", unique = true, nullable = false)
    protected String name;
    
    @Column(name = "email", unique = true, nullable = false)
    protected String email;
    
    @Column(name = "password")
    protected String password;

    @Column(name = "avatar_url")
    protected String avatar_url;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_profile",
        joinColumns = @JoinColumn(
            name = "id_profile",
            referencedColumnName = "id"
        )
    ) 
    protected List<ProfileModel> profiles;
}
