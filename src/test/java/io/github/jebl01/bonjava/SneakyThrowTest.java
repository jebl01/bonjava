package io.github.jebl01.bonjava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jesblo on 15-08-19.
 */
public class SneakyThrowTest {

    @Test(expected = MalformedURLException.class)
    public void willThrowCheckedException() {
        Optional.of("sneaky&scary").map(SneakyThrow.withSneakyExceptions(URL::new));
    }

    @Test(expected = MalformedURLException.class)
    public void willThrowCheckedExceptionInMap2() {
        Optional<String> ot = Optional.of("left");
        Optional<String> ou = Optional.of("right");
        Assert.assertEquals(Optional.of("left:right"),
                Optionals.map2(SneakyThrow.withSneakyExceptions2((String t, String u) -> new URL(t + "+" + u)))
                        .apply(ot, ou));
    }

    @Test
    public void canCaptureAndMapSneakyErrors() {
        Either<String, String> withValue = Either.right("success");
        Either<String, URL> result = withValue.flatMap(
                ExceptionHandling.withExceptionHandler(
                        SneakyThrow.withSneakyExceptions(value -> Either.right(new URL(value))),
                        e -> Either.left(e.getMessage())));
        assertTrue(result.isLeft());
        assertEquals("no protocol: success", result.getLeft().get());
    }

    @Test(expected = MalformedURLException.class)
    public void testSupplier() {
        Optional.empty().orElseGet(SneakyThrow.withSneakyExceptionsSupply(() -> new URL("test")));
    }
}
