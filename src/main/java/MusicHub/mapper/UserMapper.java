package MusicHub.mapper;

import MusicHub.dto.UserDTO.UserDTO;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserRepresentation toUserRepresentation(UserDTO userDTO) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(userDTO.getUsername());
        userRepresentation.setEmail(userDTO.getEmail());
        userRepresentation.setFirstName(userDTO.getFirstName());
        userRepresentation.setLastName(userDTO.getLastName());
        return userRepresentation;
    }

    public UserDTO toUserDTO(UserRepresentation userRepresentation) {
        return UserDTO.builder()
                .username(userRepresentation.getUsername())
                .email(userRepresentation.getEmail())
                .firstName(userRepresentation.getFirstName())
                .lastName(userRepresentation.getLastName())
                .build();
    }
}
