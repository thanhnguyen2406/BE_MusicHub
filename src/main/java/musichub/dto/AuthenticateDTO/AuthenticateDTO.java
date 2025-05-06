package musichub.dto.AuthenticateDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticateDTO {
    @NotBlank(message = "Username can not be empty")
    String email;

    @NotBlank(message = "Password can not be empty")
    String password;
}
