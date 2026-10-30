# swe-mst-api

Api for the system MST to the development of cognitive abilities 


## Stacks

- Language: Java 21
- Framework: SpringBoot 4.1
- Build Tool: Maven
- Architecture: Package by Feature
- Patterns: SoftDeletes
- Database: Postgres 15
- Containerization: Docker & Docker Compose

## How to run

### Clone the repository

``` bash
    git clone https://github.com/Akilis16/swe-mst-api
    cd ./swe-mst-api
```

### Make the .env file

``` bash
    cp .env.example .env
```

### Run the application

``` bash
    mvn spring-boot:run OR docker compose up -d
```

## API conventions

- Base path: every endpoint lives under `/api/v1` (for example `POST /api/v1/auth/login`).
- JSON in `camelCase`, dates in ISO-8601 (UTC), ids are UUIDs.
- Errors follow RFC 9457 Problem Details (`application/problem+json`) and carry a stable `code`:

| Status | `code` | When |
|---|---|---|
| `400` | `MALFORMED_REQUEST`, `INVALID_JSON` | Body cannot be parsed, or a JSON field is not valid JSON |
| `401` | `UNAUTHENTICATED`, `INVALID_CREDENTIALS` | Missing/expired/tampered token; wrong e-mail or password |
| `403` | `FORBIDDEN`, `SESSION_FORBIDDEN` | Insufficient role; someone else's session |
| `404` | `NOT_FOUND` | Unknown resource or inactive game |
| `409` | `EMAIL_ALREADY_USED`, `GAME_SLUG_IN_USE`, `SESSION_ALREADY_ENDED`, `SESSION_ABANDONED`, `SESSION_ALREADY_SCORED`, `DATA_INTEGRITY` | State or uniqueness conflicts |
| `422` | `VALIDATION_FAILED`, `PROFILE_NOT_FOUND`, `PROFILE_REFERENCE_INVALID`, `SUDOKU_PUZZLE_INVALID` | Field validation (`errors: [{field, message}]`) and content rules |

- OpenAPI generated from the code: `GET /v3/api-docs` (JSON) and `/swagger-ui.html`, both public.

## Authentication

All endpoints require a JWT bearer token, except `POST /api/v1/auth/register`, `POST /api/v1/auth/login`
and the OpenAPI pages. Tokens are signed with HMAC-SHA256 using `JWT_SECRET` (at least 32 characters)
and expire after `JWT_EXPIRATION_SECONDS` (default 3600). Passwords are stored as BCrypt hashes.

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | public | Creates a user (`displayName`, `email`, `password` ≥ 8 chars) with profile `PLAYER` and returns a token. |
| `POST` | `/api/v1/auth/login` | public | Returns a token for valid `email` + `password`; `401` otherwise. |
| `GET` | `/api/v1/me` | bearer | The authenticated user: `id`, `displayName`, `email`, `roles`, `preferences`. |
| `PATCH` | `/api/v1/me/preferences` | bearer | Merges `{timed, highContrast, largeFont, reduceMotion}`; omitted fields keep their value. |

``` bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@example.com","password":"segredo123"}'
# {"accessToken":"<jwt>","tokenType":"Bearer","expiresIn":3600,"user":{...}}

curl http://localhost:8080/api/v1/games -H "Authorization: Bearer <jwt>"
```

The token carries the user id as `sub` and the user's profile codes as `roles` (`ROLE_<CODE>` authorities).

### Profiles

Profiles `ADMIN` and `PLAYER` are created automatically on startup. New accounts get `PLAYER`.
Set `ADMIN_EMAIL` and `ADMIN_PASSWORD` in `.env` to create the first administrator when no
`ADMIN` user exists yet. Admins assign profiles through `/api/v1/users` with `"profiles": [{"code": "ADMIN"}]`.
Access rules live in `SecurityConfig`:

| Resources | GET | POST / PUT / PATCH / DELETE |
|---|---|---|
| `/users`, `/profiles`, `/admin/**` | ADMIN | ADMIN |
| `/games` | any user | ADMIN |
| `/play-sessions` (list) | ADMIN | — |
| `/play-sessions/{id}`, `/me/**` | owner (ADMIN sees all) | owner |

