package MusicHub.service.implement;

import MusicHub.configuration.KeycloakProperties;
import MusicHub.dto.UserDTO.ResetPasswordDTO;
import MusicHub.dto.UserDTO.UserDTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserService implements IUserService {
    Keycloak keycloak;
    KeycloakProperties keycloakProperties;
    UserRepository userRepository;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmailVerified(false);

        CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
        credentialRepresentation.setValue(userDTO.getPassword());
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

        List<CredentialRepresentation> list = new ArrayList<>();
        list.add(credentialRepresentation);
        user.setCredentials(list);

        UsersResource usersResource = getUsersResource();

        Response response = usersResource.create(user);

        if(Objects.equals(201,response.getStatus())){
            URI location = response.getLocation();
            String path = location.getPath();
            String userId = path.substring(path.lastIndexOf("/") + 1);

            User dbUser = new User();
            dbUser.setId(userId);
            dbUser.setDisplayName(userDTO.getFirstName() + " " + userDTO.getLastName());
            dbUser.setJoinedAt(LocalDateTime.now());
            dbUser.setAvatar(null);

            userRepository.save(dbUser);

            emailVerification(userId);
            return userDTO;
        }
        if (response.getStatus() != 201) {
            throw new RuntimeException("Keycloak user creation failed: " + response.getStatusInfo());
        }

        return null;
    }

    private UsersResource getUsersResource() {
        RealmResource realm1 = keycloak.realm(keycloakProperties.getRealm());
        return realm1.users();
    }

    @Override
    public UserRepresentation getUserById(String userId) {


        return  getUsersResource().get(userId).toRepresentation();
    }

    @Override
    public void deleteUserById(String userId) {
        getUsersResource().delete(userId);
    }


    @Override
    public void emailVerification(String userId){

        UsersResource usersResource = getUsersResource();
        usersResource.get(userId).sendVerifyEmail();
    }

    public UserResource getUserResource(String userId){
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }

    @Override
    public void updatePassword(String userId) {

        UserResource userResource = getUserResource(userId);
        List<String> actions= new ArrayList<>();
        actions.add("UPDATE_PASSWORD");
        userResource.executeActionsEmail(actions);

    }

    @Override
    public void updatePassword(ResetPasswordDTO resetPassword, String userId) {
        UserResource userResource = getUserResource(userId);
        CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
        credentialRepresentation.setValue(resetPassword.getPassword());
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setTemporary(false);
        userResource.resetPassword(credentialRepresentation);
    }


//    @Override
//    public Mono<ResponseAPI<UserDTO>> getMyInfo() {
//        return ReactiveSecurityContextHolder.getContext()
//                .map(ctx -> (JwtAuthenticationToken) ctx.getAuthentication())
//                .map(auth -> auth.getToken().getClaimAsString("email"))
//                .flatMap(email -> userRepository.findByEmail(email)
//                        .map(user -> {
//                            UserDTO userDTO = userMapper.toUserDTO(user);
//                            return ResponseAPI.<UserDTO>builder()
//                                    .code(200)
//                                    .data(userDTO)
//                                    .message("My Info fetched successfully")
//                                    .build();
//                        })
//                        .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
//                )
//                .onErrorResume(AppException.class, e -> Mono.just(
//                        ResponseAPI.<UserDTO>builder()
//                                .code(e.getErrorCode().getCode())
//                                .message(e.getErrorCode().getMessage())
//                                .build()
//                ))
//                .onErrorResume(e -> Mono.just(
//                        ResponseAPI.<UserDTO>builder()
//                                .code(500)
//                                .message("Error Occurs During Get User Info: " + e.getMessage())
//                                .build()
//                ));
//    }
//
//    @Override
//    public Mono<ResponseAPI<Void>> createUser(String email, String name) {
//        if (!email.endsWith("@gmail.com")) {
//            return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_USERNAME_DOMAIN));
//        }
//
//        return userRepository.existsByEmail(email)
//                .flatMap(existed -> {
//                    if (existed) {
//                        return Mono.error(new AppException(ErrorCode.USER_EXISTED));
//                    }
//
//                    User user = User.builder()
//                            .email(email)
//                            .name(name)
//                            .role("USER")
//                            .isGoogleAccount(true)
//                            .build();
//
//                    return userRepository.save(user)
//                            .thenReturn(ResponseAPI.<Void>builder()
//                                    .code(200)
//                                    .message("User register successfully")
//                                    .build());
//                })
//                .onErrorResume(AppException.class, e ->
//                        Mono.just(ResponseAPI.<Void>builder()
//                                .code(e.getErrorCode().getCode())
//                                .message(e.getErrorCode().getMessage())
//                                .build())
//                )
//                .onErrorResume(e -> Mono.just(ResponseAPI.<Void>builder()
//                        .code(500)
//                        .message("Error Occurs During Register User: " + e.getMessage())
//                        .build()));
//    }

}
