package io.github.alexandrefilipiak.requestmanagement.core.domain;

public enum RequestEvent {
    CREATE(false), VERIFY(false), ACCEPT(false), PUBLISH(false), REJECT(true), DELETE(true);

    private final boolean reasonRequired;

    RequestEvent(boolean reasonRequired) {
        this.reasonRequired = reasonRequired;
    }

    public boolean isReasonRequired() {
        return reasonRequired;
    }
}
