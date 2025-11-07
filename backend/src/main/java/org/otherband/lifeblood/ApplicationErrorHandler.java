package org.otherband.lifeblood;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

@Slf4j
@ControllerAdvice
public class ApplicationErrorHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleException(UserException userException) {
        log.warn("User exception occurred", userException);
        return ResponseEntity.badRequest().body(new ErrorResponse(userException.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException violationException) {
        log.warn("Violation exception occurred", violationException);
        List<ObjectError> allErrors = violationException.getAllErrors();
        List<String> errorMessages = allErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
        return ResponseEntity.badRequest().body(new ErrorResponse(String.join(",", errorMessages)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception anyException) {
        log.error("Internal server error", anyException);
        return ResponseEntity.internalServerError().body(new ErrorResponse("Something went wrong"));
    }

    public record ErrorResponse(String errorMessage) {
    }

}
