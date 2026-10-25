package io.github.alexandrefilipiak.requestmanagement.core.usecases;

/**
 * Thrown by the use cases when no request exists for the given id.
 * <p>
 * The message is written for the end user and may be shown to them as is:
 * it must never contain technical details.
 */
public class RequestNotFoundException extends RuntimeException {
    private final long id;

    public RequestNotFoundException(long id) {
        super("No request found with id: %d".formatted(id));
        this.id = id;
    }
}