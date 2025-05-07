package musichub.service.implement;

import musichub.configuration.KeycloakProperties;
import musichub.dto.ResponseAPI;
import musichub.dto.UserDTO.ResetPasswordDTO;
import musichub.dto.UserDTO.UserDTO;
import musichub.exception.AppException;
import musichub.exception.ErrorCode;
import musichub.mapper.UserMapper;
import musichub.model.User;
import musichub.repository.UserRepository;
import musichub.service.interf.IUserService;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserService implements IUserService {
    Keycloak keycloak;
    KeycloakProperties keycloakProperties;
    UserRepository userRepository;
    UserMapper userMapper;

    static String USER_ROLE = "musicHub_user";

    @Override
    public Mono<ResponseAPI<UserDTO>> getUserById(String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(existingUser -> {
                    UserRepresentation userRepresentation = getUsersResource().get(userId).toRepresentation();
                    UserDTO userDTO = userMapper.toUserDTO(userRepresentation, existingUser);
                    return Mono.just(ResponseAPI.<UserDTO>builder()
                            .code(200)
                            .data(userDTO)
                            .message("User fetched successfully")
                            .build());
                })
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

                    List<UserRepresentation> existingUsers = usersResource.search(userDTO.getEmail(), true);
                    if (!existingUsers.isEmpty()) {
                        throw new AppException(ErrorCode.USER_EXISTED);
                    }

                    List<UserRepresentation> usersByEmail = usersResource.search(null, null, null, userDTO.getEmail(), null, null);
                    if (!usersByEmail.isEmpty()) {
                        throw new AppException(ErrorCode.USER_EXISTED);
                    }

                    UserRepresentation user = userMapper.toUserRepresentation(userDTO);

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

                    User dbUser = buildDbUser(userId, userDTO.getDisplayName());
                    assignUserRole(userId);
                    
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

    private String getClientUUID() {
        return keycloak.realm(keycloakProperties.getRealm())
                .clients()
                .findByClientId(keycloakProperties.getClientId())
                .get(0)
                .getId();
    }

    private void assignUserRole(String userId) {
        String clientUUID = getClientUUID();

        RoleRepresentation userRole = keycloak.realm(keycloakProperties.getRealm())
                .clients()
                .get(clientUUID)
                .roles()
                .get(USER_ROLE)
                .toRepresentation();

        getUserResource(userId)
                .roles()
                .clientLevel(clientUUID)
                .add(List.of(userRole));
    }

    private User buildDbUser(String userId, String username) {
        User dbUser = new User();
        dbUser.setId(userId);
        dbUser.setDisplayName(username);
        dbUser.setAvatar(null);
        dbUser.setCreatedAt(LocalDateTime.now());
        return dbUser;
    }
}
