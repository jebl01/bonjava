package io.github.jebl01.babaco;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by jesblo on 15-08-19.
 */
public class SideEffects {
    public static <T, R> Function<T, R> withSideEffect(Function<T, R> f, Consumer<R> effect) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(effect);
        return (t) -> {
            R result = f.apply(t);
            effect.accept(result);
            return result;
        };
    }

    public static <T, U, R> BiFunction<T, U, R> withSideEffect2(BiFunction<T, U, R> f, Consumer<R> effect) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(effect);
        return (t, u) -> {
            R result = f.apply(t, u);
            effect.accept(result);
            return result;
        };
    }

    public static <T> Supplier<T> withSideEffectSupply(Supplier<T> supplier, Consumer<T> effect) {
        Objects.requireNonNull(supplier);
        return () -> {
            T result = supplier.get();
            effect.accept(result);
            return result;
        };
    }
}
