package io.github.jebl01.babaco.matching;

import static io.github.jebl01.babaco.matching.MatchingConsumer._case;
import static io.github.jebl01.babaco.matching.MatchingConsumer.match_;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jebl01.babaco.Either;
import io.github.jebl01.babaco.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

public class MatchingConsumerTest {
    private Consumer<Exception> getExceptionConsumer(final List<String> catches) {
        return MatchingConsumer.match_(
                MatchingConsumer._case(IOException.class, e -> catches.add("io")),
                MatchingConsumer._case(IllegalArgumentException.class, e -> catches.add("illegal")),
                MatchingConsumer._case(RuntimeException.class, e -> catches.add("runtime")),
                MatchingConsumer._default(e -> catches.add("default")));
    }

    @Test
    public void willInvokeDefaultIfNoMatch() {
        final List<String> catches = new ArrayList<>();
        getExceptionConsumer(catches)
                .andThen(e -> catches.add("always"))
                .accept(new ClassNotFoundException());
        assertEquals(2, catches.size());
        assertEquals("default", catches.get(0));
        assertEquals("always", catches.get(1));
    }

    @Test
    public void canMatchUsingOnlyPredicate() {
        final List<String> recorder = mock(List.class);
        final Consumer<Tuple.Tuple2<String, String>> f = MatchingConsumer.match_(
                MatchingConsumer._case(s -> s.v1.startsWith("a"), s -> recorder.add(s.v1 + s.v2 + ":first")),
                MatchingConsumer._case(s -> s.v1.startsWith("b"), s -> recorder.add(s.v1 + s.v2 + ":second")),
                MatchingConsumer._case(s -> s.v1.startsWith("c"), s -> recorder.add(s.v1 + s.v2 + ":third")),
                MatchingConsumer._default(s -> recorder.add(s.v1 + s.v2 + ":default"))
        );
        f.accept(Tuple.of("a_", "test"));
        f.accept(Tuple.of("b_", "test"));
        f.accept(Tuple.of("c_", "test"));
        f.accept(Tuple.of("no_", "match"));
        verify(recorder).add("a_test:first");
        verify(recorder).add("b_test:second");
        verify(recorder).add("c_test:third");
        verify(recorder).add("no_match:default");
    }

    @Test
    public void canMatchOneCase() {
        final List<String> catches = new ArrayList<>();
        getExceptionConsumer(catches)
                .andThen(e -> catches.add("always"))
                .accept(new IOException());
        assertEquals(2, catches.size());
        assertEquals("io", catches.get(0));
        assertEquals("always", catches.get(1));
    }

    @Test
    public void canMatchMultipleCases() {
        final List<String> catches = new ArrayList<>();
        getExceptionConsumer(catches)
                .andThen(e -> catches.add("always"))
                .accept(new IllegalArgumentException());
        assertEquals(3, catches.size());
        assertEquals("illegal", catches.get(0));
        assertEquals("runtime", catches.get(1));
        assertEquals("always", catches.get(2));
    }

    @Test
    public void canMatchEitherOnType() {
        List<String> recorder = mock(List.class);
        Either<String, String> rightStringA = Either.right("A");
        Either<String, String> leftStringA = Either.left("A");
        Either<String, Integer> rightInt1 = Either.right(1);
        Either<Integer, Integer> leftInt1 = Either.left(1);
        Consumer<Either> consumer = MatchingConsumer.match_(
                MatchingConsumer._case(Either.right(String.class), s -> recorder.add("right string " + s)),
                MatchingConsumer._case(Either.left(String.class), s -> recorder.add("left string " + s)),
                MatchingConsumer._case(Either.right(Integer.class), i -> recorder.add("right int " + i)),
                MatchingConsumer._case(Either.left(Integer.class), i -> recorder.add("left int " + i)),
                MatchingConsumer._default(s -> recorder.add(s + "no match"))
        );
        consumer.accept(rightStringA);
        consumer.accept(leftStringA);
        consumer.accept(rightInt1);
        consumer.accept(leftInt1);
        verify(recorder, times(4)).add(anyString());
        verify(recorder).add("right string A");
        verify(recorder).add("left string A");
        verify(recorder).add("right int 1");
        verify(recorder).add("left int 1");
    }

    @Test
    public void canMatchEitherWithPredicate() {
        List<String> recorder = mock(List.class);
        Either<String, String> rightStringA = Either.right("A");
        Either<String, String> rightStringB = Either.right("B");
        Either<String, String> leftStringA = Either.left("A");
        Either<String, String> leftStringB = Either.left("B");
        Either<String, Integer> rightInt1 = Either.right(1);
        Either<String, Integer> rightInt2 = Either.right(2);
        Either<Integer, Integer> leftInt1 = Either.left(1);
        Either<Integer, Integer> leftInt2 = Either.left(2);
        Consumer<Either> consumer = MatchingConsumer.match_(
                MatchingConsumer._case(Either.right(String.class), "A"::equals, s -> recorder.add("right string A")),
                MatchingConsumer._case(Either.right(String.class), "B"::equals, s -> recorder.add("right string B")),
                MatchingConsumer._case(Either.left(String.class), "A"::equals, s -> recorder.add("left string A")),
                MatchingConsumer._case(Either.left(String.class), "B"::equals, s -> recorder.add("left string B")),
                MatchingConsumer._case(Either.right(Integer.class), i -> i == 1, i -> recorder.add("right int 1")),
                MatchingConsumer._case(Either.right(Integer.class), i -> i == 2, i -> recorder.add("right int 2")),
                MatchingConsumer._case(Either.left(Integer.class), i -> i == 1, i -> recorder.add("left int 1")),
                MatchingConsumer._case(Either.left(Integer.class), i -> i == 2, i -> recorder.add("left int 2")),
                MatchingConsumer._default(s -> recorder.add(s + "no match"))
        );
        consumer.accept(rightStringA);
        consumer.accept(rightStringB);
        consumer.accept(leftStringA);
        consumer.accept(leftStringB);
        consumer.accept(rightInt1);
        consumer.accept(rightInt2);
        consumer.accept(leftInt1);
        consumer.accept(leftInt2);
        verify(recorder, times(8)).add(anyString());
        verify(recorder).add("right string A");
        verify(recorder).add("right string B");
        verify(recorder).add("left string A");
        verify(recorder).add("left string B");
        verify(recorder).add("right int 1");
        verify(recorder).add("right int 2");
        verify(recorder).add("left int 1");
        verify(recorder).add("left int 2");
    }
}
