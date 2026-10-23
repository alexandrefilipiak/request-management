package io.github.alexandrefilipiak.requestmanagement.domain;

public enum RequestEvent {
    VERIFY(false), ACCEPT(false), PUBLISH(false), REJECT(true), DELETE(true);

    private final boolean reasonRequired;

    RequestEvent(boolean reasonRequired) {
        this.reasonRequired = reasonRequired;
    }

    public boolean isReasonRequired() {
        return reasonRequired;
    }
}
