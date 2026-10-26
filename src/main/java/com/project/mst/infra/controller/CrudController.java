package com.project.mst.infra.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.project.mst.infra.database.model.Model;
import com.project.mst.infra.service.CrudService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public abstract class CrudController<T extends Model> {

    protected final CrudService<T> service;

    protected CrudController(CrudService<T> service) {
        this.service = service;
    }

    @GetMapping
    public List<T> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public T findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<T> create(@RequestBody T entity) {
        T created = service.create(entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public T update(@PathVariable UUID id, @RequestBody T entity) {
        return service.update(id, entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
