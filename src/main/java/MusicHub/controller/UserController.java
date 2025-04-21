package MusicHub.controller;

import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.ResetPasswordDTO;
import MusicHub.dto.UserDTO.UserDTO;
import MusicHub.model.User;
import MusicHub.service.interf.IUserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @GetMapping("/my-info")
    public Mono<ResponseEntity<ResponseAPI<UserDTO>>> getMyInfo(Principal principal) {
        return userService.getUserById(principal.getName())
                .map(response -> ResponseEntity.status(response.getCode()).body(response));
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<ResponseAPI<Void>>> createUser(@RequestBody  Mono<UserDTO> requestMono) {
        return requestMono.flatMap(request ->
                userService.createUser(request)
                        .map(response -> ResponseEntity.status(response.getCode()).body(response)));
    }

    @DeleteMapping("/delete/{userId}")
    public Mono<ResponseEntity<ResponseAPI<Void>>> deleteUserById(@PathVariable String userId) {
        return userService.deleteUserById(userId)
                .map(response -> ResponseEntity.status(response.getCode()).body(response));
    }

    @PutMapping("/{userId}/send-verify-email")
    public Mono<ResponseEntity<ResponseAPI<Void>>> sendVerificationEmail(@PathVariable String userId) {
        return userService.emailVerification(userId)
                .map(response -> ResponseEntity.status(response.getCode()).body(response));
    }

    @PutMapping("/update-password")
    public void updatePassword(Principal principal) {
        userService.updatePassword(principal.getName());
    }

    @PutMapping("/change-password")
    public void updatePassword(@RequestBody ResetPasswordDTO request, Principal principal) {
        userService.updatePassword(request,principal.getName());
    }
}
