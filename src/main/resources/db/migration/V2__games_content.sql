create table card_theme (
    id uuid primary key,
    name varchar(100) not null,
    back_image_path varchar(500) not null,
    active boolean not null default true,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_card_theme_name unique (name)
);

create table card_template (
    id uuid primary key,
    theme_id uuid not null references card_theme (id),
    image_path varchar(500) not null,
    label varchar(100) not null,
    active boolean not null default true,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz
);

create index ix_card_template_theme on card_template (theme_id);

create table memory_round (
    id uuid primary key,
    play_session_id uuid not null references play_session (id),
    theme_id uuid not null references card_theme (id),
    pairs integer not null,
    errors integer not null default 0,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_memory_round_play_session unique (play_session_id)
);

create table memory_move (
    id uuid primary key,
    round_id uuid not null references memory_round (id),
    move_number integer not null,
    card_template_id uuid not null references card_template (id),
    position integer not null,
    matched boolean not null default false,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_memory_move_round_number unique (round_id, move_number),
    constraint ck_memory_move_position check (position >= 0)
);

create table tic_tac_toe_match (
    id uuid primary key,
    play_session_id uuid not null references play_session (id),
    mode varchar(20) not null,
    result varchar(10),
    version integer not null default 0,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_tic_tac_toe_match_play_session unique (play_session_id),
    constraint ck_tic_tac_toe_match_mode check (mode in ('LOCAL_2P', 'VS_COMPUTER')),
    constraint ck_tic_tac_toe_match_result check (result is null or result in ('X_WINS', 'O_WINS', 'DRAW'))
);

create table tic_tac_toe_move (
    id uuid primary key,
    match_id uuid not null references tic_tac_toe_match (id),
    move_number integer not null,
    row_idx integer not null,
    col_idx integer not null,
    mark varchar(1) not null,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_tic_tac_toe_move_number unique (match_id, move_number),
    constraint uk_tic_tac_toe_move_cell unique (match_id, row_idx, col_idx),
    constraint ck_tic_tac_toe_move_row check (row_idx between 0 and 2),
    constraint ck_tic_tac_toe_move_col check (col_idx between 0 and 2),
    constraint ck_tic_tac_toe_move_mark check (mark in ('X', 'O'))
);

create table quiz (
    id uuid primary key,
    game_id uuid not null references game (id),
    title varchar(100) not null,
    active boolean not null default true,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz
);

create table question (
    id uuid primary key,
    quiz_id uuid not null references quiz (id),
    category varchar(50),
    text text not null,
    active boolean not null default true,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz
);

create index ix_question_quiz on question (quiz_id);

create table option (
    id uuid primary key,
    question_id uuid not null references question (id),
    text text not null,
    is_correct boolean not null default false,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz
);

create index ix_option_question on option (question_id);
create unique index ux_option_one_correct_per_question on option (question_id) where is_correct;

create table quiz_attempt (
    id uuid primary key,
    play_session_id uuid not null references play_session (id),
    quiz_id uuid not null references quiz (id),
    correct_count integer not null default 0,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_quiz_attempt_play_session unique (play_session_id)
);

create table quiz_answer (
    id uuid primary key,
    attempt_id uuid not null references quiz_attempt (id),
    question_id uuid not null references question (id),
    option_id uuid references option (id),
    correct boolean not null,
    answered_at timestamptz not null,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_quiz_answer_attempt_question unique (attempt_id, question_id)
);

create table sudoku_puzzle (
    id uuid primary key,
    difficulty varchar(20) not null,
    givens varchar(81) not null,
    solution varchar(81) not null,
    active boolean not null default true,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint ck_sudoku_puzzle_difficulty check (difficulty in ('EASY', 'MEDIUM', 'HARD')),
    constraint ck_sudoku_puzzle_givens check (length(givens) = 81 and givens ~ '^[0-9]{81}$'),
    constraint ck_sudoku_puzzle_solution check (length(solution) = 81 and solution ~ '^[1-9]{81}$')
);

create table sudoku_attempt (
    id uuid primary key,
    play_session_id uuid not null references play_session (id),
    puzzle_id uuid not null references sudoku_puzzle (id),
    mistakes integer not null default 0,
    time_limit_seconds integer,
    solved boolean not null default false,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_sudoku_attempt_play_session unique (play_session_id)
);

create table sudoku_entry (
    id uuid primary key,
    attempt_id uuid not null references sudoku_attempt (id),
    cell_idx integer not null,
    value integer,
    correct boolean not null,
    entered_at timestamptz not null,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint ck_sudoku_entry_cell check (cell_idx between 0 and 80),
    constraint ck_sudoku_entry_value check (value is null or value between 1 and 9)
);

create index ix_sudoku_entry_attempt on sudoku_entry (attempt_id);

create table word (
    id uuid primary key,
    text varchar(50) not null,
    category varchar(50) not null,
    difficulty varchar(20),
    active boolean not null default true,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_word_text_category unique (text, category),
    constraint ck_word_difficulty check (difficulty is null or difficulty in ('EASY', 'MEDIUM', 'HARD'))
);

create table hangman_round (
    id uuid primary key,
    play_session_id uuid not null references play_session (id),
    word_id uuid not null references word (id),
    max_errors integer not null default 6,
    result varchar(10),
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_hangman_round_play_session unique (play_session_id),
    constraint ck_hangman_round_result check (result is null or result in ('WIN', 'LOSS'))
);

create table hangman_guess (
    id uuid primary key,
    round_id uuid not null references hangman_round (id),
    letter varchar(1) not null,
    correct boolean not null,
    id_user_created uuid,
    created_at timestamptz,
    id_user_updated uuid,
    updated_at timestamptz,
    id_user_deleted uuid,
    deleted_at timestamptz,
    constraint uk_hangman_guess_round_letter unique (round_id, letter)
);
