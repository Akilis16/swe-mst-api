create table "user" (
    id uuid primary key,
    display_name varchar(100) not null,
    email varchar(255) not null,
    password_hash varchar(255),
    preferences jsonb,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_user_email unique (email)
);

create table profile (
    id uuid primary key,
    code varchar(50) not null,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_profile_code unique (code)
);

create table user_profile (
    user_id uuid not null references "user" (id),
    profile_id uuid not null references profile (id),
    primary key (user_id, profile_id)
);

create table game (
    id uuid primary key,
    slug varchar(50) not null,
    title varchar(100) not null,
    description text,
    image_path varchar(500),
    active boolean not null default true,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_game_slug unique (slug)
);

create table play_session (
    id uuid primary key,
    user_id uuid not null references "user" (id),
    game_id uuid not null references game (id),
    status varchar(20) not null,
    difficulty varchar(20),
    timed boolean not null default true,
    started_at timestamptz not null,
    ended_at timestamptz,
    client_meta jsonb,
    version integer not null default 0,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint ck_play_session_status check (status in ('IN_PROGRESS', 'FINISHED', 'ABANDONED')),
    constraint ck_play_session_difficulty check (difficulty is null or difficulty in ('EASY', 'MEDIUM', 'HARD')),
    constraint ck_play_session_ended_after_start check (ended_at is null or ended_at >= started_at)
);

create index ix_play_session_user_game_started on play_session (user_id, game_id, started_at);
create index ix_play_session_game on play_session (game_id);

create table score (
    id uuid primary key,
    play_session_id uuid not null references play_session (id),
    value integer not null,
    breakdown jsonb,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_score_play_session unique (play_session_id)
);
