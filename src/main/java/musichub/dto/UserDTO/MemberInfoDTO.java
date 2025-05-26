package musichub.dto.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import musichub.enums.ChannelRole;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfoDTO {
    private String displayName;
    private String avatarUrl;
    private ChannelRole role;
}

