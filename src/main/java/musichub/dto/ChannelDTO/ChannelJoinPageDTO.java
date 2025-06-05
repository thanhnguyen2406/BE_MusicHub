package musichub.dto.ChannelDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChannelJoinPageDTO {
    String id;
    String name;
    String url;
    List<String> tagList;
    String description;
    String password;
    Integer maxUsers;
    Integer currentUsers;
    boolean isLocked;
    String ownerDisplayName;
    String ownerAvatar;
}