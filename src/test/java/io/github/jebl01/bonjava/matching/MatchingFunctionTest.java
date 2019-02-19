package io.github.jebl01.bonjava.matching;

import static io.github.jebl01.bonjava.matching.MatchingFunction.*;
import static io.github.jebl01.bonjava.matching.MatchingFunction._case;
import static junit.framework.TestCase.assertEquals;

import io.github.jebl01.bonjava.Either;
import io.github.jebl01.bonjava.Tuple;

import java.io.IOException;
import java.util.function.Function;

import org.junit.Test;

public class MatchingFunctionTest {
    private Function<Throwable, String> getExceptionConsumerWithDefault() {
        return match_(
                _case(IOException.class, e -> "io"),
                _case(RuntimeException.class, e -> "runtime"),
                _case(IllegalArgumentException.class, e -> "illegal"),
                _default(e -> "default: " + e.getClass().getSimpleName()));
    }

    private Function<Throwable, String> getExceptionConsumerNoDefault() {
        return match_(
                _case(IOException.class, e -> "io"),
                _case(RuntimeException.class, e -> "runtime"),
                _case(IllegalArgumentException.class, e -> "illegal"));
    }

    @Test
    public void willInvokeDefaultIfNoMatch() {
        assertEquals("default: IllegalAccessError", getExceptionConsumerWithDefault().apply(new IllegalAccessError()));
    }

    @Test
    public void willMatchExact() {
        assertEquals("io", getExceptionConsumerWithDefault().apply(new IOException()));
    }

    @Test
    public void willMatchOnSuperType() {
        assertEquals("runtime", getExceptionConsumerWithDefault().apply(new IllegalArgumentException()));
        assertEquals("runtime", getExceptionConsumerWithDefault().apply(new ClassCastException()));
    }

    @Test(expected = MatchingException.class)
    public void willThrowExceptionIfNoMatch() {
        assertEquals("io", getExceptionConsumerNoDefault().apply(new IllegalAccessError()));
    }

    @Test
    public void canMatchUsingOnlyPredicate() {
        final Function<Tuple.Tuple2<String, String>, String> f = match_(
                _case(s -> s.v1.startsWith("a"), s -> s.v1 + s.v2 + ":first"),
                _case(s -> s.v1.startsWith("b"), s -> s.v1 + s.v2 + ":second"),
                _case(s -> s.v1.startsWith("c"), s -> s.v1 + s.v2 + ":third"),
                _default(s -> s.v1 + s.v2 + ":default")
        );
        assertEquals("a_test:first", f.apply(Tuple.of("a_", "test")));
        assertEquals("b_test:second", f.apply(Tuple.of("b_", "test")));
        assertEquals("c_test:third", f.apply(Tuple.of("c_", "test")));
        assertEquals("no_match:default", f.apply(Tuple.of("no_", "match")));
    }

    @Test
    public void canMatchUsingPredicate() {
        final Function<String, String> f = match_(
                _case(String.class, s -> s.startsWith("a"), s -> s + ":first"),
                _case(String.class, s -> s.startsWith("b"), s -> s + ":second"),
                _case(String.class, s -> s.startsWith("c"), s -> s + ":third"),
                _default(s -> s + ":default")
        );
        assertEquals("a_test:first", f.apply("a_test"));
        assertEquals("b_test:second", f.apply("b_test"));
        assertEquals("c_test:third", f.apply("c_test"));
        assertEquals("no_match:default", f.apply("no_match"));
    }

