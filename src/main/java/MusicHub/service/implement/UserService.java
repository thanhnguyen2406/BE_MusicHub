package MusicHub.service.implement;

import MusicHub.configuration.KeycloakProperties;
import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.ResetPasswordDTO;
import MusicHub.dto.UserDTO.UserDTO;
import MusicHub.exception.AppException;
import MusicHub.exception.ErrorCode;
import MusicHub.mapper.UserMapper;
import MusicHub.model.User;
import MusicHub.repository.UserRepository;
import MusicHub.service.interf.IUserService;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserService implements IUserService {
    Keycloak keycloak;
    KeycloakProperties keycloakProperties;
    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    public Mono<ResponseAPI<UserDTO>> getUserById(String userId) {
        return Mono.fromCallable(() ->
                        getUsersResource().get(userId).toRepresentation())
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .map(userRepresentation -> ResponseAPI.<UserDTO>builder()
                        .code(200)
                        .data(userMapper.toUserDTO(userRepresentation))
                        .message("User fetched successfully")
                        .build())
                .onErrorResume(AppException.class, e -> Mono.just(
                        ResponseAPI.<UserDTO>builder()
                                .code(e.getErrorCode().getCode())
                                .message(e.getErrorCode().getMessage())
                                .build()))
                .onErrorResume(e -> Mono.just(
                        ResponseAPI.<UserDTO>builder()
                                .code(500)
                                .message("Error Occurs During Fetching User: " + e.getMessage())
                                .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> createUser(UserDTO userDTO) {
        return Mono.fromCallable(() -> {
                    UsersResource usersResource = getUsersResource();

                    List<UserRepresentation> existingUsers = usersResource.search(userDTO.getUsername(), true);
                    if (!existingUsers.isEmpty()) {
                        throw new AppException(ErrorCode.USER_EXISTED);
                    }

                    List<UserRepresentation> usersByEmail = usersResource.search(null, null, null, userDTO.getEmail(), null, null);
                    if (!usersByEmail.isEmpty()) {
                        throw new AppException(ErrorCode.USER_EXISTED);
                    }

                    UserRepresentation user = userMapper.toUserRepresentation(userDTO);
                    user.setEnabled(true);
                    user.setEmailVerified(true);

                    CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
                    credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
                    credentialRepresentation.setValue(userDTO.getPassword());
                    credentialRepresentation.setTemporary(false);
                    user.setCredentials(List.of(credentialRepresentation));

                    Response response = usersResource.create(user);
                    if (response.getStatus() != 201) {
                        throw new AppException(ErrorCode.KEYCLOAK_FAIL);
                    }

                    URI location = response.getLocation();
                    String path = location.getPath();
                    String userId = path.substring(path.lastIndexOf("/") + 1);

                    User dbUser = new User();
                    dbUser.setId(userId);
                    dbUser.setDisplayName(userDTO.getFirstName() + " " + userDTO.getLastName());
                    dbUser.setJoinedAt(LocalDateTime.now());
                    dbUser.setAvatar(null);

                    return dbUser;
                })
                .flatMap(userRepository::save)
                .thenReturn(ResponseAPI.<Void>builder()
                        .code(200)
                        .message("User created successfully")
                        .build())
                .onErrorResume(AppException.class, e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(e.getErrorCode().getCode())
                                .message(e.getErrorCode().getMessage())
                                .build()))
                .onErrorResume(e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(500)
                                .message("Error Occurs During Creating User: " + e.getMessage())
                                .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> deleteUserById(String userId) {
        return Mono.fromCallable(() -> getUsersResource().get(userId).toRepresentation())
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user ->
                        Mono.fromRunnable(() ->
                                getUsersResource().delete(userId)))
                .then(userRepository.deleteById(userId))
                .thenReturn(ResponseAPI.<Void>builder()
                        .code(200)
                        .message("User deleted successfully")
                        .build())
                .onErrorResume(AppException.class, e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(e.getErrorCode().getCode())
                                .message(e.getErrorCode().getMessage())
                                .build()))
                .onErrorResume(e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(500)
                                .message("Error Occurs During Deleting User: " + e.getMessage())
                                .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> emailVerification(String userId) {
        return Mono.fromCallable(() -> {
                    UserResource userResource = getUsersResource().get(userId);
                    UserRepresentation userRepresentation = userResource.toRepresentation();
                    if (userRepresentation == null) {
                        throw new AppException(ErrorCode.USER_NOT_FOUND);
                    }
                    return userResource;
                })
                .flatMap(userResource -> {
                    List<String> actions = List.of("UPDATE_EMAIL");
                    userResource.executeActionsEmail(actions);
                    return Mono.just(ResponseAPI.<Void>builder()
                            .code(200)
                            .message("Verification email sent successfully")
                            .build());
                })
                .onErrorResume(AppException.class, e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(e.getErrorCode().getCode())
                                .message(e.getErrorCode().getMessage())
                                .build()))
                .onErrorResume(e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(500)
                                .message("Error Occurs During Sending Verification Email: " + e.getMessage())
                                .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> updatePassword(String userId) {
        return Mono.fromCallable(() -> {
                    UserResource userResource = getUsersResource().get(userId);
                    UserRepresentation userRepresentation = userResource.toRepresentation();
                    if (userRepresentation == null) {
                        throw new AppException(ErrorCode.USER_NOT_FOUND);
                    }
                    return userResource;
                })
                .flatMap(userResource -> {
                    List<String> actions = List.of("UPDATE_PASSWORD");
                    userResource.executeActionsEmail(actions);
                    return Mono.just(ResponseAPI.<Void>builder()
                            .code(200)
                            .message("Password update email sent successfully")
                            .build());
                })
                .onErrorResume(AppException.class, e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(e.getErrorCode().getCode())
                                .message(e.getErrorCode().getMessage())
                                .build()))
                .onErrorResume(e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(500)
                                .message("Error Occurs During Sending Password Update Email: " + e.getMessage())
                                .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> updatePassword(ResetPasswordDTO resetPassword, String userId) {
        return Mono.fromCallable(() -> {
                    if (resetPassword.getPassword() == null || resetPassword.getPassword().isEmpty()) {
                        throw new AppException(ErrorCode.PASSWORD_IS_EMPTY);
                    }
                    UserResource userResource = getUsersResource().get(userId);
                    userResource.toRepresentation();

                    CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
                    credentialRepresentation.setValue(resetPassword.getPassword());
                    credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
                    credentialRepresentation.setTemporary(false);

                    userResource.resetPassword(credentialRepresentation);

                    return ResponseAPI.<Void>builder()
                            .code(200)
                            .message("Password updated successfully")
                            .build();
                })
                .onErrorResume(AppException.class, e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(e.getErrorCode().getCode())
                                .message(e.getErrorCode().getMessage())
                                .build()))
                .onErrorResume(e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(500)
                                .message("Error Occurs During Updating Password: " + e.getMessage())
                                .build()));
    }

    private UsersResource getUsersResource() {
        RealmResource realm1 = keycloak.realm(keycloakProperties.getRealm());
        return realm1.users();
    }

    public UserResource getUserResource(String userId){
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }
}
