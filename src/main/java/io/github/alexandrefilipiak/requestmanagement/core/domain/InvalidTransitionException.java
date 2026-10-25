package io.github.alexandrefilipiak.requestmanagement.core.domain;

public class InvalidTransitionException extends DomainException {

    private final RequestState state;
    private final RequestEvent event;

    public InvalidTransitionException(RequestState state, RequestEvent event) {
        super("Cannot apply %s to a request in state %s".formatted(event, state));
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
