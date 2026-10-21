package com.project.mst.infra.database.model;

import java.util.Date;

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
    
    @Column(name = "id_user_created")
    private Long id_user_created;
    @Column(name = "created_at")
    private Date created_at;
    
    @Column(name = "id_user_updated")
    private Long id_user_updated;
    @Column(name = "updated_at")
    private Date updated_at;

    @Column(name = "id_user_deleted")
    private Long id_user_deleted;
    @Column(name = "deleted_at")
    private Date deleted;

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
