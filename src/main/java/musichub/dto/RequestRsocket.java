package musichub.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestRsocket {
    Map<String, Object> payload = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public <T> void setPayload(String key, T value) {
        payload.put(key, value);
    }

    public <T> T getPayloadAs(String key, Class<T> type) {
        Object raw = payload.get(key);
        return objectMapper.convertValue(raw, type);
    }
}
