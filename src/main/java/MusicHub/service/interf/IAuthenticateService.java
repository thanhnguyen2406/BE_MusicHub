package MusicHub.service.interf;

import MusicHub.dto.AuthenticateDTO.AuthenticateDTO;
import MusicHub.dto.AuthenticateDTO.IntrospectDTO;
import MusicHub.dto.AuthenticateDTO.TokenResponseDTO;
import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.UserDTO;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.text.ParseException;

public interface IAuthenticateService {
    Mono<ResponseAPI<TokenResponseDTO>> authenticate(AuthenticateDTO request);
    Mono<ResponseAPI<IntrospectDTO>> introspect(String token);
}
