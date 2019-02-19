package io.github.jebl01.bonjava;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SneakyThrow {
    @FunctionalInterface
    public interface SneakyFunction<T, R> {
        R apply(T t) throws Throwable;
    }

    @FunctionalInterface
    public interface SneakyBiFunction<T, U, R> {
        R apply(T t, U u) throws Throwable;
    }

    @FunctionalInterface
    public interface SneakyRunnable {
        void run() throws Throwable;
    }

    @FunctionalInterface
    public interface SneakyConsumer<T> {
        void accept(T t) throws Throwable;
    }

    @FunctionalInterface
    public interface SneakyBiConsumer<T, U> {
        void accept(T t, U u) throws Throwable;
    }

    @FunctionalInterface
    public interface SneakySupplier<T> {
        T get() throws Throwable;
    }

    public static <T, R> Function<T, R> withSneakyExceptions(SneakyFunction<T, R> f) {
        Objects.requireNonNull(f);
        return o -> {
            try {
                return f.apply(o);
            }
            catch(Throwable e) {
                SneakyThrow.<RuntimeException>sneakyException(e);
                return null;
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> withSneakyExceptions2(SneakyBiFunction<T, U, R> f) {
        Objects.requireNonNull(f);
        return (o, o2) -> {
            try {
                return f.apply(o, o2);
            }
            catch(Throwable e) {
                SneakyThrow.<RuntimeException>sneakyException(e);
                return null;
            }
        };
    }

    public static Runnable withSneakyExceptionsRun(SneakyRunnable f) {
        Objects.requireNonNull(f);
        return () -> {
            try {
                f.run();
            }
            catch(Throwable e) {
                SneakyThrow.<RuntimeException>sneakyException(e);
            }
        };
    }

    public static <T> Consumer<T> withSneakyExceptionsConsume(SneakyConsumer<T> f) {
        Objects.requireNonNull(f);
        return (o) -> {
            try {
                f.accept(o);
            }
            catch(Throwable e) {
                SneakyThrow.<RuntimeException>sneakyException(e);
            }
        };
    }

    public static <T, U> BiConsumer<T, U> withSneakyExceptionsConsume2(SneakyBiConsumer<T, U> f) {
        Objects.requireNonNull(f);
        return (t, u) -> {
            try {
                f.accept(t, u);
            }
            catch(Throwable e) {
                SneakyThrow.<RuntimeException>sneakyException(e);
            }
        };
    }

    public static <T> Supplier<T> withSneakyExceptionsSupply(SneakySupplier<T> f) {
        Objects.requireNonNull(f);
        return () -> {
            try {
                return f.get();
            }
            catch(Throwable e) {
                SneakyThrow.<RuntimeException>sneakyException(e);
                return null;
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <T extends Throwable> T sneakyException(Throwable t) throws T {
        throw (T) t;
    }
}
