package com.project.mst.infra.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.project.mst.infra.database.model.Model;
import com.project.mst.infra.database.repository.BaseRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public abstract class CrudService<T extends Model> {

    protected final BaseRepository<T> repository;

    protected CrudService(BaseRepository<T> repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<T> findAll() {
        return repository.findAllActive();
    }

    @Transactional(readOnly = true)
    public List<T> findAllWithTrashed() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public T findById(UUID id) {
        return repository.findActiveById(id)
            .orElseThrow(() -> new EntityNotFoundException("Registro " + id + " não encontrado"));
    }

    @Transactional(readOnly = true)
    public T findByIdWithTrashed(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Registro " + id + " não encontrado"));
    }

    @Transactional
    public T create(T entity) {
        entity.setId(null);
        return repository.save(entity);
    }

    @Transactional
    public T update(UUID id, T entity) {
        T current = findById(id);

        entity.setId(current.getId());
        entity.setCreated_at(current.getCreated_at());
        entity.setId_user_created(current.getId_user_created());

        return repository.save(entity);
    }

    @Transactional
    public void delete(UUID id) {
        T current = findById(id);
        current.setDeleted_at(new Date());
        repository.save(current);
    }
}
