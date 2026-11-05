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

- OpenAPI generated from the code: `GET /v3/api-docs` (JSON) and `/swagger-ui.html`, both public. Endpoints are grouped by tag
  (Autenticação, Conta, Catálogo, Sessões, Progresso, Jogo da Velha, Jogo da Memória, Quiz, Sudoku, Forca, Conteúdo (ADMIN)) with a summary,
  the error codes each one can return and the score formulas; use *Authorize* with the token from `/auth/login` to call them from the UI.

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

### CORS

CORS is off by default: the API sends no `Access-Control-*` headers and a preflight `OPTIONS` gets `401`
like any other unauthenticated call. Browsers therefore block pages served from another origin, which is the
intended production setup, where nginx serves the web app and proxies `/api` on the same origin. For local
development with the Angular app on another port, list the allowed origins in `.env`:

```
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://127.0.0.1:4200
```

Listed origins get preflight answers without a token, the `GET, POST, PUT, PATCH, DELETE, OPTIONS` methods and
the `Authorization` and `Content-Type` headers; any other origin receives `403`. Tools that do not run in a
browser (curl, Postman, Swagger UI on the same host) are not affected either way.

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
Each game has its own start endpoint that opens the session; `POST /play-sessions` exists for games without one.

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/play-sessions` | Starts a generic session: `{"gameSlug": "velha", "difficulty": "EASY", "timed": true, "clientMeta": "<json>"}`. `timed` defaults to the user's preference. |
| `POST` | `/api/v1/play-sessions/{id}/finish` | Ends the session; the game's `ScoreCalculator` computes the score. Repeating the call returns the same state. |
| `POST` | `/api/v1/play-sessions/{id}/abandon` | Marks the session as abandoned; `409` if already ended. |
| `GET` | `/api/v1/me/play-sessions?game=&page=&size=` | Own sessions, newest first, each with its `score` (`value`, `breakdown`). |
| `GET` | `/api/v1/me/progress?game=&from=&to=` | Own progress series per game and day: sessions, finished, abandoned, average duration, average score, errors and correct answers (from the score breakdown). Default period: last 30 days, max 366. |
| `GET` | `/api/v1/play-sessions/{id}` | One session, owner or ADMIN. |
| `GET` | `/api/v1/play-sessions?game=&page=&size=` | All sessions, ADMIN only. |

Score formulas (a session holds at most one score, written only by the server):

| Game | Score | Breakdown keys |
|---|---|---|
| `velha` | vs computer: 100 win, 50 draw, 10 loss; local 2 players: 50 per finished match | `mode`, `result`, `moves`, `seconds` |
| `memoria` | `100 - 5 × errors`, minimum 0 | `pairs`, `errors`, `seconds` |
| `quiz` | `floor(correct × 100 / total)` (RN-13 performance) | `correct`, `errors`, `total`, `performance`, `message`, `seconds` |
| `sudoku` | solved: `(100 - 20 × mistakes) × level` (EASY 1, MEDIUM 2, HARD 3); otherwise 0 | `solved`, `difficulty`, `errors`, `timeLimitSeconds`, `seconds` |
| `forca` | win: `100 - 10 × errors`; loss: 0 | `result`, `errors`, `wordLength`, `category`, `seconds` |

### Games

All game endpoints require a player token, open a play session on start and close it on the server when the match
ends. Answer keys never leave the server before the end of the match.

| Game | Endpoints | Rules enforced server-side |
|---|---|---|
| Tic-tac-toe | `POST /tic-tac-toe/matches` `{mode: LOCAL_2P \| VS_COMPUTER, timed?}` · `GET /tic-tac-toe/matches/{id}` · `POST /tic-tac-toe/matches/{id}/moves` `{row, col}` | X starts; marks alternate; 8 winning lines; draw with 9 cells; in `VS_COMPUTER` the server plays O on a random empty cell in the same response. `409 TICTACTOE_CELL_OCCUPIED`, `TICTACTOE_NOT_YOUR_TURN`, `TICTACTOE_MATCH_FINISHED`. |
| Memory | `GET /memory/themes` · `POST /memory/rounds` `{themeId, timed?}` · `GET /memory/rounds/{id}` · `POST /memory/rounds/{id}/moves` `{position}` | 9 pairs shuffled into 18 positions stored per round; cards are identified only when flipped; the second card of each try closes the pair or counts an error; the last pair ends the session. `409 MEMORY_POSITION_ALREADY_MATCHED`, `MEMORY_POSITION_ALREADY_REVEALED`, `MEMORY_ROUND_FINISHED`; `422 MEMORY_POSITION_INVALID`. |
| Quiz | `POST /quiz/attempts` `{quizId?, timed?}` · `GET /quiz/attempts/{id}` · `GET /quiz/attempts/{id}/questions/next` · `POST /quiz/attempts/{id}/answers` `{questionId, optionId}` · `POST /quiz/attempts/{id}/finish` | Questions in `position` order, options without `isCorrect`; one answer per question, the correct option is returned after answering; finishing requires every question answered. `404 QUIZ_NO_MORE_QUESTIONS`, `409 QUIZ_QUESTION_ALREADY_ANSWERED`, `QUIZ_QUESTIONS_PENDING`, `QUIZ_ATTEMPT_FINISHED`; `422 QUIZ_OPTION_INVALID`. |
| Sudoku | `POST /sudoku/attempts` `{difficulty, timeLimitSeconds?: 180 \| 300 \| 600}` · `GET /sudoku/attempts/{id}` · `POST /sudoku/attempts/{id}/entries` `{cellIdx, value?}` · `POST /sudoku/attempts/{id}/reveal` | Random active puzzle of the level; givens only, never the solution; a wrong value counts a mistake and is erased; 3 mistakes = `LOSS`; 81 correct cells = `WIN`; an expired time limit ends the attempt as `TIMEOUT` (session abandoned); reveal returns the solution and abandons the session without score. `409 SUDOKU_CELL_IS_GIVEN`, `SUDOKU_ATTEMPT_FINISHED`; `422 SUDOKU_TIME_LIMIT_INVALID`. |
| Hangman | `GET /hangman/categories` · `POST /hangman/rounds` `{category, timed?}` · `GET /hangman/rounds/{id}` · `POST /hangman/rounds/{id}/guesses` `{letter}` | Random active word of the category; mask computed on the server; letters A–Z only, no repeats; 6 errors = `LOSS`, all letters = `WIN`; the word is returned only when finished. `409 HANGMAN_LETTER_ALREADY_USED`, `HANGMAN_ROUND_FINISHED`; `422 HANGMAN_LETTER_INVALID`. |

The 2048 game was removed from the catalog (BL-036, Q-05): the legacy implementation was unpublished, unstyled and unrelated to the system's purpose.

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
| `V4__gameplay.sql` | `question.position` (order of the legacy file) and `memory_round.layout` (shuffled card ids per round) |
| `V5__remove_2048.sql` | Removes the 2048 game |

Sudoku grids are stored row by row as 81 digits, `0` meaning an empty cell.
A database created before Flyway was introduced must be recreated (`docker compose down -v`).

## Tests

`./mvnw test` needs no external database: unit and web-slice tests run with mocks, and
`MstApplicationTests` starts an embedded PostgreSQL 16 (downloaded from Maven Central on first run)
to apply the migrations, validate the schema, bootstrap the profiles and exercise login, catalog, OpenAPI and a
full round of each game through the HTTP API (no answer key in any player response).
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