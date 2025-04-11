package MusicHub.dto.UserDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    @NotBlank(message = "Username can not be empty")
    String email;

    @NotBlank(message = "Password can not be empty")
    String password;

    @NotBlank(message = "Name can not be empty")
    String name;
}
