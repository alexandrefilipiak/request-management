package io.github.alexandrefilipiak.requestmanagement.infrastructure.persistence;

import io.github.alexandrefilipiak.requestmanagement.core.domain.Request;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.RequestRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaRequestRepository extends JpaRepository<Request, Long>, RequestRepository {
    @Override
    @Query(value = "SELECT nextval('publication_number_seq')", nativeQuery = true)
    long nextPublicationNumber();
}
