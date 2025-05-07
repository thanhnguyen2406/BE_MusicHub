package musichub.dto.ChannelDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
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
public class ChannelDTO {
    String id;
    @JsonProperty("name")
    String name;
    @JsonProperty("url")
    String url;
    @JsonProperty("tagList")
    List<String> tagList;
    @JsonProperty("description")
    String description;
    @JsonProperty("password")
    String password;
    @JsonProperty("maxUsers")
    Integer maxUsers;
    @JsonProperty("allowOthersToManageSongs")
    Boolean allowOthersToManageSongs;
    @JsonProperty("allowOthersToControlPlayback")
    Boolean allowOthersToControlPlayback;
}
