package br.com.desafio.app.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataSourceConfigTest {

    private final DataSourceConfig config = new DataSourceConfig();

    @Test
    void deveCriarDataSourceSemAutoCreate() {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        properties.setUsername("sa");
        properties.setPassword("");

        DataSource dataSource = config.dataSource(properties, false, "postgres");

        assertNotNull(dataSource);
    }

    @Test
    void deveIgnorarEnsureDatabaseParaJdbcNaoPostgres() {
        ReflectionTestUtils.invokeMethod(config, "ensureDatabaseExists", "jdbc:h2:mem:testdb", "sa", "", "postgres");
    }

    @Test
    void deveFalharAoFazerParseDeUrlInvalida() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(config, "parsePostgresUrl", "jdbc:postgresql://localhost"));

        assertTrueContains(ex.getMessage(), "URL JDBC PostgreSQL inválida");
    }

    @Test
    void deveFazerParseDeUrlPostgresValida() throws Exception {
        Object parsed = ReflectionTestUtils.invokeMethod(
                config,
                "parsePostgresUrl",
                "jdbc:postgresql://localhost:5432/desafio?sslmode=disable"
        );

        String hostPrefix = (String) parsed.getClass().getDeclaredMethod("hostPrefix").invoke(parsed);
        String databaseName = (String) parsed.getClass().getDeclaredMethod("databaseName").invoke(parsed);
        String querySuffix = (String) parsed.getClass().getDeclaredMethod("querySuffix").invoke(parsed);

        assertEquals("jdbc:postgresql://localhost:5432", hostPrefix);
        assertEquals("desafio", databaseName);
        assertEquals("?sslmode=disable", querySuffix);
    }

    @Test
    void deveFalharQuandoIdentificadorInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(config, "validateIdentifier", "db-invalido"));

        assertTrueContains(ex.getMessage(), "Identificador de banco inválido");
    }

    private void assertTrueContains(String text, String expectedPart) {
        org.junit.jupiter.api.Assertions.assertTrue(text.contains(expectedPart));
    }
}
