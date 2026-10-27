package com.project.mst.module.scores;

import com.project.mst.module.playsessions.PlaySessionModel;

public interface ScoreCalculator {

    String gameSlug();

    int calculate(PlaySessionModel session);
}
