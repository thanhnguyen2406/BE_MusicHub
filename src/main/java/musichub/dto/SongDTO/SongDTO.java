package musichub.dto.SongDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import musichub.enums.Status;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SongDTO {
    String id;
    String title;
    String artist;
    String url;
    String moodTag;
    String thumbnail;
    Integer duration;
    Status status;
    Integer totalUpVotes;
    Integer totalDownVotes;
}
