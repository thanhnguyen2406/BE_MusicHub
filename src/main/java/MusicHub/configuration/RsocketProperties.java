package MusicHub.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.rsocket.server")
public class RsocketProperties {
    private String port;
    private String mappingPath;
    private String transport;
}
