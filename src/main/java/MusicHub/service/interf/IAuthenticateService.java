package MusicHub.service.interf;

import MusicHub.dto.AuthenticateDTO.AuthenticateDTO;
import MusicHub.dto.AuthenticateDTO.IntrospectDTO;
import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.UserDTO;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.text.ParseException;

public interface IAuthenticateService {
    Mono<ResponseAPI<Void>> authenticate(AuthenticateDTO request, Boolean isGoogleLogin);
    Mono<ResponseAPI<Void>> introspect(IntrospectDTO request);
    Mono<ResponseAPI<String>> generateAuthUrl(ServerHttpRequest request, String state);
    Mono<ResponseAPI<Void>> getAccessToken(String code, String state);
    Mono<ResponseAPI<Void>> registerUser(UserDTO request);
}
