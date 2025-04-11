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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    @GetMapping("/my-info")
    public ResponseEntity<ResponseAPI<UserDTO>> getMyInfo() {
        ResponseAPI<UserDTO> response = userService.getMyInfo();
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
