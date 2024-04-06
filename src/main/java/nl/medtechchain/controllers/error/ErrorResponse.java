package nl.medtechchain.controllers.error;

/**
 * A custom class for error responses.
 *
 * @param status        the HTTP status code
 * @param error         the error itself (reason)
 * @param message       the error message
 * @param path          the request URI that is being handled
 */
public record ErrorResponse(int status, String error, String message, String path) {
}
