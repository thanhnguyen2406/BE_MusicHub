package MusicHub.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String issuerUri;
    private String jwkSetUri;
    private Auth auth;
    private int expiration;

    @Data
    public static class Auth {
        private Converter converter;

        @Data
        public static class Converter {
            private String resourceId;
            private String principleAttribute;
        }
    }
}
