package io.github.alexandrefilipiak.requestmanagement.core.domain;

import org.springframework.test.util.ReflectionTestUtils;

public final class RequestFixtures {

    private RequestFixtures() {}

    public static Request aRequestIn(RequestState state) {
        Request request = Request.create("name", "content");

        switch (state) {
            case VERIFIED -> { request.verify();
            }
            case ACCEPTED -> { request.verify(); request.accept(); }
            case PUBLISHED -> { request.verify(); request.accept(); request.publish(1L); }
            case REJECTED -> { request.verify(); request.reject("reason"); }
            case DELETED -> request.delete("reason");
            default -> {}
        }
        return request;
    }

    public static Request aRequestIn(RequestState state, long id) {
        Request request = aRequestIn(state);
        ReflectionTestUtils.setField(request, "id", id);
        return request;
    }
}