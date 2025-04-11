package MusicHub.mapper;

import MusicHub.dto.UserDTO.UserDTO;
import MusicHub.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toUser(UserDTO dto) {
        return User.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .name(dto.getName())
                .build();
    }
}
