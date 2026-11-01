package com.project.mst.module.memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.module.games.GameModel;
import com.project.mst.module.memory.dto.MemoryMoveRequest;
import com.project.mst.module.memory.dto.MemoryMoveResponse;
import com.project.mst.module.memory.dto.MemoryRoundResponse;
import com.project.mst.module.memory.dto.StartRoundRequest;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.playsessions.PlaySessionStatus;
import com.project.mst.module.playsessions.dto.PlaySessionResponse;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.users.UserModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemoryServiceTest {

    @Mock CardThemeRepository themes;
    @Mock CardTemplateRepository cards;
    @Mock MemoryRoundRepository rounds;
    @Mock MemoryMoveRepository moves;
    @Mock PlaySessionService sessions;
    @Mock ScoreService scores;

    MemoryService service;
    CardThemeModel theme = new CardThemeModel();
    List<CardTemplateModel> templates;
    PlaySessionModel session;
    List<MemoryMoveModel> stored = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new MemoryService(themes, cards, rounds, moves, sessions, scores, new Random(1));
        theme.setId(UUID.randomUUID());
        theme.setName("Tema");
        theme.setBackImagePath("img/back.png");
        templates = IntStream.range(0, 9).mapToObj(i -> {
            CardTemplateModel card = new CardTemplateModel();
            card.setId(UUID.randomUUID());
            card.setTheme(theme);
            card.setLabel("Carta " + i);
            card.setImagePath("img/" + i + ".png");
            return card;
        }).toList();
        UserModel user = new UserModel();
        user.setId(UUID.randomUUID());
        GameModel game = new GameModel();
        game.setSlug("memoria");
        session = new PlaySessionModel();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setGame(game);
        session.setStatus(PlaySessionStatus.IN_PROGRESS);
        session.setStartedAt(new Date());
    }

    @Test
    void iniciarGera18PosicoesSemIdentificarAsCartas() {
        when(themes.findEnabledById(theme.getId())).thenReturn(Optional.of(theme));
        when(cards.findEnabledByThemeId(theme.getId())).thenReturn(templates);
        when(sessions.open("memoria", null, null)).thenReturn(session);
        when(rounds.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MemoryRoundResponse response = service.start(new StartRoundRequest(theme.getId(), null));

        assertThat(response.pairs()).isEqualTo(9);
        assertThat(response.cards()).hasSize(18).allMatch(c -> c.label() == null && c.imagePath() == null);
        assertThat(response.revealedPositions()).isEmpty();
    }

    @Test
    void segundaCartaIgualFechaOParEDiferenteContaErro() {
        MemoryRoundModel round = openRound();

        MemoryMoveResponse first = service.move(round.getId(), new MemoryMoveRequest(0));
        assertThat(first.card().label()).isEqualTo("Carta 0");
        assertThat(first.pairCompleted()).isFalse();
        assertThat(first.revealedPositions()).containsExactly(0);

        MemoryMoveResponse pair = service.move(round.getId(), new MemoryMoveRequest(1));
        assertThat(pair.pairCompleted()).isTrue();
        assertThat(pair.errors()).isZero();
        assertThat(pair.matchedPairs()).isEqualTo(1);
        assertThat(pair.revealedPositions()).containsExactly(0, 1);

        service.move(round.getId(), new MemoryMoveRequest(2));
        MemoryMoveResponse miss = service.move(round.getId(), new MemoryMoveRequest(4));
        assertThat(miss.pairCompleted()).isFalse();
        assertThat(miss.errors()).isEqualTo(1);
        assertThat(miss.previousCard().label()).isEqualTo("Carta 1");
        assertThat(miss.revealedPositions()).containsExactly(0, 1);
        verify(sessions, never()).finish(any());
    }

    @Test
    void cartaFixadaERepetidaRetornam409() {
        MemoryRoundModel round = openRound();
        service.move(round.getId(), new MemoryMoveRequest(0));

        assertThatThrownBy(() -> service.move(round.getId(), new MemoryMoveRequest(0)))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("MEMORY_POSITION_ALREADY_REVEALED");

        service.move(round.getId(), new MemoryMoveRequest(1));
        assertThatThrownBy(() -> service.move(round.getId(), new MemoryMoveRequest(1)))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("MEMORY_POSITION_ALREADY_MATCHED");
    }

    @Test
    void ultimoParEncerraASessao() {
        MemoryRoundModel round = openRound();
        when(sessions.finish(session.getId())).thenReturn(PlaySessionResponse.from(session, null));

        MemoryMoveResponse last = null;
        for (int i = 0; i < 18; i++) {
            last = service.move(round.getId(), new MemoryMoveRequest(i));
        }

        assertThat(last.finished()).isTrue();
        assertThat(last.matchedPairs()).isEqualTo(9);
        verify(sessions).finish(session.getId());
    }

    private MemoryRoundModel openRound() {
        List<UUID> layout = new ArrayList<>();
        for (CardTemplateModel card : templates) {
            layout.add(card.getId());
            layout.add(card.getId());
        }
        MemoryRoundModel round = new MemoryRoundModel();
        round.setId(UUID.randomUUID());
        round.setPlaySession(session);
        round.setTheme(theme);
        round.setPairs(9);
        round.setErrors(0);
        round.setLayout("[" + String.join(",", layout.stream().map(id -> "\"" + id + "\"").toList()) + "]");
        when(rounds.findActiveById(round.getId())).thenReturn(Optional.of(round));
        when(cards.findAllByIds(anyCollection())).thenReturn(templates);
        when(moves.findByRoundIdOrdered(round.getId())).thenAnswer(inv -> new ArrayList<>(stored));
        when(moves.save(any())).thenAnswer(inv -> {
            MemoryMoveModel move = inv.getArgument(0);
            if (!stored.contains(move)) {
                stored.add(move);
            }
            return move;
        });
        lenient().when(rounds.save(any())).thenAnswer(inv -> inv.getArgument(0));
        return round;
    }
}
