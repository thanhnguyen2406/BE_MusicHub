package musichub.service.impl;

import musichub.common.ResponseUtil;
import musichub.configuration.security.KeycloakProperties;
import musichub.dto.AuthenticateDTO.AuthenticateDTO;
import musichub.dto.AuthenticateDTO.IntrospectDTO;
import musichub.dto.AuthenticateDTO.TokenResponseDTO;
import musichub.dto.ResponseAPI;
import musichub.service.AuthenticateService;
import lombok.AccessLevel;
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
public class AuthenticateServiceImpl implements AuthenticateService {
    KeycloakProperties keycloak;
    WebClient webClient;

    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String GRANT_TYPE = "grant_type";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String INTROSPECT = "/introspect";
    private static final String TOKEN = "token";

    public AuthenticateServiceImpl(KeycloakProperties keycloak, WebClient.Builder builder) {
        this.keycloak = keycloak;
        this.webClient = builder.build();
    }

    public Mono<ResponseAPI<TokenResponseDTO>> authenticate(AuthenticateDTO request) {
        return webClient.post()
                .uri(keycloak.getUrls().getAuth())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData(CLIENT_ID, keycloak.getClientId())
                        .with(CLIENT_SECRET, keycloak.getClientSecret())
                        .with(GRANT_TYPE, PASSWORD)
                        .with(USERNAME, request.getEmail())
                        .with(PASSWORD, request.getPassword()))
                .retrieve()
                .bodyToMono(TokenResponseDTO.class)
                .map(response -> ResponseUtil.success(response, "Authenticated successfully"));
    }

    @Override
    public Mono<ResponseAPI<IntrospectDTO>> introspect(String token){
        return webClient.post()
                .uri(keycloak.getUrls().getAuth() + INTROSPECT)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData(CLIENT_ID, keycloak.getClientId())
                        .with(CLIENT_SECRET, keycloak.getClientSecret())
                        .with(TOKEN, token))
                .retrieve()
                .bodyToMono(IntrospectDTO.class)
                .map(response -> ResponseUtil.success(response, "Token introspected successfully"));
    }
}
