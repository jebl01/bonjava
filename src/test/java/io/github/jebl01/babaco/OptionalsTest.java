package io.github.jebl01.babaco;

import static io.github.jebl01.babaco.Optionals.flatMap2;
import static io.github.jebl01.babaco.Optionals.ifPresent;
import static io.github.jebl01.babaco.Optionals.ifPresentOrElse;
import static io.github.jebl01.babaco.Optionals.map2;
import static io.github.jebl01.babaco.Optionals.map2ToEither;
import static io.github.jebl01.babaco.Optionals.mapToEither;
import static io.github.jebl01.babaco.Optionals.thisOrThat;
import static io.github.jebl01.babaco.Optionals.toStream;
import static io.github.jebl01.babaco.Optionals.toTuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jesblo on 15-08-19.
 */
public class OptionalsTest {
    @Test
    public void map2test() {
        Optional<String> ot = Optional.empty();
        Optional<String> ou = Optional.empty();
        assertEquals(Optional.empty(), map2((String t, String u) -> t + ":" + u).apply(ot, ou));
        ot = Optional.of("left");
        assertEquals(Optional.empty(), map2((String t, String u) -> t + ":" + u).apply(ot, ou));
        ou = Optional.of("right");
        assertEquals(Optional.of("left:right"), map2((String t, String u) -> t + ":" + u).apply(ot, ou));
    }

    @Test
    public void flatMap2test() {
        Optional<String> ot = Optional.empty();
        Optional<String> ou = Optional.empty();
        assertEquals(Optional.empty(), flatMap2((String t, String u) -> Optional.of(t + ":" + u)).apply(ot, ou));
        ot = Optional.of("left");
        assertEquals(Optional.empty(), flatMap2((String t, String u) -> Optional.of(t + ":" + u)).apply(ot, ou));
        ou = Optional.of("right");
        assertEquals(Optional.of("left:right"),
                flatMap2((String t, String u) -> Optional.of(t + ":" + u)).apply(ot, ou));
    }

    @Test
    public void canMapToStream() {
        Optional<String> empty = Optional.empty();
        Optional<String> withValue = Optional.of("a,b,c");
        Stream<String> result = toStream((String value) -> Stream.of(value.split(","))).apply(empty);
        assertEquals(0, result.count());
        result = toStream((String value) -> Stream.of(value.split(","))).apply(withValue);
        assertEquals(3, result.count());
    }

    @Test
    public void canCreateStreamOfSingleOptional() {
        Optional<String> empty = Optional.empty();
        Optional<String> withValue = Optional.of("a,b,c");
        Stream<String> result = toStream(empty);
        assertEquals(0, result.count());
        result = toStream(withValue);
        assertEquals(1, result.count());
    }

    @Test
    public void canCreateStreamOfMultipleOptionals() {
        Optional<String> empty = Optional.empty();
        Optional<String> empty2 = Optional.empty();
        Optional<String> withValue = Optional.of("a");
        Optional<String> withValue2 = Optional.of("b");
        Stream<String> result = toStream(empty, empty2);
        assertEquals(0, result.count());
        result = toStream(empty, withValue, empty2, withValue2);
        assertEquals(2, result.count());
    }

    @Test
    public void canCreateStreamFromIterableOfOptionals() {
        Iterable<Optional<String>> optionalsEmpty = Collections.emptyList();
        Iterable<Optional<String>> optionals = Arrays.asList(Optional.of("a"), Optional.empty(), Optional.of("b"));
        Stream<String> result = toStream(optionalsEmpty);
        assertEquals(0, result.count());
        result = toStream(optionals);
        assertEquals(2, result.count());
    }

    @Test
    public void testThisOrThat() {
        Optional<String> optEmpty = Optional.empty();
        Optional<String> optWithValueA = Optional.of("A");
        Optional<String> optWithValueB = Optional.of("B");
        Optional<String> result = thisOrThat(optEmpty, () -> optEmpty);
        assertFalse(result.isPresent());
        result = thisOrThat(optEmpty, () -> optWithValueA);
        assertEquals(optWithValueA, result);
        result = thisOrThat(optWithValueA, () -> optEmpty);
        assertEquals(optWithValueA, result);
        result = thisOrThat(optWithValueA, () -> optWithValueB);
        assertEquals(optWithValueA, result);
    }

    @Test
    public void testIfPresent() {
        List<String> collector = new ArrayList<>();
        Optional<String> optEmpty = Optional.empty();
        Optional<String> optWithValue = Optional.of("the value");
        ifPresent(optEmpty, collector::add).ifPresent(value -> fail("a value should not magically spawn"));
        ifPresent(optWithValue, collector::add).orElseThrow(() -> new IllegalStateException("should keep its value"));
        assertEquals(1, collector.size());
        assertTrue(collector.contains("the value"));
    }

    @Test
    public void testIfPresentOrElse() {
        Optional<String> optEmpty = Optional.empty();
        Optional<String> optWithValue = Optional.of("the value");
        ifPresentOrElse(optEmpty, value -> fail(), () -> {
        });
        ifPresentOrElse(optWithValue, value -> {
        }, Assert::fail);
    }

    @Test
    public void canMapOptionalToEither() {
        Optional<String> optEmpty = Optional.empty();
        Optional<String> optWithValueA = Optional.of("A");
        Either<String, String> result = mapToEither((String v) -> Either.right(v), () -> "fail").apply(optEmpty);
        assertTrue(result.isLeft());
        assertEquals("fail", result.getLeft().get());
        result = mapToEither((String v) -> Either.right(v + ":" + v), () -> "fail").apply(optWithValueA);
        assertTrue(result.isRight());
        assertEquals("A:A", result.getRight().get());
    }

    @Test
    public void canMap2OptionalToEither() {
        Optional<String> optEmpty = Optional.empty();
        Optional<String> optWithValueA = Optional.of("A");
        Optional<String> optWithValueB = Optional.of("B");
        Either<String, String> result = map2ToEither((String a, String b) -> Either.right(a + ":" + b),
                () -> "fail").apply(optEmpty, optEmpty);
        assertTrue(result.isLeft());
        assertEquals("fail", result.getLeft().get());
        result = map2ToEither((String a, String b) -> Either.right(a + ":" + b), () -> "fail").apply(optWithValueA,
                optEmpty);
        assertTrue(result.isLeft());
        assertEquals("fail", result.getLeft().get());
        result = map2ToEither((String a, String b) -> Either.right(a + ":" + b), () -> "fail").apply(optEmpty,
                optWithValueB);
        assertTrue(result.isLeft());
        assertEquals("fail", result.getLeft().get());
        result = map2ToEither((String a, String b) -> Either.right(a + ":" + b), () -> "fail").apply(optWithValueA,
                optWithValueB);
        assertTrue(result.isRight());
        assertEquals("A:B", result.getRight().get());
    }

    @Test
    public void toPairTest() {
        Optional<String> optEmpty = Optional.empty();
        Optional<String> optWithValueA = Optional.of("A");
        Optional<String> optWithValueB = Optional.of("B");
        Optional<Tuple.Tuple2<String, String>> result = toTuple(optEmpty, optWithValueA);
        assertFalse(result.isPresent());
        result = toTuple(optWithValueA, optEmpty);
        assertFalse(result.isPresent());
        result = toTuple(optWithValueA, optWithValueB);
        assertTrue(result.isPresent());
        Assert.assertEquals(new Tuple.Tuple2<>("A", "B"), result.get());
    }
}