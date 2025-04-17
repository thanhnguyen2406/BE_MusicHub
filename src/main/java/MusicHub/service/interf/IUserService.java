package MusicHub.service.interf;

import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.UserDTO;
import reactor.core.publisher.Mono;

public interface IUserService {
    Mono<ResponseAPI<Void>> createPatient(String email, String name);
    Mono<ResponseAPI<UserDTO>> getMyInfo(String email);
}
