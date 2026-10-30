package com.project.mst.infra.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.project.mst.module.sudoku.SudokuValidator;

import org.junit.jupiter.api.Test;

class SeedContentTest {

    static final Path SEED = Path.of("src/main/resources/db/migration/V3__seed_content.sql");

    @Test
    void todosOsPuzzlesDoSeedSaoValidosComSolucaoUnica() throws IOException {
        Matcher m = Pattern.compile("'(EASY|MEDIUM|HARD)', '([0-9]{81})', '([1-9]{81})'").matcher(read());
        int count = 0;
        while (m.find()) {
            count++;
            assertThat(SudokuValidator.problems(m.group(2), m.group(3)))
                .as("puzzle %d (%s)", count, m.group(1)).isEmpty();
        }
        assertThat(count).isEqualTo(15);
    }

    @Test
    void cadaPerguntaTemQuatroAlternativasEUmaCorreta() throws IOException {
        String sql = read();
        String options = sql.substring(sql.indexOf("insert into option"), sql.indexOf("insert into word"));
        Matcher m = Pattern.compile("\\('[0-9a-f-]{36}', '([0-9a-f-]{36})', '(?:[^']|'')*', (true|false),").matcher(options);
        Map<String, int[]> perQuestion = new HashMap<>();
        while (m.find()) {
            int[] counts = perQuestion.computeIfAbsent(m.group(1), k -> new int[2]);
            counts[0]++;
            if ("true".equals(m.group(2))) {
                counts[1]++;
            }
        }
        assertThat(perQuestion).hasSize(28);
        assertThat(perQuestion.values()).allMatch(c -> c[0] == 4 && c[1] == 1);
    }

    @Test
    void palavrasDaForcaSaoMaiusculasSemAcento() throws IOException {
        String sql = read();
        String words = sql.substring(sql.indexOf("insert into word"), sql.indexOf("insert into sudoku_puzzle"));
        Matcher m = Pattern.compile("\\('[0-9a-f-]{36}', '([^']+)', '([^']+)'").matcher(words);
        int count = 0;
        while (m.find()) {
            count++;
            assertThat(m.group(1)).matches("^[A-Z]+$");
        }
        assertThat(count).isEqualTo(40);
    }

    private static String read() throws IOException {
        return Files.readString(SEED, StandardCharsets.UTF_8);
    }
}
