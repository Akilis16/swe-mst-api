package com.project.mst.module.playsessions.dto;

/** Resumo do resultado em JSON. A pontuação nunca vem do cliente (CA-031.1). */
public record FinishSessionRequest(String resultSummary) {
}
