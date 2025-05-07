package musichub.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
    private String domain;
    private String clientId;
    private String clientSecret;
    private String realm;
    private Urls urls;

    @Data
    public static class Urls {
        private String auth;
    }
}
