package io.github.jebl01.babaco;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

/**
 * Created by jesblo on 15-08-19.
 */
public class SideEffectsTest {

    @Test
    public void willInvokeSideEffect() {
        final List<String> sideEffectRecorder = new ArrayList<>();
        Optional<String> result = Optional.of("test").map(
                SideEffects.withSideEffect(
                        String::toUpperCase,
                        sideEffectRecorder::add));
        assertEquals("TEST", result.get());
        assertEquals(1, sideEffectRecorder.size());
        assertEquals("TEST", sideEffectRecorder.get(0));
    }

    @Test
    public void willInvokeSideEffect2() {
        final List<String> sideEffectRecorder = new ArrayList<>();
        Optional<String> value1 = Optional.of("1");
        Optional<String> value2 = Optional.of("2");
        Optional<String> result = Optionals.map2(
                SideEffects.withSideEffect2(
                        (String v1, String v2) -> v1 + ":" + v2,
                        sideEffectRecorder::add)).apply(value1, value2);
        assertEquals("1:2", result.get());
        assertEquals(1, sideEffectRecorder.size());
        assertEquals("1:2", sideEffectRecorder.get(0));
    }

    @Test
    public void willInvokeSideEffectOnSupplier() {
        final List<String> sideEffectRecorder = new ArrayList<>();
        final String result = SideEffects.withSideEffectSupply(
                () -> "result",
                sideEffectRecorder::add).get();
        assertEquals("result", result);
        assertEquals(1, sideEffectRecorder.size());
        assertEquals("result", sideEffectRecorder.get(0));
    }
}
