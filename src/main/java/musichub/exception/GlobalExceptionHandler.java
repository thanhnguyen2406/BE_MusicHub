package musichub.exception;

import musichub.dto.ResponseAPI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public Mono<ResponseEntity<ResponseAPI<Void>>> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        ResponseAPI<Void> response = new ResponseAPI<>();
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());

        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(AppException.class)
    public Mono<ResponseEntity<ResponseAPI<Void>>> handleThrowableException(Throwable e) {
        ResponseAPI<Void> response = new ResponseAPI<>();
        response.setCode(500);
        response.setMessage("Internal Server Error: " + e.getMessage());

        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<ResponseAPI<Void>>> handleValidation(MethodArgumentNotValidException e) {
        String enumKey = e.getFieldError().getDefaultMessage();
        ErrorCode error = ErrorCode.valueOf(enumKey);

        ResponseAPI<Void> response = new ResponseAPI<>();
        response.setCode(error.getCode());
        response.setMessage(error.getMessage());

        return Mono.just(ResponseEntity.badRequest().body(response));
    }
}

