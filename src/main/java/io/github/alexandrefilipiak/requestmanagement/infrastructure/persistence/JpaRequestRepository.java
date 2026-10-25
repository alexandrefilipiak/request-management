package io.github.alexandrefilipiak.requestmanagement.infrastructure.persistence;

import io.github.alexandrefilipiak.requestmanagement.core.domain.Request;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.ListRequestsQuery;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.RequestPage;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.RequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface JpaRequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request>, RequestRepository {

    @Override
    @Query(value = "SELECT nextval('publication_number_seq')", nativeQuery = true)
    long nextPublicationNumber();

    @Override
    default RequestPage search(ListRequestsQuery query) {
        Specification<Request> spec = Specification.<Request>unrestricted()
                .and(RequestSpecifications.nameContains(query.name()))
                .and(RequestSpecifications.hasState(query.state()));
        Page<Request> page = findAll(spec, PageRequest.of(query.page(), query.size(), Sort.by("id").descending()));
        return new RequestPage(page.getContent(), query.page(), query.size(), page.getTotalElements());
    }
}
