package MusicHub.service.implement;

import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.UserDTO;
import MusicHub.exception.AppException;
import MusicHub.exception.ErrorCode;
import MusicHub.mapper.UserMapper;
import MusicHub.model.User;
import MusicHub.repository.UserRepository;
import MusicHub.service.interf.IUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Mono<ResponseAPI<UserDTO>> getMyInfo(String username) {
        return userRepository.findByEmail(username)
                .map(user -> {
                    UserDTO userDTO = userMapper.toUserDTO(user);
                    return ResponseAPI.<UserDTO>builder()
                            .code(200)
                            .data(userDTO)
                            .message("My Info fetched successfully")
                            .build();
                })
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .onErrorResume(AppException.class, e -> Mono.just(
                        ResponseAPI.<UserDTO>builder()
                                .code(e.getErrorCode().getCode())
                                .message(e.getErrorCode().getMessage())
                                .build()
                ))
                .onErrorResume(e -> Mono.just(
                        ResponseAPI.<UserDTO>builder()
                                .code(500)
                                .message("Error Occurs During Get User Info: " + e.getMessage())
                                .build()
                ));
    }

    @Override
    public Mono<ResponseAPI<Void>> createPatient(String email, String name) {
        if (!email.endsWith("@gmail.com")) {
            return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_USERNAME_DOMAIN));
        }

        return userRepository.existsByEmail(email)
                .flatMap(existed -> {
                    if (existed) {
                        return Mono.error(new AppException(ErrorCode.USER_EXISTED));
                    }

                    User user = User.builder()
                            .email(email)
                            .name(name)
                            .role("USER")
                            .isGoogleAccount(true)
                            .build();

                    return userRepository.save(user)
                            .thenReturn(ResponseAPI.<Void>builder()
                                    .code(200)
                                    .message("User register successfully")
                                    .build());
                })
                .onErrorResume(AppException.class, e ->
                        Mono.just(ResponseAPI.<Void>builder()
                                .code(e.getErrorCode().getCode())
                                .message(e.getErrorCode().getMessage())
                                .build())
                )
                .onErrorResume(e -> Mono.just(ResponseAPI.<Void>builder()
                        .code(500)
                        .message("Error Occurs During Register User: " + e.getMessage())
                        .build()));
    }

}