    @Test
    public void canMatchNumberUsingPredicate() {
        final Function<Number, String> matchingFunction = match_(
                _case(Integer.class, i -> i == 1, i -> i + ":first_int"),
                _case(Integer.class, i -> i == 2, i -> i + ":second_int"),
                _case(Long.class, l -> l == 1, l -> l + ":first_long"),
                _case(Long.class, l -> l == 2, l -> l + ":second_long"),
                _case(Float.class, f -> f.compareTo(1.1f) == 0, f -> f + ":first_float"),
                _case(Float.class, f -> f.compareTo(2.2f) == 0, l -> l + ":second_float"),
                _case(Double.class, d -> d.compareTo(1.1d) == 0, d -> d + ":first_double"),
                _case(Double.class, d -> d.compareTo(2.2d) == 0, d -> d + ":second_double"),
                _default(n -> n + ":default")
        );
        assertEquals("1:first_int", matchingFunction.apply(1));
        assertEquals("2:second_int", matchingFunction.apply(2));
        assertEquals("1:first_long", matchingFunction.apply(1L));
        assertEquals("2:second_long", matchingFunction.apply(2L));
        assertEquals("1.1:first_float", matchingFunction.apply(1.1f));
        assertEquals("2.2:second_float", matchingFunction.apply(2.2f));
        assertEquals("1.1:first_double", matchingFunction.apply(1.1d));
        assertEquals("2.2:second_double", matchingFunction.apply(2.2d));
        assertEquals("1:default", matchingFunction.apply((short) 1));
    }

    @Test
    public void canMatchOnObject() {
        final Function<Object, String> f = match_(
                _case(String.class, s -> "string with value: " + s),
                _case(Integer.class, i -> "int with value: " + i),
                _case(Exception.class, e -> "exception with message: " + e.getMessage()),
                _default(s -> "unknown")
        );
        assertEquals("string with value: test", f.apply("test"));
        assertEquals("int with value: 42", f.apply(42));
        assertEquals("exception with message: hello", f.apply(new IOException("hello")));
        assertEquals("unknown", f.apply(Either.left("test")));
    }

    @Test
    public void canMatchEitherOnType() {
        Either<String, String> rightStringA = Either.right("A");
        Either<String, String> leftStringA = Either.left("A");
        Either<String, Integer> rightInt1 = Either.right(1);
        Either<Integer, Integer> leftInt1 = Either.left(1);
        final Function<Either, String> f = match_(
                _case(Either.right(String.class), s -> "right string " + s),
                _case(Either.left(String.class), s -> "left string " + s),
                _case(Either.right(Integer.class), i -> "right int " + i),
                _case(Either.left(Integer.class), i -> "left int " + i),
                _default(s -> s + "no match")
        );
        assertEquals("right string A", f.apply(rightStringA));
        assertEquals("left string A", f.apply(leftStringA));
        assertEquals("right int 1", f.apply(rightInt1));
        assertEquals("left int 1", f.apply(leftInt1));
    }

    @Test
    public void canMatchEitherWithPredicate() {
        Either<String, String> rightStringA = Either.right("A");
        Either<String, String> rightStringB = Either.right("B");
        Either<String, String> leftStringA = Either.left("A");
        Either<String, String> leftStringB = Either.left("B");
        Either<String, Integer> rightInt1 = Either.right(1);
        Either<String, Integer> rightInt2 = Either.right(2);
        Either<Integer, Integer> leftInt1 = Either.left(1);
        Either<Integer, Integer> leftInt2 = Either.left(2);
        final Function<Either, String> f = match_(
                _case(Either.right(String.class), "A"::equals, s -> "right string A"),
                _case(Either.right(String.class), "B"::equals, s -> "right string B"),
                _case(Either.left(String.class), "A"::equals, s -> "left string A"),
                _case(Either.left(String.class), "B"::equals, s -> "left string B"),
                _case(Either.right(Integer.class), i -> i == 1, i -> "right int 1"),
                _case(Either.right(Integer.class), i -> i == 2, i -> "right int 2"),
                _case(Either.left(Integer.class), i -> i == 1, i -> "left int 1"),
                _case(Either.left(Integer.class), i -> i == 2, i -> "left int 2"),
                _default(s -> s + "no match")
        );
        assertEquals("right string A", f.apply(rightStringA));
        assertEquals("right string B", f.apply(rightStringB));
        assertEquals("left string A", f.apply(leftStringA));
        assertEquals("left string B", f.apply(leftStringB));
        assertEquals("right int 1", f.apply(rightInt1));
        assertEquals("right int 2", f.apply(rightInt2));
        assertEquals("left int 1", f.apply(leftInt1));
        assertEquals("left int 2", f.apply(leftInt2));
    }
}
