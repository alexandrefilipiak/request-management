package io.github.alexandrefilipiak.requestmanagement.domain;

public class ReasonRequiredException extends RuntimeException {
    private final RequestEvent event;

    public ReasonRequiredException(RequestEvent event) {
        super("Reason is required for event %s".formatted(event.name()));
        this.event=event;
    }

    public RequestEvent getEvent() {
        return event;
    }
}
