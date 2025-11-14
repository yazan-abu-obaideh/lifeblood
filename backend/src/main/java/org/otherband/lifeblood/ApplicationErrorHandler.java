package org.otherband.lifeblood;

import lombok.extern.slf4j.Slf4j;
import org.otherband.lifeblood.generated.model.ErrorResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@Slf4j
@ControllerAdvice
public class ApplicationErrorHandler {

    @ExceptionHandler(UserAuthException.class)
    public ResponseEntity<ErrorResponse> handleException(UserAuthException userAuthException) {
        return ResponseEntity.status(401).body(toErrorResponse(userAuthException.getMessage()));
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleException(UserException userException) {
        log.warn("User exception occurred", userException);
        return ResponseEntity.badRequest().body(toErrorResponse(userException.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException violationException) {
        log.warn("Violation exception occurred", violationException);
        List<ObjectError> allErrors = violationException.getAllErrors();
        List<String> errorMessages = allErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
        return ResponseEntity.badRequest().body(toErrorResponse(String.join(",", errorMessages)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception anyException) {
        log.error("Internal server error", anyException);
        return ResponseEntity.internalServerError().body(toErrorResponse("Something went wrong"));
    }

    private ErrorResponse toErrorResponse(String message) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorMessage(message);
        return errorResponse;
    }

}
