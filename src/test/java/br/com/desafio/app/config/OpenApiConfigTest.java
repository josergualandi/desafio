package br.com.desafio.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiConfigTest {

    @Test
    void deveCriarDefinicaoOpenApiComBasicAuth() {
        OpenApiConfig config = new OpenApiConfig();

        OpenAPI openAPI = config.openAPI();

        assertEquals("API de Portfólio de Projetos", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("basicAuth"));
        assertEquals("basic", openAPI.getComponents().getSecuritySchemes().get("basicAuth").getScheme());
    }
}
