package io.github.alexandrefilipiak.requestmanagement.infrastructure.persistence;

import io.github.alexandrefilipiak.requestmanagement.core.domain.Request;
import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState;
import org.springframework.data.jpa.domain.Specification;

final class RequestSpecifications {

    private RequestSpecifications() {
    }

    static Specification<Request> nameContains(String name) {
        return (root, query, cb) -> name == null ? null
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    static Specification<Request> hasState(RequestState state) {
        return (root, query, cb) -> state == null ? null
                : cb.equal(root.get("state"), state);
    }
}