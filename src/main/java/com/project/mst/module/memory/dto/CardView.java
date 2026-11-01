package com.project.mst.module.memory.dto;

import com.project.mst.module.memory.CardTemplateModel;

public record CardView(int position, String label, String imagePath) {

    public static CardView hidden(int position) {
        return new CardView(position, null, null);
    }

    public static CardView revealed(int position, CardTemplateModel card) {
        return new CardView(position, card.getLabel(), card.getImagePath());
    }
}
