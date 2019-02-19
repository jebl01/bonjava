package io.github.jebl01.bonjava;

import static io.github.jebl01.bonjava.ExceptionHandling.withExceptionHandler;
import static io.github.jebl01.bonjava.ExceptionHandling.withExceptionHandler2;
import static io.github.jebl01.bonjava.ExceptionHandling.withExceptionHandlerConsume;
import static io.github.jebl01.bonjava.ExceptionHandling.withExceptionHandlerConsume2;
import static io.github.jebl01.bonjava.ExceptionHandling.withExceptionHandlerRun;
import static io.github.jebl01.bonjava.ExceptionHandling.withExceptionHandlerSupply;
import static io.github.jebl01.bonjava.Optionals.map2ToEither;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Test;

public class ExceptionHandlingTest {
    @Test
    public void canCaptureAndMapErrors() {
        Either<String, String> withValue = Either.right("success");
        Either<String, String> result = withValue.flatMap(
                withExceptionHandler(
                        value -> Either.right(returnNothingButThrowRuntimeException("mapped failure")),
                        e -> Either.left(e.getMessage())));
        assertTrue(result.isLeft());
        assertEquals("mapped failure", result.getLeft().get());
    }

    @Test
    public void canCaptureAndMapErrors2() {
        Optional<String> optWithValueA = Optional.of("A");
        Optional<String> optWithValueB = Optional.of("B");
        Either<String, String> result = map2ToEither(
                withExceptionHandler2(
                        (String a, String b) -> Either.right(returnNothingButThrowRuntimeException("mapped failure")),
                        e -> Either.left(e.getMessage())),
                () -> "unused left provider...").apply(optWithValueA, optWithValueB);
        assertTrue(result.isLeft());
        assertEquals("mapped failure", result.getLeft().get());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void rethrowingExceptionInMapperYieldsExpectedResult() {
        Either<String, String> withValue = Either.right("success");
        withValue.map(
                withExceptionHandler(
                        value -> Either.right(returnNothingButThrowRuntimeException("mapped failure")),
                        exception -> {
                            throw new UnsupportedOperationException(exception);
                        }));
    }

    @Test(expected = IOException.class)
    public void canRethrowCheckedException() {
        Either<String, String> withValue = Either.right("success");
        withValue.map(
                withExceptionHandler(
                        SneakyThrow.withSneakyExceptions(
                                value -> Either.right(returnNothingButThrowException("mapped failure"))),
                        SneakyThrow.withSneakyExceptions(
                                exception -> {
                                    throw new IOException(exception);
                                })));
    }

    @Test
    public void canHandleExceptionsWhenFlatMappingStream() {
        List<String> failedRecorder = new ArrayList<>();
        Stream<String> stringsToConvert = Stream.of("fail",
                "http://page.web",
                "wrong",
                "https://success.com",
                "fail.com");
        Stream<URL> urls = stringsToConvert.flatMap(
                withExceptionHandler(
                        SneakyThrow.withSneakyExceptions(string -> Stream.of(new URL(string))),
                        exception -> {
                            failedRecorder.add(exception.getMessage());
                            return Stream.empty();
                        }));
        assertEquals(2, urls.count());
        assertEquals(3, failedRecorder.size());
    }

    @Test
    public void canHandleExceptionInSupplier() throws Exception {
        Supplier<String> runnable = withExceptionHandlerSupply(
                SneakyThrow.withSneakyExceptionsSupply(() -> {
                    throw new Exception("test");
                }),
                Throwable::getMessage);
        String result = runnable.get();
        assertEquals("test", result);
    }

    @Test
    public void canHandleExceptionInRunnable() throws Exception {
        final AtomicReference<String> recorder = new AtomicReference<>();
        Runnable runnable = withExceptionHandlerRun(
                SneakyThrow.withSneakyExceptionsRun(() -> {
                    throw new Exception("test");
                }),
                error -> recorder.set(error.getMessage()));
        runnable.run();
        assertEquals("test", recorder.get());
    }

    @Test
    public void canHandleExceptionInConsumer() throws Exception {
        final AtomicReference<String> recorder = new AtomicReference<>();
        Consumer<String> consumer = withExceptionHandlerConsume(
                SneakyThrow.withSneakyExceptionsConsume((data) -> {
                    throw new Exception(data);
                }),
                error -> recorder.set(error.getMessage()));
        consumer.accept("test");
        assertEquals("test", recorder.get());
    }

    @Test
    public void canHandleExceptionInConsumer2() throws Exception {
        final AtomicReference<String> recorder = new AtomicReference<>();
        BiConsumer<String, String> consumer = withExceptionHandlerConsume2(
                SneakyThrow.withSneakyExceptionsConsume2((data, data2) -> {
                    throw new Exception(data + ":" + data2);
                }),
                error -> recorder.set(error.getMessage()));
        consumer.accept("test", "test2");
        assertEquals("test:test2", recorder.get());
    }

    private String returnNothingButThrowRuntimeException(String message) {
        throw new RuntimeException(message);
    }

    private String returnNothingButThrowException(String message) throws Exception {
        throw new Exception(message);
    }
}
