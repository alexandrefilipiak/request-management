package io.github.alexandrefilipiak.requestmanagement.domain;

public enum RequestState {
    CREATED(true), VERIFIED(true), ACCEPTED(false),
    PUBLISHED(false), DELETED(false), REJECTED(false);

    private final boolean contentEditable;

    RequestState(boolean contentEditable) {
        this.contentEditable = contentEditable;
    }

    boolean isContentEditable() {
        return contentEditable;
    }
}