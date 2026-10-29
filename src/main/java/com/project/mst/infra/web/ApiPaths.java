package com.project.mst.infra.web;

public final class ApiPaths {

    public static final String PREFIX = "/api/v1";

    private ApiPaths() {
    }

    public static String v1(String path) {
        return PREFIX + path;
    }
}
