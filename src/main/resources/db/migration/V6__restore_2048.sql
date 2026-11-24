insert into game (id, slug, title, description, image_path, active, created_at, updated_at) values
  ('1c6b78a1-1313-55ac-a348-b9b5640a138d', '2048', '2048', 'Deslize as peças e junte números iguais até chegar a 2048.', 'img/jogos/2048.png', true, now(), now());

create table game_2048_round (
    id uuid primary key,
    play_session_id uuid not null references play_session (id),
    board jsonb not null,
    points integer not null default 0,
    moves integer not null default 0,
    max_tile integer not null default 0,
    result varchar(10),
    version integer not null default 0,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_game_2048_round_play_session unique (play_session_id),
    constraint ck_game_2048_round_result check (result is null or result in ('WIN', 'LOSS')),
    constraint ck_game_2048_round_points check (points >= 0 and moves >= 0 and max_tile >= 0)
);
