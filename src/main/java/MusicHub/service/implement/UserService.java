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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public ResponseAPI<UserDTO> getMyInfo() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> user = userRepository.findByEmail(username);
            if (user.isEmpty()) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
            UserDTO userDTO = UserDTO.builder()
                    .email(user.get().getEmail())
                    .name(user.get().getName())
                    .build();
            return ResponseAPI.<UserDTO>builder()
                    .code(200)
                    .data(userDTO)
                    .message("My Info fetched successfully")
                    .build();
        } catch (AppException e) {
            return ResponseAPI.<UserDTO>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getErrorCode().getMessage())
                    .build();
        } catch (Exception e) {
            return ResponseAPI.<UserDTO>builder()
                    .code(500)
                    .message("Error Occurs During Get User Info: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ResponseAPI<Void> createPatient(String email, String name) {
        try {
            if (!email.endsWith("@gmail.com")) {
                throw new AppException(ErrorCode.UNAUTHENTICATED_USERNAME_DOMAIN);
            }
            if (userRepository.findByEmail(email).isPresent()) {
                throw new AppException(ErrorCode.USER_EXISTED);
            }
            User user = User.builder()
                    .email(email)
                    .name(name)
                    .role("PATIENT")
                    .isGoogleAccount(true)
                    .build();
            userRepository.save(user);
            return ResponseAPI.<Void>builder()
                    .code(200)
                    .message("Patient register successfully")
                    .build();
        } catch (AppException e) {
            return ResponseAPI.<Void>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getErrorCode().getMessage())
                    .build();
        } catch (Exception e) {
            return ResponseAPI.<Void>builder()
                    .code(500)
                    .message("Error Occurs During Register Patient: " + e.getMessage())
                    .build();
        }
    }
}
