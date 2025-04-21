package MusicHub.service.implement;

import MusicHub.configuration.KeycloakProperties;
import MusicHub.dto.AuthenticateDTO.AuthenticateDTO;
import MusicHub.dto.AuthenticateDTO.IntrospectDTO;
import MusicHub.dto.AuthenticateDTO.TokenResponseDTO;
import MusicHub.dto.ResponseAPI;
import MusicHub.service.interf.IAuthenticateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticateService implements IAuthenticateService {
    KeycloakProperties keycloak;
    WebClient webClient;

    public AuthenticateService(KeycloakProperties keycloak, WebClient.Builder builder) {
        this.keycloak = keycloak;
        this.webClient = builder.build();
    }

    public Mono<ResponseAPI<TokenResponseDTO>> authenticate(AuthenticateDTO request) {
        return webClient.post()
                .uri(keycloak.getUrls().getAuth())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("client_id", keycloak.getClientId())
                        .with("client_secret", keycloak.getClientSecret())
                        .with("grant_type", "password")
                        .with("username", request.getUsername())
                        .with("password", request.getPassword()))
                .retrieve()
                .bodyToMono(TokenResponseDTO.class)
                .map(response -> ResponseAPI.<TokenResponseDTO>builder()
                        .code(200)
                        .message("Authentication successful")
                        .data(response)
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<TokenResponseDTO>builder()
                        .code(500)
                        .message("Authentication failed: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<IntrospectDTO>> introspect(String token){
        return webClient.post()
                .uri(keycloak.getUrls().getAuth() + "/introspect")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("client_id", keycloak.getClientId())
                        .with("client_secret", keycloak.getClientSecret())
                        .with("token", token))
                .retrieve()
                .bodyToMono(IntrospectDTO.class)
                .map(response -> ResponseAPI.<IntrospectDTO>builder()
                        .code(200)
                        .message("Token introspected successfully")
                        .data(response)
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<IntrospectDTO>builder()
                        .code(500)
                        .message("Failed to introspect token: " + e.getMessage())
                        .build()));
    }
}
