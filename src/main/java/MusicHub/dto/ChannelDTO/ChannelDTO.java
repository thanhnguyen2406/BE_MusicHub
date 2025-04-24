package MusicHub.dto.ChannelDTO;

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
    @NotBlank(message = "Channel's name can not be empty")
    @JsonProperty("name")
    String name;
    @NotBlank(message = "Channel's url can not be empty")
    @JsonProperty("url")
    String url;
    @NotBlank(message = "Channel's tags can not be empty")
    @JsonProperty("tagList")
    List<String> tagList;
    @JsonProperty("description")
    String description;
    @JsonProperty("password")
    String password;
}
