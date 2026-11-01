package com.project.mst.module.quizzes.dto;

import java.util.UUID;

import com.project.mst.module.quizzes.OptionModel;

public record OptionView(UUID id, String text) {

    public static OptionView from(OptionModel option) {
        return new OptionView(option.getId(), option.getText());
    }
}
