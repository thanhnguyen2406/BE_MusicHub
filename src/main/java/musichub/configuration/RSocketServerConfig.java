package musichub.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;

@Configuration
public class RSocketServerConfig {

    @Bean
    public RSocketMessageHandler messageHandler() {
        return new RSocketMessageHandler();
    }
}
