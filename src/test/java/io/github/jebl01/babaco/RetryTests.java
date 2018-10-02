package io.github.jebl01.babaco;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * Created by jesblo on 16-02-05.
 */
@RunWith(Enclosed.class)
public class RetryTests {

    public static class FunctionReturningRetry {
        @Test
        public void testHappyCase() {
            Optional<String> data = Optional.of("test");
            Optional<String> result = data.map(Retry.withRetries(
                    String::toUpperCase,
                    d -> d.equals("TEST"),
                    3,
                    100,
                    2));
            assertEquals("TEST", result.get());
        }

        @Test
        public void testRetrySome() {
            final List<String> convertedData = Arrays.asList("TE", "TES", "TEST");
            final AtomicInteger retryNo = new AtomicInteger(0);
            Optional<String> data = Optional.of("test");
            Optional<String> result = data.map(Retry.withRetries(
                    d -> {
                        System.out.println("trying to apply function");
                        return convertedData.get(retryNo.getAndIncrement());
                    },
                    d -> d.equals("TEST"),
                    3,
                    200,
                    2));
            assertEquals("TEST", result.get());
        }

        @Test
        public void testFailRetry() {
            final List<String> convertedData = Arrays.asList("TE", "TES", "TEST");
            final AtomicInteger retryNo = new AtomicInteger(0);
            Optional<String> data = Optional.of("test");
            try {
                data.map(Retry.withRetries(
                        d -> {
                            System.out.println("trying to apply function");
                            return convertedData.get(retryNo.getAndIncrement());
                        },
                        d -> d.equals("TEST"),
                        2,
                        200,
                        2));
                fail();
            }
            catch(Retry.RetryException e) {
                assertEquals("retried 2 times but failed", e.getMessage());
                assertNull(e.getCause());
                return;
            }
            fail();
        }

        @Test
        public void testFailRetryWithCause() {
            Optional<String> data = Optional.of("test");
            try {
                data.map(Retry.withRetries(
                        d -> {
                            throw new RuntimeException("things failed");
                        },
                        d -> true,
                        2,
                        200,
                        2));
                fail();
            }
            catch(Retry.RetryException e) {
                assertEquals("retried 2 times but failed with exception", e.getMessage());
                assertNotNull(e.getCause());
                assertEquals("things failed", e.getCause().getMessage());
                return;
            }
            fail();
        }
    }

    public static class EitherReturningFunctionRetry {
        @Test
        public void testHappyCase() {
            final Either<String, String> data = Either.right("test");
            Either<String, String> result = data.flatMap(Retry.withRetries(
                    String::toUpperCase,
                    d -> d.equals("TEST"),
                    e -> "error: " + e.getMessage(),
                    3,
                    100,
                    2));
            assertEquals("TEST", result.getRight().get());
        }

        @Test
        public void testRetrySome() {
            final List<String> convertedData = Arrays.asList("TE", "TES", "TEST");
            final AtomicInteger retryNo = new AtomicInteger(0);
            final Either<String, String> data = Either.right("test");
            Either<String, String> result = data.flatMap(Retry.withRetries(
                    d -> {
                        System.out.println("trying to apply function");
                        return convertedData.get(retryNo.getAndIncrement());
                    },
                    d -> d.equals("TEST"),
                    e -> "error: " + e.getMessage(),
                    3,
                    200,
                    2));
            assertEquals("TEST", result.getRight().get());
        }

        @Test
        public void testFailRetry() {
            final List<String> convertedData = Arrays.asList("TE", "TES", "TEST");
            final AtomicInteger retryNo = new AtomicInteger(0);
            final Either<String, String> data = Either.right("test");
            Either<String, String> result = data.flatMap(Retry.withRetries(
                    d -> {
                        System.out.println("trying to apply function");
                        return convertedData.get(retryNo.getAndIncrement());
                    },
                    d -> d.equals("TEST"),
                    e -> "error: " + e.getMessage(),
                    2,
                    200,
                    2));
            assertEquals("error: retried 2 times but failed", result.getLeft().get());
        }

        @Test
        public void testFailRetryWithCause() {
            final Either<String, String> data = Either.right("test");
            Either<String, String> result = data.flatMap(Retry.withRetries(
                    d -> {
                        throw new RuntimeException("things failed");
                    },
                    d -> true,
                    e -> "error: " + e.getMessage(),
                    2,
                    200,
                    2));
            assertEquals("error: retried 2 times but failed with exception", result.getLeft().get());
        }
    }

    public static class SupplierReturningRetry {
        @Test
        public void testHappyCase() {
            String result = Retry.withRetriesSupply(
                    () -> "TEST",
                    d -> d.equals("TEST"),
                    3,
                    100,
                    2).get();
            assertEquals("TEST", result);
        }

        @Test
        public void testRetrySome() {
            final List<String> convertedData = Arrays.asList("TE", "TES", "TEST");
            final AtomicInteger retryNo = new AtomicInteger(0);
            String result = Retry.withRetriesSupply(
                    () -> {
                        System.out.println("trying to supply value");
                        return convertedData.get(retryNo.getAndIncrement());
                    },
                    d -> d.equals("TEST"),
                    3,
                    200,
                    2).get();
            assertEquals("TEST", result);
        }

