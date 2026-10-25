package io.github.alexandrefilipiak.requestmanagement.core.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * One entry of a request's history: a state change, with the event that caused it.
 * Entries are created by {@link Request} only and never modified.
 * No getter for request: we don't need to retrieve the associated request from this entity,
 * and leaving it out avoids accidental lazy loads.
 */
@Entity
@Table(name = "state_changes")
public class StateChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_state", length = 20)
    private RequestState fromState;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_state", nullable = false, length = 20)
    private RequestState toState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestEvent event;

    @Column(length = 1000)
    private String reason;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected StateChange() {
        // required by JPA
    }

    StateChange(Request request, RequestState fromState, RequestState toState,
                RequestEvent event, String reason, Instant occurredAt) {
        this.request = request;
        this.fromState = fromState;
        this.toState = toState;
        this.event = event;
        this.reason = reason;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public RequestState getFromState() {
        return fromState;
    }

    public RequestState getToState() {
        return toState;
    }

    public RequestEvent getEvent() {
        return event;
    }

    public String getReason() {
        return reason;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}