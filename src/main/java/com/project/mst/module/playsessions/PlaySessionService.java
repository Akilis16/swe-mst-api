package com.project.mst.module.playsessions;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class PlaySessionService extends CrudService<PlaySessionModel> {

    public PlaySessionService(PlaySessionRepository repository) {
        super(repository);
    }
}
