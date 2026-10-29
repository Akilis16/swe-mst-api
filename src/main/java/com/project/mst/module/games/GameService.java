package com.project.mst.module.games;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.security.CurrentUser;
import com.project.mst.infra.service.CrudService;
import com.project.mst.module.games.dto.GameResponse;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.mst.infra.controller.ApiException;

@Service
public class GameService extends CrudService<GameModel> {

    private final GameRepository games;

    public GameService(GameRepository games) {
        super(games);
        this.games = games;
    }

    @Transactional(readOnly = true)
    public List<GameResponse> catalog(boolean includeInactive) {
        List<GameModel> result = includeInactive && CurrentUser.isAdmin()
            ? games.findAllActive()
            : games.findAllActiveAndEnabled();
        return result.stream().map(GameResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public GameResponse findBySlug(String slug) {
        String normalized = GameModel.normalizeSlug(slug);
        GameModel game = CurrentUser.isAdmin()
            ? games.findBySlug(normalized).filter(g -> g.getDeleted_at() == null)
                .orElseThrow(() -> new EntityNotFoundException("Jogo " + normalized + " não encontrado"))
            : games.findEnabledBySlug(normalized)
                .orElseThrow(() -> new EntityNotFoundException("Jogo " + normalized + " não encontrado"));
        return GameResponse.from(game);
    }

    @Transactional(readOnly = true)
    public GameModel requireEnabledBySlug(String slug) {
        String normalized = GameModel.normalizeSlug(slug);
        return games.findEnabledBySlug(normalized)
            .orElseThrow(() -> new EntityNotFoundException("Jogo " + normalized + " não encontrado"));
    }

    @Override
    @Transactional
    public GameModel create(GameModel entity) {
        entity.setSlug(entity.getSlug());
        ensureSlugAvailable(entity.getSlug(), null);
        return super.create(entity);
    }

    @Override
    @Transactional
    public GameModel update(UUID id, GameModel entity) {
        GameModel current = findById(id);
        entity.setSlug(current.getSlug());
        return super.update(id, entity);
    }

    @Transactional
    public GameResponse setActive(UUID id, boolean active) {
        GameModel game = findById(id);
        game.setActive(active);
        CurrentUser.id().ifPresent(game::setId_user_updated);
        return GameResponse.from(games.save(game));
    }

    private void ensureSlugAvailable(String slug, UUID currentId) {
        if (slug == null) {
            return;
        }
        games.findBySlug(slug)
            .filter(other -> currentId == null || !currentId.equals(other.getId()))
            .ifPresent(other -> {
                throw new ApiException(HttpStatus.CONFLICT, "GAME_SLUG_IN_USE", "Slug já em uso");
            });
    }
}
