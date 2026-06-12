package br.com.desafio.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class DataSourceConfig {

    private static final Pattern POSTGRES_URL_PATTERN =
            Pattern.compile("^(jdbc:postgresql://[^/]+)/([^?]+)(\\?.*)?$");

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties,
                                 @Value("${app.database.auto-create:true}") boolean autoCreateDatabase,
                                 @Value("${app.database.admin-database:postgres}") String adminDatabase) {
        String url = properties.getUrl();
        String username = properties.determineUsername();
        String password = properties.determinePassword();

        if (autoCreateDatabase) {
            ensureDatabaseExists(url, username, password, adminDatabase);
        }

        return properties.initializeDataSourceBuilder().build();
    }

    private void ensureDatabaseExists(String jdbcUrl,
                                      String username,
                                      String password,
                                      String adminDatabase) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:postgresql://")) {
            return;
        }

        ParsedPostgresUrl parsedUrl = parsePostgresUrl(jdbcUrl);
        validateIdentifier(parsedUrl.databaseName());
        validateIdentifier(adminDatabase);

        String adminJdbcUrl = parsedUrl.hostPrefix() + "/" + adminDatabase + parsedUrl.querySuffix();

        try (Connection connection = DriverManager.getConnection(adminJdbcUrl, username, password)) {
            if (databaseAlreadyExists(connection, parsedUrl.databaseName())) {
                return;
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE \"" + parsedUrl.databaseName() + "\"");
            }
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Não foi possível criar automaticamente o banco de dados: " + parsedUrl.databaseName(), ex);
        }
    }

    private boolean databaseAlreadyExists(Connection connection, String databaseName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM pg_database WHERE datname = ?")) {
            statement.setString(1, databaseName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private ParsedPostgresUrl parsePostgresUrl(String jdbcUrl) {
        Matcher matcher = POSTGRES_URL_PATTERN.matcher(jdbcUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("URL JDBC PostgreSQL inválida: " + jdbcUrl);
        }

        String hostPrefix = matcher.group(1);
        String databaseName = matcher.group(2);
        String querySuffix = matcher.group(3) == null ? "" : matcher.group(3);

        return new ParsedPostgresUrl(hostPrefix, databaseName, querySuffix);
    }

    private void validateIdentifier(String value) {
        if (value == null || !value.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Identificador de banco inválido: " + value);
        }
    }

    private record ParsedPostgresUrl(String hostPrefix, String databaseName, String querySuffix) {
    }
}
