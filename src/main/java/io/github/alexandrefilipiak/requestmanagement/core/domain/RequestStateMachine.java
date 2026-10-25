package io.github.alexandrefilipiak.requestmanagement.core.domain;

import java.util.Map;

import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestEvent.*;
import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState.*;

public class RequestStateMachine {
    private static final Map<RequestState, Map<RequestEvent, RequestState>> TRANSITIONS = Map.of(
            CREATED,  Map.of(VERIFY, VERIFIED, DELETE, DELETED),
            VERIFIED, Map.of(ACCEPT, ACCEPTED, REJECT, REJECTED),
            ACCEPTED, Map.of(PUBLISH, PUBLISHED, REJECT, REJECTED)
    );

    private RequestStateMachine() {}

    public static RequestState next(RequestState initialState, RequestEvent event) {
        RequestState target = TRANSITIONS.getOrDefault(initialState, Map.of()).get(event);
        if (target==null) {
            throw new InvalidTransitionException(initialState, event);
        }
        return target;

    }
}
