package io.github.jebl01.bonjava;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by jesblo on 15-08-17.
 */
public abstract class Either<L, R> {

    public static <L, R> Either<L, R> left(L left) {
        return new Left<>(left);
    }

    public static <L, R> Either<L, R> right(R right) {
        return new Right<>(right);
    }

    public static <L, R> Either<L, R> fromNullable(R value, Supplier<L> leftSupplier) {
        return Optionals.mapToEither((R v) -> right(v), leftSupplier).apply(Optional.ofNullable(value));
    }

    public static <L, R> Either<L, R> fromOptional(Optional<R> value, Supplier<L> leftSupplier) {
        return Optionals.mapToEither((R v) -> right(v), leftSupplier).apply(value);
    }

    public static <L, R> Either<L, R> thisOrThat(Either<L, R> thiz, Supplier<Either<L, R>> thatSupplier) {
        return thiz.isRight() ? thiz : thatSupplier.get();
    }

    public void ifLeft(Consumer<L> consumer) {
    }

    public void ifRight(Consumer<R> consumer) {
    }

    public boolean isLeft() {
        return false;
    }

    public boolean isRight() {
        return false;
    }

    public abstract <T> Optional<T> get(Function<? super L, Optional<T>> leftSink,
                                        Function<? super R, Optional<T>> rightSink);

    public Optional<L> getLeft() {
        return Optional.empty();
    }

    public Optional<R> getRight() {
        return Optional.empty();
    }

    public abstract <T> Either<L, T> map(Function<? super R, T> f);

    public abstract <T> Either<T, R> mapLeft(Function<? super L, T> f);

    public abstract <T> Either<L, T> flatMap(Function<? super R, Either<L, T>> f);

    public void consume(Consumer<Either<L, R>> consumer) {
        consumer.accept(this);
    }

    public void consume(Consumer<L> leftConsumer, Consumer<R> rightConsumer) {
        ifLeft(leftConsumer);
        ifRight(rightConsumer);
    }

    public <T> T map(Function<L, T> lmap, Function<R, T> rmap) {
        if(isLeft()) {
            return Objects.requireNonNull(lmap, "lmap cannot be null").apply(getLeft().get());
        }
        else {
            return Objects.requireNonNull(rmap, "rmap cannot be null").apply(getRight().get());
        }
    }

    public <E extends Throwable> R getOrThrow(Function<L, E> f) throws E {
        if(isRight()) {
            return getRight().get();
        }
        throw f.apply(getLeft().get());
    }

    public static class Left<L, R> extends Either<L, R> {
        private final L left;

        private Left(L left) {
            Objects.requireNonNull(left);
            this.left = left;
        }

        @Override
        public <T> Either<L, T> map(Function<? super R, T> f) {
            return left(left);
        }

        public <T> Either<T, R> mapLeft(Function<? super L, T> f) {
            Objects.requireNonNull(f);
            return left(f.apply(left));
        }

        @Override
        public <T> Either<L, T> flatMap(Function<? super R, Either<L, T>> f) {
            return left(left);
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public void ifLeft(final Consumer<L> consumer) {
            consumer.accept(left);
        }

        @Override
        public <T> Optional<T> get(final Function<? super L, Optional<T>> leftSink,
                                   final Function<? super R, Optional<T>> rightSink) {
            return leftSink.apply(left);
        }

        @Override
        public Optional<L> getLeft() {
            return Optional.of(left);
        }
    }

    public static class Right<L, R> extends Either<L, R> {
        private final R right;

        private Right(R right) {
            Objects.requireNonNull(right);
            this.right = right;
        }

        @Override
        public <T> Either<L, T> map(final Function<? super R, T> f) {
            Objects.requireNonNull(f);
            return right(f.apply(right));
        }

        @Override
        public <T> Either<T, R> mapLeft(final Function<? super L, T> f) {
            return right(right);
        }

        @Override
        public <T> Either<L, T> flatMap(final Function<? super R, Either<L, T>> f) {
            Objects.requireNonNull(f);
            return f.apply(right);
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public void ifRight(final Consumer<R> consumer) {
            consumer.accept(right);
        }

        @Override
        public <T> Optional<T> get(final Function<? super L, Optional<T>> leftSink,
                                   final Function<? super R, Optional<T>> rightSink) {
            return rightSink.apply(right);
        }

        @Override
        public Optional<R> getRight() {
            return Optional.of(right);
        }
    }
}
