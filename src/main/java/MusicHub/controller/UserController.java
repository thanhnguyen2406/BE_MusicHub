package MusicHub.controller;

import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.UserDTO;
import MusicHub.service.interf.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @GetMapping("/my-info")
    public Mono<ResponseEntity<ResponseAPI<UserDTO>>> getMyInfo(Mono<Principal> principalMono) {
        return principalMono.flatMap(principal ->
                userService.getMyInfo(principal.getName())
                        .map(response -> ResponseEntity.status(response.getCode()).body(response)));
    }
}
