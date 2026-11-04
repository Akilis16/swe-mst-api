package com.project.mst.infra.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    private static final String DESCRIPTION = """
        API do sistema MST (Memória, Saúde e Tecnologia): contas, catálogo, sessões, pontuação e os cinco jogos.

        **Autenticação**: `POST /api/v1/auth/login` devolve um JWT; envie `Authorization: Bearer <token>` (botão *Authorize*).

        **Erros**: Problem Details (RFC 9457) com o campo `code` estável. Validação responde `422 VALIDATION_FAILED` com `errors: [{field, message}]`; \
        `401 UNAUTHENTICATED`, `403 FORBIDDEN`, `404 NOT_FOUND`, `409` para conflitos de regra (`SESSION_ALREADY_ENDED`, `TICTACTOE_CELL_OCCUPIED`, \
        `MEMORY_POSITION_ALREADY_MATCHED`, `QUIZ_QUESTION_ALREADY_ANSWERED`, `SUDOKU_CELL_IS_GIVEN`, `HANGMAN_LETTER_ALREADY_USED`, ...).

        **Partidas**: cada início de jogo abre uma `play_session`; a pontuação é calculada só no servidor ao encerrar e nunca é enviada pelo cliente. \
        Gabaritos (`isCorrect`, `solution`, palavra da forca) não aparecem nas respostas antes do fim da partida.

        **Pontuação**: Velha 100 vitória / 50 empate / 10 derrota contra o computador, 50 por partida local; \
        Memória 100 − 5 × erros; Quiz = acertos × 100 / total; Sudoku (100 − 20 × erros) × nível (1, 2, 3) se resolvido; \
        Forca 100 − 10 × erros na vitória. Revelar o Sudoku abandona a sessão sem pontuação.
        """;

    @Bean
    OpenAPI mstOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("MST API")
                .version("v1")
                .description(DESCRIPTION))
            .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
