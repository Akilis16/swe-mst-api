package com.project.mst.module.quizzes;

public final class QuizPerformance {

    private QuizPerformance() {
    }

    public static int percent(int correct, int total) {
        return total <= 0 ? 0 : Math.floorDiv(correct * 100, total);
    }

    public static String message(int performance) {
        if (performance >= 90) {
            return "Excelente";
        }
        if (performance >= 70) {
            return "Muito bom";
        }
        if (performance >= 50) {
            return "Bom";
        }
        return "Pode melhorar";
    }
}
