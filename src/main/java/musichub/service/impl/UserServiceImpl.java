package musichub.service.impl;

import musichub.common.AssertUtil;
import musichub.common.ResponseUtil;
import musichub.configuration.security.KeycloakProperties;
import musichub.dto.ResponseAPI;
import musichub.dto.UserDTO.ResetPasswordDTO;
import musichub.dto.UserDTO.UserDTO;
import musichub.exception.AppException;
import musichub.exception.ErrorCode;
import musichub.mapper.UserMapper;
import musichub.model.User;
import musichub.repository.UserRepository;
import musichub.service.UserService;
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
public class UserServiceImpl implements UserService {
    Keycloak keycloak;
    KeycloakProperties keycloakProperties;
    UserRepository userRepository;
    UserMapper userMapper;

    private static final String USER_ROLE = "musicHub_user";
    private static final int HTTP_CREATED = 201;

    @Override
    public Mono<ResponseAPI<UserDTO>> getUserById(String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(existingUser -> {
                    UserRepresentation userRepresentation = getUsersResource().get(userId).toRepresentation();

                    UserDTO userDTO = userMapper.toUserDTO(userRepresentation, existingUser);

                    return Mono.just(ResponseUtil.success(userDTO, "User fetched successfully"));
                });
    }

    @Override
    public Mono<ResponseAPI<Void>> createUser(UserDTO userDTO) {
        return Mono.fromCallable(() -> {
                    String userId = createUserInKeycloak(userDTO);

                    User dbUser = createUserModel(userId, userDTO);

                    assignUserRole(userId);
                    
                    return dbUser;
                })
                .flatMap(userRepository::save)
                .thenReturn(ResponseUtil.success("User created successfully"));
    }

    private String createUserInKeycloak(UserDTO dto) {
        UsersResource usersResource = getUsersResource();

        List<UserRepresentation> existingUsers = usersResource.search(dto.getEmail(), true);
        AssertUtil.notEmpty(existingUsers, ErrorCode.USER_EXISTED);

        List<UserRepresentation> usersByEmail = usersResource.search(null, null, null, dto.getEmail(), null, null);
        AssertUtil.notEmpty(usersByEmail, ErrorCode.USER_EXISTED);

        UserRepresentation user = userMapper.toUserRepresentation(dto);

        Response response = usersResource.create(user);
        AssertUtil.isTrue(response.getStatus() != HTTP_CREATED, ErrorCode.KEYCLOAK_FAIL);

        URI location = response.getLocation();
        String path = location.getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }


    private User createUserModel(String userId, UserDTO dto) {
        return userMapper.toUser(userId, dto);
    }

    private void assignUserRole(String userId) {
        String clientUUID = getClientUUID();
        RoleRepresentation userRole = getUserRoleRepresentation();

        getUserResource(userId)
                .roles()
                .clientLevel(clientUUID)
                .add(List.of(userRole));
    }

    private String getClientUUID() {
        return keycloak.realm(keycloakProperties.getRealm())
                .clients()
                .findByClientId(keycloakProperties.getClientId())
                .get(0)
                .getId();
    }

    private RoleRepresentation getUserRoleRepresentation() {
        String clientUUID = getClientUUID();
        return keycloak.realm(keycloakProperties.getRealm())
                .clients()
                .get(clientUUID)
                .roles()
                .get(USER_ROLE)
                .toRepresentation();
    }

    @Override
    public Mono<ResponseAPI<Void>> deleteUserById(String userId) {
        return Mono.fromCallable(() -> getUsersResource().get(userId).toRepresentation())
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user ->
                        Mono.fromRunnable(() ->
                                getUsersResource().delete(userId)))
                .then(userRepository.deleteById(userId))
                .thenReturn(ResponseUtil.success("User deleted successfully"));
    }

    @Override
    public Mono<ResponseAPI<Void>> updateUserById(UserDTO request, String userId) {
        String id = request.getId();
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(dbUser -> {
                    UserResource userResource = getUsersResource().get(userId);
                    UserRepresentation userRepresentation = userResource.toRepresentation();

                    AssertUtil.isNull(userRepresentation, ErrorCode.USER_NOT_FOUND);
                    AssertUtil.isTrue(!userId.equals(id), ErrorCode.USER_NOT_FOUND);

                    updateUser(userRepresentation, dbUser, request);
                    userResource.update(userRepresentation);

                    return userRepository.save(dbUser)
                            .thenReturn(ResponseUtil.success("User updated successfully"));
                });
    }

    private void updateUser(UserRepresentation userRepresentation, User dbUser, UserDTO userDTO) {
        userRepresentation.setFirstName(userDTO.getFirstName());
        userRepresentation.setLastName(userDTO.getLastName());

        dbUser.setUpdatedAt(LocalDateTime.now());
        dbUser.setDisplayName(userDTO.getDisplayName());
        dbUser.setAvatar(userDTO.getAvatar());
    }

    @Override
    public Mono<ResponseAPI<Void>> emailVerification(String userId) {
        return Mono.fromCallable(() -> {
                    UserResource userResource = getUsersResource().get(userId);
                    UserRepresentation userRepresentation = userResource.toRepresentation();

                    AssertUtil.isNull(userRepresentation, ErrorCode.USER_NOT_FOUND);

                    return userResource;
                })
                .flatMap(userResource -> {
                    List<String> actions = List.of("UPDATE_EMAIL");
                    userResource.executeActionsEmail(actions);
                    return Mono.just(ResponseUtil.success("Verification email sent successfully"));
                });
    }

    @Override
    public Mono<ResponseAPI<Void>> updatePassword(String userId) {
        return Mono.fromCallable(() -> {
                    UserResource userResource = getUsersResource().get(userId);
                    UserRepresentation userRepresentation = userResource.toRepresentation();

                    AssertUtil.isNull(userRepresentation, ErrorCode.USER_NOT_FOUND);

                    return userResource;
                })
                .flatMap(userResource -> {
                    List<String> actions = List.of("UPDATE_PASSWORD");
                    userResource.executeActionsEmail(actions);
                    return Mono.just(ResponseUtil.success("Password update email sent successfully"));
                });
    }

    @Override
    public Mono<ResponseAPI<Void>> updatePassword(ResetPasswordDTO resetPassword, String userId) {
        return Mono.fromCallable(() -> {
                    String password = resetPassword.getPassword();
                    AssertUtil.isNull(password, ErrorCode.PASSWORD_IS_EMPTY);
                    AssertUtil.notBlank(password, ErrorCode.PASSWORD_IS_EMPTY);

                    UserResource userResource = getUsersResource().get(userId);
                    userResource.toRepresentation();

                    CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
                    credentialRepresentation.setValue(resetPassword.getPassword());
                    credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
                    credentialRepresentation.setTemporary(false);

                    userResource.resetPassword(credentialRepresentation);

                    return ResponseUtil.success("Password updated successfully");
                });
    }

    private UsersResource getUsersResource() {
        RealmResource realm = keycloak.realm(keycloakProperties.getRealm());
        return realm.users();
    }

    public UserResource getUserResource(String userId){
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }
}