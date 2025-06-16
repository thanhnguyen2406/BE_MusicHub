package musichub.common;

import musichub.exception.AppException;
import musichub.exception.ErrorCode;
import reactor.core.publisher.Mono;

import java.util.Collection;

public class AssertUtil {
    public static void isTrue(boolean condition, ErrorCode errorCode) {
        if (!condition) throw new AppException(errorCode);
    }

    public static <T> Mono<T> isTrueMono(boolean condition, ErrorCode errorCode) {
        return (!condition) ? Mono.error(new AppException(errorCode)) : Mono.empty();
    }

    public static void isNull(Object obj, ErrorCode errorCode) {
        if (obj == null) throw new AppException(errorCode);
    }

    public static void notEmpty(Collection<?> collection, ErrorCode errorCode) {
        if (collection != null && !collection.isEmpty()) {
            throw new AppException(errorCode);
        }
    }

    public static void notBlank(String value, ErrorCode errorCode) {
        if (value == null || value.trim().isEmpty()) throw new AppException(errorCode);
    }
}