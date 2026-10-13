# swe-mst-api

### Api for the system MST to the development of cognitive abilities 

---

#### Database diagram

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