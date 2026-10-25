package io.github.alexandrefilipiak.requestmanagement.infrastructure.api;

import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestEvent;
import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState;
import io.github.alexandrefilipiak.requestmanagement.core.domain.StateChange;

import java.time.Instant;
import java.util.List;

record HistoryResponse(long requestId, List<Entry> entries) {

    record Entry(RequestState fromState, RequestState toState, RequestEvent event,
                 String reason, Instant occurredAt) {

        static Entry from(StateChange c) {
            return new Entry(c.getFromState(), c.getToState(), c.getEvent(),
                    c.getReason(), c.getOccurredAt());
        }
    }

    static HistoryResponse of(long requestId, List<StateChange> history) {
        return new HistoryResponse(requestId, history.stream().map(Entry::from).toList());
    }
}
