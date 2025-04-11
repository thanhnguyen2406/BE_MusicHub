package MusicHub.service.interf;

import MusicHub.dto.AuthenticateDTO.AuthenticateDTO;
import MusicHub.dto.AuthenticateDTO.IntrospectDTO;
import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.UserDTO;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;

import java.text.ParseException;

public interface IAuthenticateService {
    ResponseAPI<Void> authenticate(AuthenticateDTO request, Boolean isGoogleLogin);
    ResponseAPI<Void> introspect(IntrospectDTO request) throws ParseException, JOSEException;
    ResponseAPI<String> generateAuthUrl(HttpServletRequest request, String login);
    ResponseAPI<Void> getAccessToken(String code, String state);
    ResponseAPI<Void> registerUser(UserDTO request);
}
