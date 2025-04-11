package MusicHub.controller;

import MusicHub.dto.AuthenticateDTO.AuthenticateDTO;
import MusicHub.dto.AuthenticateDTO.IntrospectDTO;
import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.UserDTO;
import MusicHub.service.interf.IAuthenticateService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticateController {
    private final IAuthenticateService authenticateService;

    @PostMapping("/login")
    public ResponseEntity<ResponseAPI<Void>> authenticate(@RequestBody AuthenticateDTO request) {
        ResponseAPI<Void> response = authenticateService.authenticate(request, false);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/introspect")
    public ResponseEntity<ResponseAPI<Void>> introspect(@RequestBody IntrospectDTO request) throws ParseException, JOSEException {
        ResponseAPI<Void> response = authenticateService.introspect(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/login-google")
    public ResponseEntity<ResponseAPI<String>> googleLogin(HttpServletRequest request) {
        ResponseAPI<String> response = authenticateService.generateAuthUrl(request, "login");
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/callback-google")
    public ResponseEntity<ResponseAPI<Void>> googleCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest request) {
        ResponseAPI<Void> response = authenticateService.getAccessToken(code, state);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/register")
    ResponseEntity<ResponseAPI<Void>> registerUser(@RequestBody UserDTO request) {
        ResponseAPI<Void> response = authenticateService.registerUser(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/register-google")
    ResponseEntity<ResponseAPI<String>> googleRegister(HttpServletRequest request) {
        ResponseAPI<String> response = authenticateService.generateAuthUrl(request, "register");
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
