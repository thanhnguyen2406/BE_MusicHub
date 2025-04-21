package MusicHub.controller;

import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.ResetPasswordDTO;
import MusicHub.dto.UserDTO.UserDTO;
import MusicHub.service.interf.IUserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @PostMapping("/add")
    public UserDTO createUser(@RequestBody UserDTO userDTO) {

        return userService.createUser(userDTO);
    }

    @GetMapping
    public UserRepresentation getUser(Principal principal) {
        return userService.getUserById(principal.getName());
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable String userId) {
        userService.deleteUserById(userId);
    }


    @PutMapping("/{userId}/send-verify-email")
    public void sendVerificationEmail(@PathVariable String userId) {
        userService.emailVerification(userId);
    }
    @PutMapping("/update-password")
    public void updatePassword(Principal principal) {
        userService.updatePassword(principal.getName());
    }
    @PutMapping("/change-password")
    public void updatePassword(@RequestBody ResetPasswordDTO request, Principal principal) {
        userService.updatePassword(request,principal.getName());
    }

//    @PreAuthorize("hasRole('USER')")
//    @GetMapping("/my-info")
//    public Mono<ResponseEntity<ResponseAPI<UserDTO>>> getMyInfo() {
//        return userService.getMyInfo()
//                .map(response -> ResponseEntity.status(response.getCode()).body(response));
//    }
}
