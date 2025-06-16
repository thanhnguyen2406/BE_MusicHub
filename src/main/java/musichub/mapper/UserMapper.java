package musichub.mapper;

import lombok.RequiredArgsConstructor;
import musichub.dto.UserDTO.MemberInfoDTO;
import musichub.dto.UserDTO.UserDTO;
import musichub.enums.ChannelRole;
import musichub.model.User;
import musichub.repository.UserRepository;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final UserRepository userRepository;

    public UserRepresentation toUserRepresentation(UserDTO userDTO) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEmail(userDTO.getEmail());
        userRepresentation.setFirstName(userDTO.getFirstName());
        userRepresentation.setLastName(userDTO.getLastName());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(userDTO.getPassword());
        credentialRepresentation.setTemporary(false);
        userRepresentation.setCredentials(List.of(credentialRepresentation));
        return userRepresentation;
    }

    public User toUser (String userId ,UserDTO userDTO) {
        return User.builder()
                .id(userId)
                .avatar(userDTO.getAvatar())
                .displayName(userDTO.getDisplayName())
                .createdAt(LocalDateTime.now())
                .addedBy(userId)
                .build();
    }

    public UserDTO toUserDTO(UserRepresentation userRepresentation, User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(userRepresentation.getEmail())
                .firstName(userRepresentation.getFirstName())
                .lastName(userRepresentation.getLastName())
                .createdAt(user.getCreatedAt())
                .avatar(user.getAvatar())
                .displayName(user.getDisplayName())
                .build();
    }

    public Mono<List<MemberInfoDTO>> toMembersList(Set<String> members, String userId) {
        return Flux.fromIterable(members)
                .flatMap(memberId ->
                        userRepository.findById(memberId)
                                .map(user -> MemberInfoDTO.builder()
                                        .userId(memberId)
                                        .displayName(user.getDisplayName())
                                        .avatarUrl(user.getAvatar())
                                        .role(memberId.equals(userId) ? ChannelRole.OWNER : ChannelRole.MEMBER)
                                        .build())
                )
                .collectSortedList((a, b) -> {
                    if (a.getRole() == ChannelRole.OWNER) return -1;
                    if (b.getRole() == ChannelRole.OWNER) return 1;
                    return 0;
                });
    }
}
