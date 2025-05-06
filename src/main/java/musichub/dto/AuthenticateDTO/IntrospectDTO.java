package musichub.dto.AuthenticateDTO;

import lombok.Data;

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
