package musichub.dto.ChannelDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import musichub.dto.SongDTO.SongDTO;
import musichub.dto.UserDTO.MemberInfoDTO;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChannelInfoDTO {
    String id;
    String name;
    String ownerId;
    String url;
    List<String> tagList;
    String description;
    Integer maxUsers;
    Integer currentUsers;
    Boolean allowOthersToManageSongs;
    Boolean allowOthersToControlPlayback;
    Boolean isLocked;
    List<MemberInfoDTO> members;
    List<SongDTO> songs;
}
