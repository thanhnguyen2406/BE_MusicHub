package musichub.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class ClientRsocketConfig {
    private final RsocketProperties rsocket;
    private final ObjectMapper objectMapper;

    @Bean
    public RSocketRequester getRSocketRequester() {
        RSocketRequester.Builder builder = RSocketRequester.builder();

        RSocketStrategies rSocketStrategies = RSocketStrategies.builder()
                .encoders(encoders -> encoders.add(new Jackson2JsonEncoder(objectMapper)))
                .decoders(decoders -> decoders.add(new Jackson2JsonDecoder(objectMapper)))
                .build();

        return builder
                .rsocketStrategies(rSocketStrategies)
                .rsocketConnector(connector ->
                        connector.reconnect(Retry.fixedDelay(2, Duration.ofSeconds(2)))
                )
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .websocket(URI.create("ws://localhost:" + rsocket.getPort() + rsocket.getMappingPath()));
    }
}