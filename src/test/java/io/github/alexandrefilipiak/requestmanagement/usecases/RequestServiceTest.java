package io.github.alexandrefilipiak.requestmanagement.usecases;

import io.github.alexandrefilipiak.requestmanagement.domain.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static io.github.alexandrefilipiak.requestmanagement.domain.RequestState.PUBLISHED;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    RequestRepository repository;

    @InjectMocks
    RequestService service;

    @Test
    @DisplayName("publishing assigns the next publication number")
    void publishAssignsNumber() {
        //given
        Request request = anAcceptedRequest();
        when(repository.findById(42L)).thenReturn(Optional.of(request));
        when(repository.nextPublicationNumber()).thenReturn(7L);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        Request result = service.publish(42L);

        //then
        assertThat(result.getState()).isEqualTo(PUBLISHED);
        assertThat(result.getPublicationNumber()).isEqualTo(7L);
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
        verify(repository, never()).save(any());
    }

    private static Request anAcceptedRequest() {
        Request request = Request.create("name","content");
        request.verify();
        request.accept();

        return request;
    }
}
