package musichub.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class OauthGoogleProperties {

    private Registration registration;
    private Provider provider;

    @Data
    public static class Registration {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }

    @Data
    public static class Provider {
        private String authorizationUri;
        private String tokenUri;
        private String userInfoUri;
    }
}