### Catalog

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/games` | Active games (`slug`, `title`, `description`, `imagePath`). ADMIN may pass `?includeInactive=true`. |
| `GET` | `/api/v1/games/{slug}` | One game; inactive games are `404` for players. |
| `POST` / `PUT` / `DELETE` | `/api/v1/games`, `/api/v1/games/{id}` | ADMIN. The `slug` is unique and cannot change after creation. |
| `PATCH` | `/api/v1/games/{id}/active` | ADMIN. `{"active": false}` hides the game from the catalog and blocks new sessions. |

### Play sessions and scores

A play session is the single record of a user playing a game. The server sets the user
(from the token), the game, the timestamps and the status; the client never sends a score.

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/play-sessions` | Starts a session: `{"gameSlug": "velha", "difficulty": "EASY", "timed": true, "clientMeta": "<json>"}`. `difficulty` and `clientMeta` are optional; `timed` defaults to the user's preference. |
| `POST` | `/api/v1/play-sessions/{id}/finish` | Ends the session; a score is computed server-side when the game has a `ScoreCalculator`. Repeating the call returns the same state. |
| `POST` | `/api/v1/play-sessions/{id}/abandon` | Marks the session as abandoned; `409` if already ended. |
| `GET` | `/api/v1/me/play-sessions?game=&page=&size=` | Own sessions, newest first, each with its `score` (`value`, `breakdown`). |
| `GET` | `/api/v1/play-sessions/{id}` | One session, owner or ADMIN. |
| `GET` | `/api/v1/play-sessions?game=&page=&size=` | All sessions, ADMIN only. |

A session holds at most one score and scores are never written through the API.

### Content administration (ADMIN)

Game content is managed only by administrators, so answer keys (`isCorrect`, sudoku `solution`,
the hangman word) never reach players. CRUD at `/api/v1/admin/{card-themes, card-templates, quizzes, questions, options, puzzles, words}`.
Puzzles are rejected (`422 SUDOKU_PUZZLE_INVALID`) unless the solution is a valid grid, consistent with the
givens, and the givens admit exactly one solution. Words are stored upper-case, A–Z only.

## Database

The schema is owned by Flyway (`src/main/resources/db/migration`) and Hibernate only validates it
(`spring.jpa.hibernate.ddl-auto=validate`). Keep `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` in `.env`.

| Migration | Content |
|---|---|
| `V1__core.sql` | `user`, `profile`, `user_profile`, `game`, `play_session`, `score` |
| `V2__games_content.sql` | Memory (`card_theme`, `card_template`, `memory_round`, `memory_move`), tic-tac-toe (`tic_tac_toe_match`, `tic_tac_toe_move`), quiz (`quiz`, `question`, `option`, `quiz_attempt`, `quiz_answer`), sudoku (`sudoku_puzzle`, `sudoku_attempt`, `sudoku_entry`), hangman (`word`, `hangman_round`, `hangman_guess`) with the uniqueness and check constraints from the data model |
| `V3__seed_content.sql` | 6 games (2048 inactive), 28 reviewed quiz questions with category, 40 hangman words, 15 generated sudoku puzzles with unique solutions, 3 card themes with 27 labelled cards |

Sudoku grids are stored row by row as 81 digits, `0` meaning an empty cell.
A database created before Flyway was introduced must be recreated (`docker compose down -v`).

## Tests

`./mvnw test` needs no external database: unit and web-slice tests run with mocks, and
`MstApplicationTests` starts an embedded PostgreSQL 16 (downloaded from Maven Central on first run)
to apply the migrations, validate the schema, bootstrap the profiles and exercise login, catalog and OpenAPI.
`SeedContentTest` checks the seed file itself: every puzzle valid and unique, one correct option per question, words upper-case.

## Diagrams

### Database

