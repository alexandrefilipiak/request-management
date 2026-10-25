package io.github.alexandrefilipiak.requestmanagement.core.domain;

import io.github.alexandrefilipiak.requestmanagement.core.domain.InvalidTransitionException;
import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestEvent;
import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState;
import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestStateMachine;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RequestStateMachineTest {

    @ParameterizedTest
    @CsvSource({
      "CREATED,  VERIFY,  VERIFIED",
      "CREATED,  DELETE,  DELETED",
      "VERIFIED, ACCEPT,  ACCEPTED",
      "VERIFIED, REJECT,  REJECTED",
      "ACCEPTED, REJECT,  REJECTED",
      "ACCEPTED, PUBLISH, PUBLISHED"
    })
    @DisplayName("Test all the transitions of the state machine, happy case")
    public void testTransitionsHappyCase(RequestState initialState, RequestEvent event, RequestState targetState) {
        assertThat(RequestStateMachine.next(initialState, event)).isEqualTo(targetState);
    }

    @Test
    @DisplayName("Test a bad transition")
    public void testBadTransition() {
        //given
        RequestState state = RequestState.CREATED;
        RequestEvent event = RequestEvent.PUBLISH;

        //when
        ThrowableAssert.ThrowingCallable callable = () -> RequestStateMachine.next(state, event);

        //then
        assertThatThrownBy(callable)
                .isInstanceOf(InvalidTransitionException.class)
                .hasMessageContaining("CREATED")
                .hasMessageContaining("PUBLISH");
    }

}
