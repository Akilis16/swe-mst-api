package com.project.mst.infra.database.model;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass 
@Getter 
@Setter 
public abstract class SoftDelete 
{
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "id_user_created")
    private UUID id_user_created;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "created_at")
    private Date created_at;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "id_user_updated")
    private UUID id_user_updated;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "updated_at")
    private Date updated_at;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "id_user_deleted")
    private UUID id_user_deleted;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "deleted_at")
    private Date deleted_at;

    @PrePersist
    protected void onCreate()
    {
        this.created_at = new Date();
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updated_at = new Date();
    }

}
