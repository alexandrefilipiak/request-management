package io.github.alexandrefilipiak.requestmanagement.core.domain;

import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestStateTest {

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = {"CREATED", "VERIFIED"})
    @DisplayName("Test states for which contentEditable=true")
    public void testContentEditableTrue(RequestState state) {
        assertThat(state.isContentEditable()).isEqualTo(true);
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = {"CREATED", "VERIFIED"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Test states for which contentEditable=false")
    public void testContentEditableFalse(RequestState state) {
        assertThat(state.isContentEditable()).isEqualTo(false);
    }
}
