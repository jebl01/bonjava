package io.github.jebl01.babaco;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by jesblo on 15-08-17.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Optionals {
    public static <T, U, R> BiFunction<Optional<T>, Optional<U>, Optional<R>> map2(BiFunction<T, U, R> f) {
        Objects.requireNonNull(f);
        return (ot, ou) -> ot.flatMap(t -> ou.map(u -> f.apply(t, u)));
    }

    public static <T, U, R> BiFunction<Optional<T>, Optional<U>, Optional<R>> flatMap2(BiFunction<T, U, Optional<R>> f) {
        Objects.requireNonNull(f);
        return (ot, ou) -> ot.flatMap(t -> ou.flatMap(u -> f.apply(t, u)));
    }

    public static <T, R> Function<Optional<T>, Stream<R>> toStream(Function<T, Stream<R>> f) {
        Objects.requireNonNull(f);
        return (ot) -> ot.map(f).orElseGet(Stream::empty);
    }

    public static <T> Stream<T> toStream(Optional<T> optional) {
        return optional.map(Stream::of).orElse(Stream.empty());
    }

    public static <T> Stream<T> toStream(Optional<T>... optional) {
        return Arrays.stream(optional)
                .flatMap(Optionals::toStream);
    }

    public static <T> Stream<T> toStream(Iterable<Optional<T>> optionals) {
        return StreamSupport.stream(optionals.spliterator(), false)
                .flatMap(Optionals::toStream);
    }

    public static <T, L, R> Function<Optional<T>, Either<L, R>> mapToEither(Function<T, Either<L, R>> f,
                                                                            Supplier<L> leftSupplier) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(leftSupplier);
        return (ot) -> ot.map(f).orElseGet(() -> Either.left(leftSupplier.get()));
    }

    public static <T, U, L, R> BiFunction<Optional<T>, Optional<U>, Either<L, R>> map2ToEither(BiFunction<T, U, Either<L, R>> f,
                                                                                               Supplier<L> leftSupplier) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(leftSupplier);
        return (ot, ou) -> {
            if(!ot.isPresent() || !ou.isPresent()) {
                return Either.left(leftSupplier.get());
            }
            return f.apply(ot.get(), ou.get());
        };
    }

    public static <A, B> Optional<Tuple.Tuple2<A, B>> toTuple(Optional<A> oa, Optional<B> ob) {
        Objects.requireNonNull(oa);
        Objects.requireNonNull(ob);
        return map2((BiFunction<A, B, Tuple.Tuple2<A, B>>) Tuple.Tuple2::new).apply(oa, ob);
    }

    public static <T> Optional<T> thisOrThat(Optional<T> thiz, Supplier<Optional<T>> thatSupplier) {
        return thiz.isPresent() ? thiz : thatSupplier.get();
    }

    public static <T> Optional<T> ifPresent(Optional<T> optional, Consumer<T> consumer) {
        optional.ifPresent(consumer);
        return optional;
    }

    public static <T> void ifPresentOrElse(Optional<T> optional, Consumer<T> consumer, Runnable elseRunner) {
        if(optional.isPresent()) {
            consumer.accept(optional.get());
        }
        else {
            elseRunner.run();
        }
    }
}
