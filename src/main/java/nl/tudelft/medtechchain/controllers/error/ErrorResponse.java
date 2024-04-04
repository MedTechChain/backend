package nl.tudelft.medtechchain.controllers.error;

public record ErrorResponse(int status, String error, String message, String path) {
}
