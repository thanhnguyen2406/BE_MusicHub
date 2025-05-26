package musichub.configuration.rsocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rsocket.frame.decoder.PayloadDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
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
    public RSocketServerCustomizer rSocketServerCustomizer() {
        return server -> server
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                .fragment(1024 * 1024);
    }

    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        RSocketStrategies rSocketStrategies = RSocketStrategies.builder()
                .encoders(encoders -> encoders.add(new Jackson2JsonEncoder(objectMapper)))
                .decoders(decoders -> decoders.add(new Jackson2JsonDecoder(objectMapper)))
                .metadataExtractorRegistry(registry -> registry.metadataToExtract(
                        MimeTypeUtils.parseMimeType("message/x.rsocket.routing.v0"),
                        String.class,
                        "route"
                ))
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