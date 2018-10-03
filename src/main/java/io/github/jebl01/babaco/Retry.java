package io.github.jebl01.babaco;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by jesblo on 16-02-05.
 */
public class Retry {

    private static void validateCommonArgs(int retries, long millisecWait, double backingOffMultiplier) {
        if(retries < 0) {
            throw new IllegalArgumentException("retries");
        }
        if(millisecWait < 0) {
            throw new IllegalArgumentException("millisecWait");
        }
        if(backingOffMultiplier < 0) {
            throw new IllegalArgumentException("backingOffMultiplier");
        }
    }

    public static <T, R> Function<T, R> withRetries(final Function<T, R> f,
                                                    final Predicate<R> predicate,
                                                    int retries,
                                                    long millisecWait,
                                                    double backingOffMultiplier) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(predicate);
        validateCommonArgs(retries, millisecWait, backingOffMultiplier);
        return t -> retry(
                () -> f.apply(t),
                predicate,
                e -> {
                    SneakyThrow.<RuntimeException>sneakyException(e);
                    return null;
                },
                Function.identity(),
                retries,
                millisecWait,
                backingOffMultiplier);
    }

    public static <T, L, R> Function<T, Either<L, R>> withRetries(final Function<T, R> f,
                                                                  final Predicate<R> predicate,
                                                                  final Function<Exception, L> errorMapper,
                                                                  int retries,
                                                                  long millisecWait,
                                                                  double backingOffMultiplier) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(errorMapper);
        validateCommonArgs(retries, millisecWait, backingOffMultiplier);
        return t -> retry(
                () -> f.apply(t),
                predicate,
                (e) -> Either.left(errorMapper.apply(e)),
                Either::right,
                retries,
                millisecWait,
                backingOffMultiplier);
    }

    public static <T, L1, L2, R> Function<T, Either<L2, R>> eitherWithRetries(final Function<T, Either<L1, R>> f,
                                                                              final Predicate<R> predicate,
                                                                              final Function<Exception, L2> errorMapper,
                                                                              int retries,
                                                                              long millisecWait,
                                                                              double backingOffMultiplier) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(errorMapper);
        validateCommonArgs(retries, millisecWait, backingOffMultiplier);
        return t -> retryEither(
                () -> f.apply(t),
                predicate,
                errorMapper,
                retries,
                millisecWait,
                backingOffMultiplier);
    }

    public static <T> Supplier<T> withRetriesSupply(final Supplier<T> f,
                                                    final Predicate<T> predicate,
                                                    int retries,
                                                    long millisecWait,
                                                    double backingOffMultiplier) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(predicate);
        validateCommonArgs(retries, millisecWait, backingOffMultiplier);
        return () -> retry(
                f,
                predicate,
                e -> {
                    SneakyThrow.<RuntimeException>sneakyException(e);
                    return null;
                },
                Function.identity(),
                retries,
                millisecWait,
                backingOffMultiplier);
    }

    public static <L, R> Supplier<Either<L, R>> withRetriesSupply(final Supplier<R> f,
                                                                  final Predicate<R> predicate,
                                                                  final Function<Exception, L> errorMapper,
                                                                  int retries,
                                                                  long millisecWait,
                                                                  double backingOffMultiplier) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(errorMapper);
        validateCommonArgs(retries, millisecWait, backingOffMultiplier);
        return () -> retry(
                f,
                predicate,
                (e) -> Either.left(errorMapper.apply(e)),
                Either::right,
                retries,
                millisecWait,
                backingOffMultiplier);
    }

    public static <L1, L2, R> Supplier<Either<L2, R>> eitherWithRetriesSupply(final Supplier<Either<L1, R>> f,
                                                                              final Predicate<R> predicate,
                                                                              final Function<Exception, L2> errorMapper,
                                                                              int retries,
                                                                              long millisecWait,
                                                                              double backingOffMultiplier) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(errorMapper);
        validateCommonArgs(retries, millisecWait, backingOffMultiplier);
        return () -> retryEither(
                f,
                predicate,
                errorMapper,
                retries,
                millisecWait,
                backingOffMultiplier);
    }

    public static Runnable withRetriesRun(final Runnable f,
                                          final Consumer<Throwable> errorHandler,
                                          int retries,
                                          long millisecWait,
                                          double backingOffMultiplier) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(errorHandler);
        validateCommonArgs(retries, millisecWait, backingOffMultiplier);
        return () -> retry(
                () -> {
                    f.run();
                    return true;
                },
                r -> true,
                (t) -> {
                    errorHandler.accept(t);
                    return true;
                },
                Function.identity(),
                retries,
                millisecWait,
                backingOffMultiplier);
    }

    private static <T, R> T retry(Supplier<R> f,
                                  final Predicate<R> predicate,
                                  final Function<Exception, T> errorMapper,
                                  final Function<R, T> resultMapper,
                                  int retries,
                                  long millisecWait,
                                  double backingOffMultiplier) {
        for(int i = 0; i < retries; i++) {
            try {
                R result = f.get();
                if(predicate.test(result)) {
                    return resultMapper.apply(result);
                }
            }
            catch(Throwable e) {
                if(i == retries - 1) {
                    return errorMapper.apply(new RetryException("retried " + retries + " times but failed with exception",
                            e));
                }
            }
            try {
                Thread.sleep(millisecWait);
            } catch (InterruptedException e) {
                return errorMapper.apply(e);
            }
            millisecWait = (long) (millisecWait * backingOffMultiplier);
        }
        return errorMapper.apply(new RetryException("retried " + retries + " times but failed"));
    }

    private static <L1, L2, R> Either<L2, R> retryEither(Supplier<Either<L1, R>> f,
                                                         final Predicate<R> predicate,
                                                         final Function<Exception, L2> errorMapper,
                                                         int retries,
                                                         long millisecWait,
                                                         double backingOffMultiplier) {
        for(int i = 0; i < retries; i++) {
            try {
                final Either<L1, R> result = f.get();
                if(result.isRight() && predicate.test(result.getRight().get())) {
                    return Either.right(result.getRight().get());
                }
            }
            catch(Throwable e) {
                if(i == retries - 1) {
                    return Either.left(errorMapper.apply(new RetryException("retried " + retries + " times but failed with exception",
                            e)));
                }
            }
            try {
                Thread.sleep(millisecWait);
            } catch (InterruptedException e) {
                return Either.left(errorMapper.apply(e));
            }
            millisecWait = (long) (millisecWait * backingOffMultiplier);
        }
        return Either.left(errorMapper.apply(new RetryException("retried " + retries + " times but failed")));
    }

    public static class RetryException extends RuntimeException {
        public RetryException(String reason) {
            super(reason);
        }

        public RetryException(String reason, Throwable cause) {
            super(reason, cause);
        }
    }
}
