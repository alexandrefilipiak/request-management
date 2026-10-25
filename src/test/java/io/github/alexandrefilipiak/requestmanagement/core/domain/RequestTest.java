package io.github.alexandrefilipiak.requestmanagement.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestFixtures.aRequestIn;
import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class RequestTest {

    @Test
    @DisplayName("a new request starts in CREATED")
    void startsCreated() {
        //given
        String name = "name";
        String content = "content";

        //when
        Request request = Request.create(name, content);

        //then
        assertThat(request.getState()).isEqualTo(CREATED);
    }

    @Test
    @DisplayName("rejecting a verified request stores the reason")
    void rejectStoresReason() {
        //given
        Request request = aRequestIn(VERIFIED);

        //when
        request.reject("incomplete");

        //then
        assertThat(request.getState()).isEqualTo(REJECTED);
        assertThat(request.getReason()).isEqualTo("incomplete");
    }

    @Test
    @DisplayName("rejecting without a reason fails and leaves the state unchanged")
    void rejectWithoutReason() {
        //given
        Request request = aRequestIn(VERIFIED);

        //when
        Throwable thrown = catchThrowable(() -> request.reject("  "));

        //then
        assertThat(thrown).isInstanceOf(ReasonRequiredException.class);
        assertThat(request.getState()).isEqualTo(VERIFIED);
    }

    @Test
    @DisplayName("content cannot be edited once accepted")
    void contentNotEditableWhenAccepted() {
        //given
        Request request = aRequestIn(ACCEPTED);

        //when
        Throwable thrown = catchThrowable(() -> request.updateContent("new"));

        //then
        assertThat(thrown).isInstanceOf(ContentNotEditableException.class);
        assertThat(request.getContent()).isEqualTo("content");
    }


}
