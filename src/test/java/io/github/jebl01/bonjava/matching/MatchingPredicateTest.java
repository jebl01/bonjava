package io.github.jebl01.bonjava.matching;

import static io.github.jebl01.bonjava.matching.MatchingPredicate._case;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import io.github.jebl01.bonjava.Either;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.Test;

public class MatchingPredicateTest {
    private Predicate<Throwable> getExceptionPredicateNoDefault() {
        return MatchingPredicate.match_(
                MatchingPredicate._case(IOException.class, e -> true),
                MatchingPredicate._case(IllegalArgumentException.class, e -> true),
                MatchingPredicate._case(RuntimeException.class, e -> false));
    }

    @Test
    public void willInvokeDefaultIfNoMatch() {
        assertTrue(Optional.of("alpha").filter(MatchingPredicate.match_(
                MatchingPredicate._case(String.class, "beta"::equals, value -> false),
                MatchingPredicate._case(String.class, "gamma"::equals, value -> false),
                MatchingPredicate._default(value -> true))
        ).isPresent());
    }

    @Test
    public void willMatchExact() {
        assertTrue(getExceptionPredicateNoDefault().test(new IOException()));
        assertTrue(getExceptionPredicateNoDefault().test(new IllegalArgumentException()));
        assertFalse(getExceptionPredicateNoDefault().test(new RuntimeException()));
    }

    @Test(expected = MatchingPredicate.MatchingException.class)
    public void willThrowExceptionIfNoMatch() {
        assertTrue(getExceptionPredicateNoDefault().test(new IllegalAccessError()));
    }

    @Test
    public void willMathOnSuperType() {
        assertTrue(getExceptionPredicateNoDefault().test(new AccessDeniedException("")));
    }

    @Test
    public void canMatchUsingOnlyPredicate() {
        final Predicate<String> p = MatchingPredicate.match_(
                MatchingPredicate._case(s -> s.startsWith("a"), s -> false),
                MatchingPredicate._case(s -> s.startsWith("b"), s -> true),
                MatchingPredicate._case(s -> s.startsWith("g"), s -> false),
                MatchingPredicate._default(s -> false)
        );
        assertFalse(p.test("alpha"));
        assertTrue(p.test("beta"));
        assertFalse(p.test("gamma"));
        assertFalse(p.test("delta"));
    }

    @Test
    public void canMatchUsingPredicate() {
        final Predicate<String> p = MatchingPredicate.match_(
                MatchingPredicate._case(String.class, s -> s.startsWith("a"), s -> false),
                MatchingPredicate._case(String.class, s -> s.startsWith("b"), s -> true),
                MatchingPredicate._case(String.class, s -> s.startsWith("g"), s -> false),
                MatchingPredicate._default(s -> false)
        );
        assertFalse(p.test("alpha"));
        assertTrue(p.test("beta"));
        assertFalse(p.test("gamma"));
        assertFalse(p.test("delta"));
    }

    @Test
    public void canMatchNumberUsingPredicate() {
        final Predicate<Number> p = MatchingPredicate.match_(
                MatchingPredicate._case(Integer.class, i -> i == 1, i -> true),
                MatchingPredicate._case(Long.class, l -> l == 1, l -> false),
                MatchingPredicate._case(Float.class, f -> f.compareTo(1f) == 0, f -> false),
                MatchingPredicate._case(Double.class, d -> d.compareTo(1d) == 0, d -> false)
        );
        assertTrue(p.test(1));
        assertFalse(p.test(1L));
        assertFalse(p.test(1f));
        assertFalse(p.test(1d));
    }

    @Test
    public void canMatchOnObject() {
        final Predicate<Object> p = MatchingPredicate.match_(
                MatchingPredicate._case(String.class, s -> false),
                MatchingPredicate._case(Integer.class, i -> true),
                MatchingPredicate._case(Exception.class, e -> false)
        );
        assertFalse(p.test("alpha"));
        assertTrue(p.test(123));
        assertFalse(p.test(new IOException()));
    }

    @Test
    public void canMatchEitherOnType() {
        Either<String, String> rightString = Either.right("A");
        Either<String, String> leftString = Either.left("A");
        Either<String, Integer> rightInt = Either.right(1);
        Either<Integer, Integer> leftInt = Either.left(1);
        final Predicate<Either> p = MatchingPredicate.match_(
                MatchingPredicate._case(Either.right(String.class), s -> true),
                MatchingPredicate._case(Either.left(String.class), s -> false),
                MatchingPredicate._case(Either.right(Integer.class), i -> true),
                MatchingPredicate._case(Either.left(Integer.class), i -> false)
        );
        assertTrue(p.test(rightString));
        assertFalse(p.test(leftString));
        assertTrue(p.test(rightInt));
        assertFalse(p.test(leftInt));
    }

    @Test
    public void canMatchEitherWithPredicate() {
        //we like A and 1 both in right and left...
        final Predicate<Either> p = MatchingPredicate.match_(
                MatchingPredicate._case(Either.right(String.class), "A"::equals, s -> true),
                MatchingPredicate._case(Either.right(String.class), "B"::equals, s -> false),
                MatchingPredicate._case(Either.left(String.class), "A"::equals, s -> true),
                MatchingPredicate._case(Either.left(String.class), "B"::equals, s -> false),
                MatchingPredicate._case(Either.right(Integer.class), i -> i == 1, i -> true),
                MatchingPredicate._case(Either.right(Integer.class), i -> i == 2, i -> false),
                MatchingPredicate._case(Either.left(Integer.class), i -> i == 1, i -> true),
                MatchingPredicate._case(Either.left(Integer.class), i -> i == 2, i -> false)
        );
        assertTrue(p.test(Either.right("A")));
        assertFalse(p.test(Either.right("B")));
        assertTrue(p.test(Either.left("A")));
        assertFalse(p.test(Either.left("B")));
        assertTrue(p.test(Either.right(1)));
        assertFalse(p.test(Either.right(2)));
        assertTrue(p.test(Either.left(1)));
        assertFalse(p.test(Either.left(2)));
    }
}
