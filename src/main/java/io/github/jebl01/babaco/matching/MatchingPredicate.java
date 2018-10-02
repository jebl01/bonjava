package io.github.jebl01.babaco.matching;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import io.github.jebl01.babaco.Either;

public class MatchingPredicate {
    @SafeVarargs
    public static <T> CasesPredicate<T> match_(CasePredicate<T, ?>... cases) {
        return new CasesPredicate<>(Arrays.asList(cases));
    }

    public static <T, X> CasePredicate<T, X> _case(final Either<Class<X>, Class<X>> either, Predicate<X> f) {
        return new EitherCasePredicate<>(either, t -> true, f);
    }

    public static <T, X> CasePredicate<T, X> _case(final Either<Class<X>, Class<X>> either,
                                                   Predicate<X> predicate,
                                                   Predicate<X> f) {
        return new EitherCasePredicate<>(either, predicate, f);
    }

    public static <T, X> CasePredicate<T, X> _case(Class<X> clazz, Predicate<X> f) {
        return new CasePredicateImpl<>(clazz, t -> true, f);
    }

    public static <T, X> CasePredicate<T, X> _case(Predicate<T> predicate, Predicate<T> f) {
        return new CasePredicate<T, X>() {
            public boolean matches(final T t) {
                return predicate.test(t);
            }

            public boolean test(final T t) {
                return f.test(t);
            }
        };
    }

    public static <T, X> CasePredicate<T, X> _case(Class<X> clazz, Predicate<X> predicate, Predicate<X> f) {
        return new CasePredicateImpl<>(clazz, predicate, f);
    }

    public static <T> CasePredicate<T, T> _default(Predicate<T> f) {
        return new CasePredicateImpl<>(Object.class, t -> true, f);
    }

    static class CasesPredicate<T> implements Predicate<T> {
        private List<CasePredicate<T, ?>> cases;

        private CasesPredicate(List<CasePredicate<T, ?>> cases) {
            this.cases = cases;
        }

        @Override
        public boolean test(final T t) {
            for(CasePredicate<T, ?> _case : cases) {
                if(_case.matches(t)) {
                    return _case.test(t);
                }
            }
            if(Throwable.class.isAssignableFrom(t.getClass())) {
                throw new MatchingException("failed to find matching applicator", (Throwable) t);
            }
            throw new MatchingException("failed to find matching applicator");
        }
    }

    public interface CasePredicate<T, X> extends Predicate<T> {
        boolean matches(T t);
    }

    @SuppressWarnings("unchecked")
    public static class CasePredicateImpl<T, X> implements CasePredicate<T, X> {
        private final Class<? super X> clazz;
        private final Predicate<X> predicate;
        private final Predicate<X> f;

        private CasePredicateImpl(Class<? super X> clazz, Predicate<X> predicate, Predicate<X> f) {
            this.clazz = clazz;
            this.predicate = predicate;
            this.f = f;
        }

        @Override
        public boolean matches(T t) {
            return clazz.isAssignableFrom(t.getClass()) && predicate.test((X) t);
        }

        @Override
        public boolean test(final T t) {
            return f.test((X) t);
        }
    }

    @SuppressWarnings("unchecked")
    public static class EitherCasePredicate<T, X> implements CasePredicate<T, X> {
        private final Either<Class<X>, Class<X>> either;
        private final Predicate<X> predicate;
        private final Predicate<X> f;

        private EitherCasePredicate(Either<Class<X>, Class<X>> either, Predicate<X> predicate, Predicate<X> f) {
            this.either = either;
            this.predicate = predicate;
            this.f = f;
        }

        @Override
        public boolean matches(T t) {
            return MatchingFunction.match_(
                    MatchingFunction._case(Either.Left.class, l -> either.getLeft()
                            .filter(c -> c.isAssignableFrom(l.getLeft().get().getClass()))
                            .filter(c -> predicate.test((X) l.getLeft().get())).isPresent()),
                    MatchingFunction._case(Either.Right.class, r -> either.getRight()
                            .filter(c -> c.isAssignableFrom(r.getRight().get().getClass()))
                            .filter(c -> predicate.test((X) r.getRight().get())).isPresent()),
                    MatchingFunction._default(s -> false)
            ).apply(t);
        }

        @Override
        public boolean test(final T t) {
            return match_(
                    _case(Either.Left.class, l -> f.test((X) l.getLeft().get())),
                    _case(Either.Right.class, r -> f.test((X) r.getRight().get()))
            ).test(t);
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
