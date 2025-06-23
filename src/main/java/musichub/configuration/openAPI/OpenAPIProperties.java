package musichub.configuration.openAPI;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "openapi")
public class OpenAPIProperties {
    private Info info;
    private Server[] servers;

    @Data
    public static class Info {
        private String title;
        private String description;
        private String version;
        private Contact contact;
    }

    @Data
    public static class Contact {
        private String name;
        private String email;
    }

    @Data
    public static class Server {
        private String url;
        private String description;
    }
}
