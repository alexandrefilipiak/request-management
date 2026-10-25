package io.github.alexandrefilipiak.requestmanagement.core.domain;

public class ContentNotEditableException extends DomainException {
    private final RequestState state;

    public ContentNotEditableException(RequestState state) {
        super("The state %s does not allow content edition".formatted(state.name()));
        this.state = state;
    }

    public RequestState getState() {
        return state;
    }
}
