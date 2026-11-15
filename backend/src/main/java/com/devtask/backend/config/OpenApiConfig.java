
package com.devtask.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI devTaskOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DevTask - API REST")
                        .version("1.0.0")
                        .description(
                                "API REST para gerenciamento de tarefas. "
                                + "Permite cadastrar, consultar, atualizar "
                                + "e excluir tarefas, além de realizar "
                                + "consultas com paginação, filtros por "
                                + "status e prioridade e ordenação. "
                                + "Desenvolvida com Java 17, Spring Boot, "
                                + "Spring Data JPA e PostgreSQL."
                        ));
    }
}
