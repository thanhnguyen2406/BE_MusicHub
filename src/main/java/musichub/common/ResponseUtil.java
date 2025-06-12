package musichub.common;

import musichub.dto.ResponseAPI;

public class ResponseUtil {
    public static <T> ResponseAPI<T> success(String message) {
        return success(null, message);
    }

    public static <T> ResponseAPI<T> success(T data, String message) {
        return ResponseAPI.<T>builder()
                .code(200)
                .data(data)
                .message(message)
                .build();
    }
}
