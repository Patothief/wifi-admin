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

    @ExceptionHandler(PlatformNotFoundException.class)
    ResponseEntity<ErrorBody> handlePlatformNotFound(PlatformNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(PlatformCommunicationException.class)
    ResponseEntity<ErrorBody> handlePlatformCommunication(PlatformCommunicationException exception) {
        return error(HttpStatus.BAD_GATEWAY, "BAD_GATEWAY", exception.getMessage());
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
        return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }

    private ResponseEntity<ErrorBody> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorBody()
                        .code(code)
                        .message(message));
    }
}
