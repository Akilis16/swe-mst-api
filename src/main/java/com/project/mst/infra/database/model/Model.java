package com.project.mst.infra.database.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass 
@Getter 
@Setter 
public abstract class Model extends SoftDelete {    
    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, unique = true, nullable = false)
    protected UUID id;
}
