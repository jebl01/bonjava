package io.github.jebl01.bonjava;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionHandling {
    public static <T, R> Function<T, R> withExceptionHandler(Function<T, R> f,
                                                             Function<Throwable, R> exceptionHandler) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(exceptionHandler);
        return (t) -> {
            try {
                return f.apply(t);
            }
            catch(Throwable throwable) {
                return applyExceptionHandler(throwable, exceptionHandler);
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> withExceptionHandler2(BiFunction<T, U, R> f,
                                                                      Function<Throwable, R> exceptionHandler) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(exceptionHandler);
        return (t, u) -> {
            try {
                return f.apply(t, u);
            }
            catch(Throwable throwable) {
                return applyExceptionHandler(throwable, exceptionHandler);
            }
        };
    }

    public static <T> Supplier<T> withExceptionHandlerSupply(Supplier<T> f, Function<Throwable, T> exceptionHandler) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(exceptionHandler);
        return () -> {
            try {
                return f.get();
            }
            catch(Throwable throwable) {
                return applyExceptionHandler(throwable, exceptionHandler);
            }
        };
    }

    public static Runnable withExceptionHandlerRun(Runnable f, Consumer<Throwable> exceptionHandler) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(exceptionHandler);
        return () -> {
            try {
                f.run();
            }
            catch(Throwable throwable) {
                applyExceptionHandler(throwable, exceptionHandler);
            }
        };
    }

    public static <T> Consumer<T> withExceptionHandlerConsume(Consumer<T> f, Consumer<Throwable> exceptionHandler) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(exceptionHandler);
        return (t) -> {
            try {
                f.accept(t);
            }
            catch(Throwable throwable) {
                applyExceptionHandler(throwable, exceptionHandler);
            }
        };
    }

    public static <T, U> BiConsumer<T, U> withExceptionHandlerConsume2(BiConsumer<T, U> f,
                                                                       Consumer<Throwable> exceptionHandler) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(exceptionHandler);
        return (t, u) -> {
            try {
                f.accept(t, u);
            }
            catch(Throwable throwable) {
                applyExceptionHandler(throwable, exceptionHandler);
            }
        };
    }

    private static <T> T applyExceptionHandler(Throwable t, Function<Throwable, T> exceptionHandler) {
        try {
            return exceptionHandler.apply(t);
        }
        catch(Throwable e) {
            SneakyThrow.<RuntimeException>sneakyException(e);
            return null;
        }
    }

    private static void applyExceptionHandler(Throwable t, Consumer<Throwable> exceptionHandler) {
        try {
            exceptionHandler.accept(t);
        }
        catch(Throwable e) {
            SneakyThrow.<RuntimeException>sneakyException(e);
        }
    }
}
