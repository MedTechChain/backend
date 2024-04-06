package nl.medtechchain.controllers.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


/**
 * A class for global exception handling.
 * All exceptions are converted into 500 INTERNAL SERVER ERROR.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Converts the thrown exception into a ResponseEntity to be sent to the client.
     *
     * @param ex            the thrown exception that is being handled
     * @param request       the HTTP request that is being handled
     * @return              the response entity with the corresponding status code and error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI()
        );
        ex.printStackTrace();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}