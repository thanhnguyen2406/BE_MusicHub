package musichub.configuration.openAPI;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OpenAPIConfig {
    OpenAPIProperties properties;

    @Bean
    public OpenAPI openAPI() {
        OpenAPIProperties.Info info = properties.getInfo();
        OpenAPIProperties.Contact contact = info.getContact();
        OpenAPIProperties.Server devServer = properties.getServers()[0];
        OpenAPIProperties.Server prodServer = properties.getServers()[1];

        return new OpenAPI()
                .info(new Info()
                        .title(info.getTitle())
                        .description(info.getDescription())
                        .version(info.getVersion())
                        .contact(new Contact()
                                .name(contact.getName())
                                .email(contact.getEmail())))
                .servers(List.of(
                        new Server()
                                .url(devServer.getUrl())
                                .description(devServer.getDescription()),
                        new Server()
                                .url(prodServer.getUrl())
                                .description(prodServer.getDescription())
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
