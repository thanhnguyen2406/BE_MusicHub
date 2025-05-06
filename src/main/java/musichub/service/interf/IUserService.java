package musichub.service.interf;

import musichub.dto.ResponseAPI;
import musichub.dto.UserDTO.ResetPasswordDTO;
import musichub.dto.UserDTO.UserDTO;
import org.keycloak.admin.client.resource.UserResource;
import reactor.core.publisher.Mono;

public interface IUserService {
    Mono<ResponseAPI<Void>> createUser(UserDTO userDTO);
    Mono<ResponseAPI<UserDTO>> getUserById(String userId);
    Mono<ResponseAPI<Void>> deleteUserById(String userId);
    Mono<ResponseAPI<Void>> emailVerification(String userId);
    UserResource getUserResource(String userId);
    Mono<ResponseAPI<Void>> updatePassword(String userId);
    Mono<ResponseAPI<Void>> updatePassword(ResetPasswordDTO resetPassword, String userId);
}
