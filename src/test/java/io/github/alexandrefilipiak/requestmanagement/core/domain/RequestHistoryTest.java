package io.github.alexandrefilipiak.requestmanagement.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestEvent.*;
import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestFixtures.aRequestIn;
import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class RequestHistoryTest {

    @Test
    @DisplayName("a new request has one history entry for its creation")
    void creationIsRecorded() {
        //when
        Request request = Request.create("name", "content");

        //then
        assertThat(request.getHistory()).hasSize(1);
        StateChange entry = request.getHistory().getFirst();
        assertThat(entry.getFromState()).isNull();
        assertThat(entry.getToState()).isEqualTo(CREATED);
        assertThat(entry.getEvent()).isEqualTo(CREATE);
        assertThat(entry.getOccurredAt()).isNotNull();
    }

    @Test
    @DisplayName("a transition appends a history entry with the states and the event")
    void transitionIsRecorded() {
        //given
        Request request = aRequestIn(CREATED);

        //when
        request.verify();

        //then
        assertThat(request.getHistory()).hasSize(2);
        StateChange entry = request.getHistory().getLast();
        assertThat(entry.getFromState()).isEqualTo(CREATED);
        assertThat(entry.getToState()).isEqualTo(VERIFIED);
        assertThat(entry.getEvent()).isEqualTo(VERIFY);
        assertThat(entry.getReason()).isNull();
    }

    @Test
    @DisplayName("a rejection records its reason in the history")
    void rejectionReasonIsRecorded() {
        //given
        Request request = aRequestIn(VERIFIED);

        //when
        request.reject("incomplete");

        //then
        assertThat(request.getHistory().getLast().getReason()).isEqualTo("incomplete");
    }

    @Test
    @DisplayName("a refused transition leaves the history unchanged")
    void refusedTransitionIsNotRecorded() {
        //given
        Request request = aRequestIn(CREATED);

        //when
        catchThrowable(() -> request.publish(1L));

        //then
        assertThat(request.getHistory()).hasSize(1);
    }


}
