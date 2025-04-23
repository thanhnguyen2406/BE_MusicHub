package MusicHub.dto.AuthenticateDTO;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
public class IntrospectDTO {
    private boolean active;
    private String username;
    private String   scope;
    private String client_id;
    private long exp;
    private long iat;
    private String sub;
    private String aud;
    private String iss;
}
