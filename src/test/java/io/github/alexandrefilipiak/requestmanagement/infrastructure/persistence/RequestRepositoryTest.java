package io.github.alexandrefilipiak.requestmanagement.infrastructure.persistence;

import io.github.alexandrefilipiak.requestmanagement.core.domain.Request;
import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState;
import io.github.alexandrefilipiak.requestmanagement.core.domain.StateChange;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.ListRequestsQuery;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.RequestPage;
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
import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(fromDb.getName()).isEqualTo("name");
        assertThat(fromDb.getContent()).isEqualTo("content");
        assertThat(fromDb.getPublicationNumber()).isNull();
        assertThat(fromDb.getState()).isEqualTo(RequestState.CREATED);
        assertThat(fromDb.getReason()).isNull();
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

        assertThat(found.getHistory()).extracting(StateChange::getToState)
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
        assertThat(storedState).isEqualTo("CREATED");
    }

    @Test
    @DisplayName("publication numbers are increasing")
    void publicationNumbersIncreasing() {
        //when
        long num1 = repository.nextPublicationNumber();
        long num2 = repository.nextPublicationNumber();

        //then
        assertThat(num1).isLessThan(num2);
    }

    @Test
    @DisplayName("search filters by name (partial, case-insensitive) and by state")
    void searchFilters() {
        //given
        repository.save(Request.create("Budget 2026", "c"));
        repository.save(Request.create("budget review", "c"));
        Request verified = Request.create("Budget approval", "c");
        verified.verify();
        repository.save(verified);
        repository.save(Request.create("Hiring plan", "c"));
        entityManager.flush();
        entityManager.clear();

        //when
        RequestPage byName = repository.search(new ListRequestsQuery("budget", null, 0, 10));
        RequestPage byNameAndState = repository.search(new ListRequestsQuery("budget", VERIFIED, 0, 10));

        //then
        assertThat(byName.items()).extracting(Request::getName)
                .containsExactlyInAnyOrder("Budget 2026", "budget review", "Budget approval");
        assertThat(byNameAndState.items()).extracting(Request::getName)
                .containsExactly("Budget approval");
    }

    @Test
    @DisplayName("search pages the results and reports the total")
    void searchPages() {
        //given
        for (int i = 0; i < 12; i++) {
            repository.save(Request.create("Paged " + i, "c"));
        }
        entityManager.flush();
        entityManager.clear();

        //when
        RequestPage first = repository.search(new ListRequestsQuery("Paged", null, 0, 10));
        RequestPage second = repository.search(new ListRequestsQuery("Paged", null, 1, 10));

        //then
        assertThat(first.items()).hasSize(10);
        assertThat(second.items()).hasSize(2);
        assertThat(first.totalElements()).isEqualTo(12);
        assertThat(first.totalPages()).isEqualTo(2);
    }
}
