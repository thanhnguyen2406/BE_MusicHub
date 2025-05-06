package musichub.mapper;

import musichub.dto.UserDTO.UserDTO;
import musichub.model.User;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserRepresentation toUserRepresentation(UserDTO userDTO) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEmail(userDTO.getEmail());
        userRepresentation.setFirstName(userDTO.getFirstName());
        userRepresentation.setLastName(userDTO.getLastName());
        return userRepresentation;
    }

    public UserDTO toUserDTO(UserRepresentation userRepresentation, User user) {
        return UserDTO.builder()
                .email(userRepresentation.getEmail())
                .firstName(userRepresentation.getFirstName())
                .lastName(userRepresentation.getLastName())
                .createdAt(user.getCreatedAt())
                .avatar(user.getAvatar())
                .displayName(user.getDisplayName())
                .build();
    }
}
