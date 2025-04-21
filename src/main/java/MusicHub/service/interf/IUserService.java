package MusicHub.service.interf;

import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.ResetPasswordDTO;
import MusicHub.dto.UserDTO.UserDTO;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import reactor.core.publisher.Mono;

public interface IUserService {
//    Mono<ResponseAPI<Void>> createUser(String email, String name);
//    Mono<ResponseAPI<UserDTO>> getMyInfo();
    UserDTO createUser(UserDTO userDTO);
    UserRepresentation getUserById(String userId);
    void deleteUserById(String userId);
    void emailVerification(String userId);
    UserResource getUserResource(String userId);
    void updatePassword(String userId);
    void updatePassword(ResetPasswordDTO resetPassword, String userId);
}
