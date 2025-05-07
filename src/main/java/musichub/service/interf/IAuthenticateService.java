package musichub.service.interf;

import musichub.dto.AuthenticateDTO.AuthenticateDTO;
import musichub.dto.AuthenticateDTO.IntrospectDTO;
import musichub.dto.AuthenticateDTO.TokenResponseDTO;
import musichub.dto.ResponseAPI;
import reactor.core.publisher.Mono;

public interface IAuthenticateService {
    Mono<ResponseAPI<TokenResponseDTO>> authenticate(AuthenticateDTO request);
    Mono<ResponseAPI<IntrospectDTO>> introspect(String token);
}
