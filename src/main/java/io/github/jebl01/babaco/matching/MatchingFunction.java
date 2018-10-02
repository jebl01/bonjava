package io.github.jebl01.babaco.matching;

import io.github.jebl01.babaco.Either;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class MatchingFunction {
    @SafeVarargs
    public static <T, R> CasesFunction<T, R> match_(CaseFunction<T, R, ?>... cases) {
        return new CasesFunction<>(Arrays.asList(cases));
    }

    public static <T, R, X> CaseFunction<T, R, X> _case(final Either<Class<X>, Class<X>> either, Function<X, R> f) {
        return new EitherCaseFunction<>(either, t -> true, f);
    }

    public static <T, R, X> CaseFunction<T, R, X> _case(final Either<Class<X>, Class<X>> either,
                                                        Predicate<X> predicate,
                                                        Function<X, R> f) {
        return new EitherCaseFunction<>(either, predicate, f);
    }

    public static <T, R, X> CaseFunction<T, R, X> _case(Predicate<T> predicate, Function<T, R> f) {
        return new CaseFunction<T, R, X>() {
            public boolean matches(final T t) {
                return predicate.test(t);
            }

            public R apply(final T t) {
                return f.apply(t);
            }
        };
    }

    public static <T, R, X> CaseFunction<T, R, X> _case(Class<X> clazz, Function<X, R> f) {
        return new CaseFunctionImpl<>(clazz, t -> true, f);
    }

    public static <T, R, X> CaseFunction<T, R, X> _case(Class<X> clazz, Predicate<X> predicate, Function<X, R> f) {
        return new CaseFunctionImpl<>(clazz, predicate, f);
    }

    public static <T, R> CaseFunction<T, R, T> _default(Function<T, R> f) {
        return new CaseFunctionImpl<>(Object.class, t -> true, f);
    }

    static class CasesFunction<T, R> implements Function<T, R> {
        private List<CaseFunction<T, R, ?>> cases;

        private CasesFunction(List<CaseFunction<T, R, ?>> cases) {
            this.cases = cases;
        }

        @Override
        public R apply(T t) {
            for(CaseFunction<T, R, ?> _case : cases) {
                if(_case.matches(t)) {
                    return _case.apply(t);
                }
            }
            if(Throwable.class.isAssignableFrom(t.getClass())) {
                throw new MatchingException("failed to find matching applicator", (Throwable) t);
            }
            throw new MatchingException("failed to find matching applicator");
        }
    }

    public interface CaseFunction<T, R, X> extends Function<T, R> {
        boolean matches(T t);
    }

    @SuppressWarnings("unchecked")
    public static class CaseFunctionImpl<T, R, X> implements CaseFunction<T, R, X> {
        private final Class<? super X> clazz;
        private final Predicate<X> predicate;
        private final Function<X, R> f;

        private CaseFunctionImpl(Class<? super X> clazz, Predicate<X> predicate, Function<X, R> f) {
            this.clazz = clazz;
            this.predicate = predicate;
            this.f = f;
        }

        @Override
        public boolean matches(T t) {
            return clazz.isAssignableFrom(t.getClass()) && predicate.test((X) t);
        }

        @Override
        public R apply(T t) {
            return f.apply((X) t);
        }
    }

    @SuppressWarnings("unchecked")
    public static class EitherCaseFunction<T, R, X> implements CaseFunction<T, R, X> {
        private final Either<Class<X>, Class<X>> either;
        private final Predicate<X> predicate;
        private final Function<X, R> f;

        private EitherCaseFunction(Either<Class<X>, Class<X>> either, Predicate<X> predicate, Function<X, R> f) {
            this.either = either;
            this.predicate = predicate;
            this.f = f;
        }

        @Override
        public boolean matches(T t) {
            return match_(
                    _case(Either.Left.class, l -> either.getLeft()
                            .filter(c -> c.isAssignableFrom(l.getLeft().get().getClass()))
                            .filter(c -> predicate.test((X) l.getLeft().get())).isPresent()),
                    _case(Either.Right.class, r -> either.getRight()
                            .filter(c -> c.isAssignableFrom(r.getRight().get().getClass()))
                            .filter(c -> predicate.test((X) r.getRight().get())).isPresent()),
                    _default(s -> false)
            ).apply(t);
        }

        @Override
        public R apply(T t) {
            return match_(
                    _case(Either.Left.class, l -> f.apply((X) l.getLeft().get())),
                    _case(Either.Right.class, r -> f.apply((X) r.getRight().get()))
            ).apply(t);
        }
    }

    public static class MatchingException extends RuntimeException {
        public MatchingException(String reason) {
            super(reason);
        }

        public MatchingException(String reason, Throwable cause) {
            super(reason, cause);
        }
    }
}
