package io.github.alexandrefilipiak.requestmanagement.usecases;

public class RequestNotFoundException extends RuntimeException {
    long id;

    public RequestNotFoundException(long id) {
        super("Request not found for id: %d".formatted(id));
        this.id = id;
    }
}
