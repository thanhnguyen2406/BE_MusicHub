package musichub.common;

import musichub.exception.AppException;
import musichub.exception.ErrorCode;

import java.util.Collection;

public class AssertUtil {
    public static void isTrue(boolean condition, ErrorCode errorCode) {
        if (condition) throw new AppException(errorCode);
    }

    public static void isNull(Object obj, ErrorCode errorCode) {
        if (obj == null) throw new AppException(errorCode);
    }

    public static void notNull(Object obj, ErrorCode errorCode) {
        if (obj != null) throw new AppException(errorCode);
    }

    public static void notEmpty(Collection<?> collection, ErrorCode errorCode) {
        if (collection != null && !collection.isEmpty()) {
            throw new AppException(errorCode);
        }
    }

    public static void isEmpty(Collection<?> collection, ErrorCode errorCode) {
        if (collection == null || collection.isEmpty()) {
            throw new AppException(errorCode);
        }
    }

    public static void notBlank(String value, ErrorCode errorCode) {
        if (value == null || value.trim().isEmpty()) throw new AppException(errorCode);
    }

}
