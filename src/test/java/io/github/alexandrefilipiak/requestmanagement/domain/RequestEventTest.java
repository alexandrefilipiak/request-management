package io.github.alexandrefilipiak.requestmanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestEventTest {

    @ParameterizedTest
    @EnumSource(value = RequestEvent.class, names = {"REJECT", "DELETE"})
    @DisplayName("Test states for which reasonRequired=true")
    public void testRequestEventTrue(RequestEvent event) {
        assertThat(event.isReasonRequired()).isEqualTo(true);
    }

    @ParameterizedTest
    @EnumSource(value = RequestEvent.class, names = {"REJECT", "DELETE"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Test states for which reasonRequired=false")
    public void testRequestEventFalse(RequestEvent event) {
        assertThat(event.isReasonRequired()).isEqualTo(false);
    }
}
