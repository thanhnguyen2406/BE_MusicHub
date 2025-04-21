//package MusicHub.controller;
//
//import MusicHub.dto.AuthenticateDTO.AuthenticateDTO;
//import MusicHub.dto.AuthenticateDTO.IntrospectDTO;
//import MusicHub.dto.ResponseAPI;
//import MusicHub.dto.UserDTO.UserDTO;
//import MusicHub.service.interf.IAuthenticateService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Mono;
//
//@RestController
//@RequestMapping("/auth")
//@RequiredArgsConstructor
//public class AuthenticateController {
//    private final IAuthenticateService authenticateService;
//
//    @PostMapping("/login")
//    public Mono<ResponseEntity<ResponseAPI<Void>>> authenticate(@RequestBody Mono<AuthenticateDTO> requestMono) {
//        return requestMono.flatMap(request ->
//                authenticateService.authenticate(request, false)
//                        .map(response -> ResponseEntity.status(response.getCode()).body(response)));
//    }
//
//    @PostMapping("/introspect")
//    public Mono<ResponseEntity<ResponseAPI<Void>>> introspect(@RequestBody Mono<IntrospectDTO> requestMono) {
//        return requestMono.flatMap(request ->
//                authenticateService.introspect(request)
//                    .map(response -> ResponseEntity.status(response.getCode()).body(response)));
//    }
//
//    @GetMapping("/login-google")
//    public Mono<ResponseEntity<ResponseAPI<String>>> googleLogin(ServerHttpRequest request) {
//        return authenticateService.generateAuthUrl(request, "login")
//                .map(response -> ResponseEntity.status(response.getCode()).body(response));
//    }
//
//    @GetMapping("/callback-google")
//    public Mono<ResponseEntity<ResponseAPI<Void>>> googleCallback(
//            @RequestParam String code,
//            @RequestParam String state,
//            ServerHttpRequest request) {
//        return authenticateService.getAccessToken(code, state)
//                .map(response -> ResponseEntity.status(response.getCode()).body(response));
//    }
//
//    @PostMapping("/register")
//    public Mono<ResponseEntity<ResponseAPI<Void>>> registerUser(@RequestBody Mono<UserDTO> requestMono) {
//        return requestMono.flatMap(request ->
//                authenticateService.registerUser(request)
//                    .map(response -> ResponseEntity.status(response.getCode()).body(response)));
//    }
//
//    @GetMapping("/register-google")
//    public Mono<ResponseEntity<ResponseAPI<String>>> googleRegister(ServerHttpRequest request) {
//        return authenticateService.generateAuthUrl(request, "register")
//                .map(response -> ResponseEntity.status(response.getCode()).body(response));
//    }
//}
