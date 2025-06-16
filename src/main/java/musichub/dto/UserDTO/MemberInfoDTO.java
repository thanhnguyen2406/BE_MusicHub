package musichub.dto.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import musichub.enums.ChannelRole;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfoDTO {
    private String userId;
    private String displayName;
    private String avatarUrl;
    private ChannelRole role;
}

