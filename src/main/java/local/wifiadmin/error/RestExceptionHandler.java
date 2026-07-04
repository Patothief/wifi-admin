package local.wifiadmin.error;

import jakarta.validation.ConstraintViolationException;
import local.wifiadmin.api.model.ErrorBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ErrorBody> handleBadRequest(BadRequestException exception) {
        return badRequest(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorBody> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        return badRequest("Request body is invalid");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorBody> handleConstraintViolation(ConstraintViolationException exception) {
        return badRequest("Request parameter is invalid");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorBody> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        return badRequest("Request body is malformed or contains unsupported values");
    }

    private ResponseEntity<ErrorBody> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorBody()
                        .code("BAD_REQUEST")
                        .message(message));
    }
}
