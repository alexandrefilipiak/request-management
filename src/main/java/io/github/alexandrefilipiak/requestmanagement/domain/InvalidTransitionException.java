package io.github.alexandrefilipiak.requestmanagement.domain;

public class InvalidTransitionException extends RuntimeException {

    private final RequestState state;
    private final RequestEvent event;

    InvalidTransitionException(RequestState state, RequestEvent event) {
        super("Cannot apply %s to an application in state %s".formatted(event, state));
        this.state = state;
        this.event = event;
    }

    public RequestState getState() {
        return state;
    }

    public RequestEvent getEvent() {
        return event;
    }
}
