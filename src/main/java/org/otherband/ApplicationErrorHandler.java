package org.otherband;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class ApplicationErrorHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleException(UserException userException) {
        return ResponseEntity.badRequest().body(new ErrorResponse(userException.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException violationException) {
        List<ObjectError> allErrors = violationException.getAllErrors();
        List<String> errorMessages = allErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
        return ResponseEntity.badRequest().body(new ErrorResponse(String.join(",", errorMessages)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception anyException) {
        return ResponseEntity.internalServerError().body(new ErrorResponse("Something went wrong"));
    }

    public record ErrorResponse(String errorMessage) {
    }

}
