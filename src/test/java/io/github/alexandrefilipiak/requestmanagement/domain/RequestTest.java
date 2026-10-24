package io.github.alexandrefilipiak.requestmanagement.domain;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.alexandrefilipiak.requestmanagement.domain.RequestState.*;
import static org.assertj.core.api.Assertions.*;

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

    private static Request aRequestIn(RequestState state) {
        Request request = Request.create("name", "content");
        switch (state) {
            case VERIFIED -> request.verify();
            case ACCEPTED -> { request.verify(); request.accept(); }
            case PUBLISHED -> { request.verify(); request.accept(); request.publish(1L); }
            case REJECTED -> { request.verify(); request.reject("reason"); }
            case DELETED -> request.delete("reason");
            default -> {}
        }
        return request;
    }
}
