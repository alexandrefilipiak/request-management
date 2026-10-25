package io.github.alexandrefilipiak.requestmanagement.infrastructure.api;

import io.github.alexandrefilipiak.requestmanagement.core.domain.Request;
import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState;

record RequestDetailsResponse(Long id, String name, String content, RequestState state,
                              String reason, Long publicationNumber) {

    static RequestDetailsResponse from(Request r) {
        return new RequestDetailsResponse(r.getId(), r.getName(), r.getContent(), r.getState(),
                r.getReason(), r.getPublicationNumber());
    }
}
