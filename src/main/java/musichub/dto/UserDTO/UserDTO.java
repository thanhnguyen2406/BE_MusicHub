package musichub.dto.UserDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    String id;
    String email;
    String firstName;
    String lastName;    
    String password;
    LocalDateTime createdAt;
    String avatar;
    String displayName;
}
