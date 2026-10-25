package io.github.alexandrefilipiak.requestmanagement.infrastructure.persistence;

import io.github.alexandrefilipiak.requestmanagement.core.domain.Request;
import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState;
import io.github.alexandrefilipiak.requestmanagement.core.domain.StateChange;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.RequestRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import io.github.alexandrefilipiak.requestmanagement.TestcontainersConfiguration;

import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
public class RequestRepositoryTest {

    @Autowired
    private RequestRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("records get saved correctly")
    void roundTrip() {
        //given
        Request saved = repository.save(Request.create("name","content"));
        entityManager.flush();
        entityManager.clear();

        //when
        Request fromDb = repository.findById(saved.getId()).orElseThrow();

        //then
        Assertions.assertThat(fromDb.getName()).isEqualTo("name");
        Assertions.assertThat(fromDb.getContent()).isEqualTo("content");
        Assertions.assertThat(fromDb.getPublicationNumber()).isNull();
        Assertions.assertThat(fromDb.getState()).isEqualTo(RequestState.CREATED);
        Assertions.assertThat(fromDb.getReason()).isNull();
    }

    @Test
    @DisplayName("the history is saved with the request and read back in order")
    void historyIsPersisted() {
        Request request = Request.create("name", "content");
        request.verify();
        Long id = repository.save(request).getId();
        entityManager.flush();
        entityManager.clear();

        Request found = repository.findById(id).orElseThrow();

        Assertions.assertThat(found.getHistory()).extracting(StateChange::getToState)
                .containsExactly(CREATED, VERIFIED);
    }

    @Test
    @DisplayName("record's state is stored as name, not ordinal")
    void stateStoredAsName() {
        //given
        Request saved = repository.save(Request.create("name", "content"));
        entityManager.flush();
        entityManager.clear();

        //when
        String storedState = (String) entityManager.getEntityManager()
                .createNativeQuery("select state from requests where id = :id")
                .setParameter("id", saved.getId())
                .getSingleResult();

        //then
        Assertions.assertThat(storedState).isEqualTo("CREATED");
    }

    @Test
    @DisplayName("publication numbers are increasing")
    void publicationNumbersIncreasing() {
        //when
        long num1 = repository.nextPublicationNumber();
        long num2 = repository.nextPublicationNumber();

        //then
        Assertions.assertThat(num1).isLessThan(num2);
    }

}
