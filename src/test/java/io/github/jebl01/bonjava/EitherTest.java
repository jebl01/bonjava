package io.github.jebl01.bonjava;

import static io.github.jebl01.bonjava.Either.fromNullable;
import static io.github.jebl01.bonjava.Either.fromOptional;
import static io.github.jebl01.bonjava.Either.left;
import static io.github.jebl01.bonjava.Either.right;
import static io.github.jebl01.bonjava.matching.MatchingConsumer.*;
import static io.github.jebl01.bonjava.matching.MatchingConsumer._case;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Test;

import io.github.jebl01.bonjava.matching.MatchingConsumer;

public class EitherTest {
    @Test
    public void leftIsLeft() {
        Either either = left("fail");
        assertFalse(either.isRight());
        assertTrue(either.isLeft());
    }

    @Test
    public void rightIsRight() {
        Either either = right("success");
        assertFalse(either.isLeft());
        assertTrue(either.isRight());
    }

    @Test
    public void fromNullableReturnsLeftIfValueIsNull() {
        String nullString = null;
        Either<String, String> result = fromNullable(nullString, () -> "fail");
        assertTrue(result.isLeft());
        assertEquals("fail", result.getLeft().get());
    }

    @Test
    public void canCreateEiterFromNullableWithValue() {
        String stringWithValue = "test";
        Either<String, String> result = fromNullable(stringWithValue, () -> "fail");
        assertTrue(result.isRight());
        assertEquals("test", result.getRight().get());
    }

    @Test
    public void fromOptionalReturnsLeftIfEmpty() {
        Either<String, String> result = fromOptional(Optional.empty(), () -> "fail");
        assertTrue(result.isLeft());
        assertEquals("fail", result.getLeft().get());
    }

    @Test
    public void canCreateEiterFromOptionalWithValue() {
        Either<String, String> result = fromOptional(Optional.of("test"), () -> "fail");
        assertTrue(result.isRight());
        assertEquals("test", result.getRight().get());
    }

    @Test
    public void willNotMapOverLeftProjectedEither() {
        Either<String, String> either = left("fail");
        Either<String, String> result = either.map(value -> value.toUpperCase());
        assertTrue(result.isLeft());
        assertEquals("fail", result.getLeft().get());
    }

    @Test
    public void willMapOverRightProjectedEither() {
        Either<String, String> either = right("success");
        Either<String, String> result = either.map(value -> value.toUpperCase());
        assertTrue(result.isRight());
        assertEquals("SUCCESS", result.getRight().get());
    }

    @Test
    public void willFlatMapOverRightProjectedEither() {
        Either<String, String> either = right("success");
        Either<String, String> result = either.flatMap(value -> right(value.toUpperCase()));
        assertTrue(result.isRight());
        assertEquals("SUCCESS", result.getRight().get());
    }

    @Test
    public void canReturnLeftWhenFlatmappingOverRight() {
        Either<String, String> either = right("success");
        Either<String, String> result = either.flatMap(value -> left("returning value on left side: " + value));
        assertTrue(result.isLeft());
        assertEquals("returning value on left side: success", result.getLeft().get());
    }

    @Test
    public void canConsumeEither() {
        Either<String, String> eitherRight = right("success");
        Either<String, String> eitherLeft = left("fail");
        eitherRight.consume(l -> fail("should be right"), r -> assertEquals("success", r));
        eitherLeft.consume(l -> assertEquals("fail", l), r -> fail("should be left"));
    }

    @Test
    public void canConsumeOnEither() {
        Either<String, String> either = right("success");
        either.consume(e -> assertTrue(e.isRight()));
        either.consume(e -> assertFalse(e.isLeft()));
    }

    @Test
    public void canConsumeOnEitherAndMatch() {
        Either<String, String> either = right("success");
        either.consume(match_(
                _case(right(String.class), string -> assertEquals("success", string)),
                _default(e -> fail())
        ));
    }

    @Test
    public void testThisOrThatWithRight() {
        Either<String, String> either = right("success");
        Either<String, String> result = Either.thisOrThat(either, () -> right("success2"));
        assertTrue(result.isRight());
        assertEquals("success", result.getRight().get());
    }

    @Test
    public void testThisOrThatWithLeftAndRight() {
        Either<String, String> either = left("fail");
        Either<String, String> result = Either.thisOrThat(either, () -> right("success2"));
        assertTrue(result.isRight());
        assertEquals("success2", result.getRight().get());
    }

    @Test
    public void testThisOrThatWithLeftAndLeft() {
        Either<String, String> either = left("fail");
        Either<String, String> result = Either.thisOrThat(either, () -> left("fail2"));
        assertTrue(result.isLeft());
        assertEquals("fail2", result.getLeft().get());
    }

    @Test(expected = RuntimeException.class)
    public void willRethrowErrors() {
        right("success").map(value -> {
            throw new RuntimeException("fail");
        });
    }

    @Test
    public void canGetRightSide() throws Throwable {
        Either<String, String> either = right("success");
        assertEquals("success", either.getOrThrow(l -> null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void willThrowIfNotRight() throws Throwable {
        Either<String, String> either = left("fail");
        assertEquals("success", either.getOrThrow(IllegalArgumentException::new));
    }
}
