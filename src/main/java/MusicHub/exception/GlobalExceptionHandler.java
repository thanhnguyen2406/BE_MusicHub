package MusicHub.exception;

import MusicHub.dto.ResponseAPI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ResponseAPI<Void>> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        ResponseAPI<Void> response = new ResponseAPI<>();
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ResponseAPI<Void>> handleValidation(MethodArgumentNotValidException e) {
        String enumKey = e.getFieldError().getDefaultMessage();
        ErrorCode error = ErrorCode.valueOf(enumKey);

        ResponseAPI<Void> response = new ResponseAPI<>();
        response.setCode(error.getCode());
        response.setMessage(error.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}
