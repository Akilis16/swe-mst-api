package com.project.mst.infra.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ApiException extends ResponseStatusException {

    private final String code;

    public ApiException(HttpStatus status, String code, String detail) {
        super(status, detail);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