``` mermaid
erDiagram
    PROFILE {
        uuid id PK
        string name
        timestamptz created_at
        timestamptz updated_at
    }

    ROUTE {
        uuid id PK
        string name
        string url
        timestamptz created_at
        timestamptz updated_at
    }

    USER_PROFILE {
        uuid id PK
        uuid user_id FK
        uuid profile_id FK
        timestamptz created_at
        timestamptz updated_at
    }

    ROUTE_PROFILE {
        uuid id PK
        uuid route_id FK
        uuid profile_id FK
        timestamptz created_at
        timestamptz updated_at
    }

    USER {
        uuid id PK
        string name
        string email
        string password_hash
        string avatar_url
        timestamptz created_at
        timestamptz updated_at
    }

    GAME {
        uuid id PK
        string slug
        string title
        text description
        json metadata
        timestamptz created_at
        timestamptz updated_at
    }

    PLAY_SESSION {
        uuid id PK
        uuid user_id FK
        uuid game_id FK
        timestamptz started_at
        timestamptz ended_at
        json client_meta
        json result_summary
    }

    SCORE {
        uuid id PK
        uuid user_id FK
        uuid game_id FK
        uuid play_session_id FK
        integer score_value
        string rank_context
        timestamptz created_at
    }

    ASSET {
        uuid id PK
        uuid game_id FK
        string path
        string type
        json metadata
    }

    QUIZ {
        uuid id PK
        uuid game_id FK
        string title
        json settings
    }

    QUESTION {
        uuid id PK
        uuid quiz_id FK
        text text
        string type
        json metadata
    }

    OPTION {
        uuid id PK
        uuid question_id FK
        text text
        boolean is_correct
    }

    QUIZ_ATTEMPT {
        uuid id PK
        uuid user_id FK
        uuid quiz_id FK
        timestamptz started_at
        timestamptz finished_at
        integer score
        json metadata
    }

    QUIZ_ANSWER {
        uuid id PK
        uuid attempt_id FK
        uuid question_id FK
        uuid option_id FK
        text free_text_answer
        boolean correct
        integer time_taken_ms
    }

    CARD_TEMPLATE {
        uuid id PK
        uuid game_id FK
        string image_url
        string label
        json metadata
    }

    MEMORY_MOVE {
        uuid id PK
        uuid play_session_id FK
        uuid card_template_id FK
        integer position
        timestamptz timestamp
        boolean matched
    }

    TIC_TAC_TOE {
        uuid id PK
        uuid game_id FK
        uuid player_x_id FK
        uuid player_o_id FK
        string result
        timestamptz started_at
        timestamptz ended_at
    }

    TIC_TAC_TOE_MOVE {
        uuid id PK
        uuid match_id FK
        uuid player_id FK
        integer position_x
        integer position_y
        integer move_number
        timestamptz timestamp
    }

    SUDOKU {
        uuid id PK
        uuid game_id FK
        json initial_grid
        json solution_grid
        string difficulty
    }

    SUDOKU_ATTEMPT {
        uuid id PK
        uuid user_id FK
        uuid puzzle_id FK
        timestamptz started_at
        timestamptz finished_at
        boolean solved
        integer moves_count
        integer time_spent_seconds
    }

    WORD {
        uuid id PK
        string word
        string category
        string difficulty
    }

    HANGMAN_SESSION {
        uuid id PK
        uuid user_id FK
        uuid word_id FK
        timestamptz started_at
        timestamptz ended_at
        integer remaining_attempts
        string status
        string masked_word
        json guesses_summary
    }

    GUESS {
        uuid id PK
        uuid session_id FK
        string letter
        boolean correct
        timestamptz created_at
    }

    USER ||--o{ USER_PROFILE : ""
    PROFILE ||--o{ USER_PROFILE : ""
    ROUTE ||--o{ ROUTE_PROFILE : ""
    PROFILE ||--o{ ROUTE_PROFILE : ""

    USER ||--o{ HANGMAN_SESSION : "plays"
    WORD ||--o{ HANGMAN_SESSION : "selected_for"
    HANGMAN_SESSION ||--o{ GUESS : "records"

    USER ||--o{ PLAY_SESSION : "has"
    GAME ||--o{ PLAY_SESSION : "has"

    USER ||--o{ SCORE : "earns"
    GAME ||--o{ SCORE : "has"
    PLAY_SESSION ||--o{ SCORE : "may_produce"

    GAME ||--o{ ASSET : "owns"

    GAME ||--o{ QUIZ : "can_be"
    QUIZ ||--o{ QUESTION : "contains"
    QUESTION ||--o{ OPTION : "has"
    QUIZ ||--o{ QUIZ_ATTEMPT : "attempts"
    QUIZ_ATTEMPT ||--o{ QUIZ_ANSWER : "records"
    USER ||--o{ QUIZ_ATTEMPT : "makes"

    GAME ||--o{ CARD_TEMPLATE : "defines"
    CARD_TEMPLATE ||--o{ MEMORY_MOVE : "referenced_by"
    PLAY_SESSION ||--o{ MEMORY_MOVE : "records"

    GAME ||--o{ TIC_TAC_TOE : "hosts"
    TIC_TAC_TOE ||--o{ TIC_TAC_TOE_MOVE : "contains"
    USER ||--o{ TIC_TAC_TOE : "plays_in"

    GAME ||--o{ SUDOKU : "provides"
    SUDOKU ||--o{ SUDOKU_ATTEMPT : "attempted_by"
    USER ||--o{ SUDOKU_ATTEMPT : "makes"
```