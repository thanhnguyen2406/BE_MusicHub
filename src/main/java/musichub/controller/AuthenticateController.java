package musichub.controller;

import musichub.dto.AuthenticateDTO.AuthenticateDTO;
import musichub.dto.AuthenticateDTO.IntrospectDTO;
import musichub.dto.AuthenticateDTO.TokenResponseDTO;
import musichub.dto.ResponseAPI;
import musichub.service.interf.IAuthenticateService;
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
