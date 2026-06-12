package br.com.desafio.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
	String securitySchemeName = "basicAuth";

	return new OpenAPI()
		.info(new Info()
			.title("API de Portfólio de Projetos")
			.version("1.0.0")
			.description("API para gestão de projetos, membros e relatórios do portfólio."))
		.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
		.schemaRequirement(securitySchemeName,
			new SecurityScheme()
				.name(securitySchemeName)
				.type(SecurityScheme.Type.HTTP)
				.scheme("basic"));
    }
}