        @Test
        public void testFailRetry() {
            final List<String> convertedData = Arrays.asList("TE", "TES", "TEST");
            final AtomicInteger retryNo = new AtomicInteger(0);
            try {
                Retry.withRetriesSupply(
                        () -> {
                            System.out.println("trying to supply value");
                            return convertedData.get(retryNo.getAndIncrement());
                        },
                        d -> d.equals("TEST"),
                        2,
                        200,
                        2).get();
                fail();
            }
            catch(Retry.RetryException e) {
                assertEquals("retried 2 times but failed", e.getMessage());
                assertNull(e.getCause());
                return;
            }
            fail();
        }

        @Test
        public void testFailRetryWithCause() {
            try {
                Retry.withRetriesSupply(
                        () -> {
                            throw new RuntimeException("things failed");
                        },
                        d -> true,
                        2,
                        200,
                        2).get();
                fail();
            }
            catch(Retry.RetryException e) {
                assertEquals("retried 2 times but failed with exception", e.getMessage());
                assertNotNull(e.getCause());
                assertEquals("things failed", e.getCause().getMessage());
                return;
            }
            fail();
        }
    }

    public static class EitherReturningSupplierRetry {
        @Test
        public void testHappyCase() {
            Either<String, String> result = Retry.withRetriesSupply(
                    () -> "TEST",
                    d -> true,
                    e -> "error: " + e.getMessage(),
                    3,
                    100,
                    2).get();
            assertEquals("TEST", result.getRight().get());
        }

        @Test
        public void testRetrySome() {
            final List<String> convertedData = Arrays.asList("TE", "TES", "TEST");
            final AtomicInteger retryNo = new AtomicInteger(0);
            Either<String, String> result = Retry.withRetriesSupply(
                    () -> {
                        System.out.println("trying to supply value");
                        return convertedData.get(retryNo.getAndIncrement());
                    },
                    d -> d.equals("TEST"),
                    e -> "error: " + e.getMessage(),
                    3,
                    200,
                    2).get();
            assertEquals("TEST", result.getRight().get());
        }

        @Test
        public void testFailRetry() {
            final List<String> convertedData = Arrays.asList("TE", "TES", "TEST");
            final AtomicInteger retryNo = new AtomicInteger(0);
            Either<String, String> result = Retry.withRetriesSupply(
                    () -> {
                        System.out.println("trying to supply value");
                        return convertedData.get(retryNo.getAndIncrement());
                    },
                    d -> d.equals("TEST"),
                    e -> "error: " + e.getMessage(),
                    2,
                    200,
                    2).get();
            assertEquals("error: retried 2 times but failed", result.getLeft().get());
        }

        @Test
        public void testFailRetryWithCause() {
            Either<String, String> result = Retry.withRetriesSupply(
                    () -> returnNothingButThrow("things failed"),
                    d -> true,
                    e -> "error: " + e.getMessage(),
                    2,
                    200,
                    2).get();
            assertEquals("error: retried 2 times but failed with exception", result.getLeft().get());
        }

        private String returnNothingButThrow(String message) {
            if(true) {
                throw new RuntimeException(message);
            }
            return null;
        }
    }

    public static class RunWithRetry {
        @Test
        public void testHappyCase() {
            Retry.withRetriesRun(
                    () -> System.out.println("running with retries"),
                    e -> fail(e.getMessage()),
                    3,
                    100,
                    2).run();
        }

        @Test
        public void testRetrySome() {
            final AtomicInteger retryCount = new AtomicInteger(0);
            Retry.withRetriesRun(
                    () -> {
                        System.out.println("running with retries");
                        if(retryCount.incrementAndGet() < 3) {
                            throw new RuntimeException("run failed");
                        }
                    },
                    e -> fail(e.getMessage()),
                    3,
                    200,
                    2).run();
            assertEquals(3, retryCount.get());
        }

        @Test
        public void testFailRetry() {
            final AtomicInteger retryCount = new AtomicInteger(0);
            final AtomicBoolean handledError = new AtomicBoolean(false);
            Retry.withRetriesRun(
                    () -> {
                        System.out.println("running with retries");
                        if(retryCount.incrementAndGet() < 3) {
                            throw new RuntimeException("run failed");
                        }
                    },
                    e -> {
                        assertEquals("retried 2 times but failed with exception", e.getMessage());
                        assertNotNull(e.getCause());
                        assertEquals(2, retryCount.get());
                        handledError.set(true);
                    },
                    2,
                    200,
                    2).run();
            assertTrue(handledError.get());
        }

        @Test
        public void testFailRetryWithCause() {
            try {
                Retry.withRetriesSupply(
                        () -> {
                            throw new RuntimeException("things failed");
                        },
                        d -> true,
                        2,
                        200,
                        2).get();
                fail();
            }
            catch(Retry.RetryException e) {
                assertEquals("retried 2 times but failed with exception", e.getMessage());
                assertNotNull(e.getCause());
                assertEquals("things failed", e.getCause().getMessage());
                return;
            }
            fail();
        }
    }
}
