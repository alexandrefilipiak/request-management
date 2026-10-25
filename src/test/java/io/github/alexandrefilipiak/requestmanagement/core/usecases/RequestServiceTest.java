package io.github.alexandrefilipiak.requestmanagement.core.usecases;

import io.github.alexandrefilipiak.requestmanagement.core.domain.Request;
import io.github.alexandrefilipiak.requestmanagement.core.domain.StateChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestFixtures.aRequestIn;
import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    RequestRepository repository;

    @InjectMocks
    RequestService service;

    @BeforeEach
    void saveReturnsItsArgument() {
        lenient().when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("creating saves a new request in CREATED")
    void createSavesRequest() {
        //when
        service.create(new CreateRequestCommand("name", "content"));

        //then
        ArgumentCaptor<Request> saved = ArgumentCaptor.forClass(Request.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("name");
        assertThat(saved.getValue().getState()).isEqualTo(CREATED);
    }

    @Test
    @DisplayName("verifying loads the request and applies the transition")
    void verifyAppliesTransition() {
        //given
        Request request = aRequestIn(CREATED);
        when(repository.findById(42L)).thenReturn(Optional.of(request));

        //when
        service.verify(42L);

        //then
        assertThat(request.getState()).isEqualTo(VERIFIED);
    }

    @Test
    @DisplayName("publishing assigns and returns the next publication number")
    void publishAssignsNumber() {
        //given
        Request request = aRequestIn(ACCEPTED);
        when(repository.findById(42L)).thenReturn(Optional.of(request));
        //when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repository.nextPublicationNumber()).thenReturn(7L);

        //when
        Long number = service.publish(42L);

        //then
        assertThat(number).isEqualTo(7L);
        assertThat(request.getState()).isEqualTo(PUBLISHED);
        assertThat(request.getPublicationNumber()).isEqualTo(7L);
    }

    @Test
    @DisplayName("an unknown id fails with RequestNotFoundException")
    void unknownId() {
        //given
        when(repository.findById(42L)).thenReturn(Optional.empty());

        //when
        Throwable thrown = catchThrowable(() -> service.verify(42L));

        //then
        assertThat(thrown).isInstanceOf(RequestNotFoundException.class);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("getting returns the loaded request")
    void getReturnsRequest() {
        //given
        Request request = aRequestIn(VERIFIED);
        when(repository.findById(42L)).thenReturn(Optional.of(request));

        //when
        Request result = service.get(42L);

        //then
        assertThat(result).isSameAs(request);
    }

    @Test
    @DisplayName("getting the history returns the request's entries oldest first")
    void getHistoryReturnsEntries() {
        //given
        Request request = aRequestIn(VERIFIED);
        when(repository.findById(42L)).thenReturn(Optional.of(request));

        //when
        List<StateChange> history = service.getHistory(42L);

        //then
        assertThat(history).extracting(StateChange::getToState).containsExactly(CREATED, VERIFIED);
    }

    @Test
    @DisplayName("updating content replaces the body and saves")
    void updateContentReplacesBody() {
        //given
        Request request = aRequestIn(CREATED);
        when(repository.findById(42L)).thenReturn(Optional.of(request));

        //when
        service.updateContent(42L, new UpdateContentCommand("new content"));

        //then
        assertThat(request.getContent()).isEqualTo("new content");
        verify(repository).save(request);
    }

    @Test
    @DisplayName("accepting applies the transition")
    void acceptAppliesTransition() {
        //given
        Request request = aRequestIn(VERIFIED);
        when(repository.findById(42L)).thenReturn(Optional.of(request));

        //when
        service.accept(42L);

        //then
        assertThat(request.getState()).isEqualTo(ACCEPTED);
        verify(repository).save(request);
    }

    @Test
    @DisplayName("rejecting applies the transition with the reason")
    void rejectAppliesTransition() {
        //given
        Request request = aRequestIn(VERIFIED);
        when(repository.findById(42L)).thenReturn(Optional.of(request));

        //when
        service.reject(42L, new RejectRequestCommand("incomplete"));

        //then
        assertThat(request.getState()).isEqualTo(REJECTED);
        assertThat(request.getReason()).isEqualTo("incomplete");
        verify(repository).save(request);
    }

    @Test
    @DisplayName("deleting applies the transition with the reason")
    void deleteAppliesTransition() {
        //given
        Request request = aRequestIn(CREATED);
        when(repository.findById(42L)).thenReturn(Optional.of(request));

        //when
        service.delete(42L, new DeleteRequestCommand("duplicate"));

        //then
        assertThat(request.getState()).isEqualTo(DELETED);
        assertThat(request.getReason()).isEqualTo("duplicate");
        verify(repository).save(request);
    }

    @Test
    @DisplayName("listing passes the query to the repository and returns its page")
    void listCallsRepository() {
        //given
        ListRequestsQuery query = new ListRequestsQuery("budget", VERIFIED, 0, 10);
        RequestPage page = new RequestPage(List.of(aRequestIn(VERIFIED)), 0, 10, 1);
        when(repository.search(query)).thenReturn(page);

        //when
        RequestPage result = service.list(query);

        //then
        assertThat(result).isSameAs(page);
    }
}