package io.github.alexandrefilipiak.requestmanagement.core.usecases;

import io.github.alexandrefilipiak.requestmanagement.core.domain.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        //given
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

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
}