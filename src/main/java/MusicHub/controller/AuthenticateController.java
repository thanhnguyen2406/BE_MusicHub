package MusicHub.controller;

import MusicHub.dto.AuthenticateDTO.AuthenticateDTO;
import MusicHub.dto.AuthenticateDTO.IntrospectDTO;
import MusicHub.dto.AuthenticateDTO.TokenResponseDTO;
import MusicHub.dto.ResponseAPI;
import MusicHub.service.interf.IAuthenticateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticateController {
    private final IAuthenticateService authenticateService;

    @PostMapping("/login")
    public Mono<ResponseEntity<ResponseAPI<TokenResponseDTO>>> authenticate(@RequestBody Mono<AuthenticateDTO> requestMono) {
        return requestMono.flatMap(request ->
                authenticateService.authenticate(request)
                        .map(response -> ResponseEntity.status(response.getCode()).body(response)));
    }

    @PostMapping("/introspect")
    public Mono<ResponseEntity<ResponseAPI<IntrospectDTO>>> introspect(@RequestParam String token) {
        return authenticateService.introspect(token)
                .map(response -> ResponseEntity.status(response.getCode()).body(response));
    }
}
