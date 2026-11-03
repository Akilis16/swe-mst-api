package com.project.mst.module.playsessions.dto;

import java.time.LocalDate;
import java.util.List;

public record ProgressResponse(LocalDate from, LocalDate to, String game, List<ProgressPoint> series) {
}
