package io.github.jebl01.bonjava.matching;

import static io.github.jebl01.bonjava.matching.MatchingConsumer.MatchingStrategy.ALL;
import static io.github.jebl01.bonjava.matching.MatchingConsumer.MatchingStrategy.MAX_ONCE;

import io.github.jebl01.bonjava.Either;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MatchingConsumer {
    public enum MatchingStrategy {
        ALL, MAX_ONCE
    }

    @SafeVarargs
    public static <T> CasesConsumer<T> match_(CaseConsumer<T, ?>... cases) {
        return new CasesConsumer<>(Arrays.asList(cases));
    }

    public static <T> CasesConsumer<T> match_(MatchingStrategy strategy, CaseConsumer<T, ?>... cases) {
        return new CasesConsumer<>(strategy, Arrays.asList(cases));
    }

    public static <T, X> CaseConsumer<T, X> _case(final Either<Class<X>, Class<X>> either, Consumer<X> f) {
        return new EitherCaseConsumer<>(either, t -> true, f, false);
    }

    public static <T, X> CaseConsumer<T, X> _case(final Either<Class<X>, Class<X>> either,
                                                  Predicate<X> predicate,
                                                  Consumer<X> f) {
        return new EitherCaseConsumer<>(either, predicate, f, false);
    }

    public static <T, X> CaseConsumer<T, X> _case(Predicate<T> predicate, Consumer<T> f) {
        return new CaseConsumer<T, X>() {
            public void accept(final T t) {
                f.accept(t);
            }

            public boolean matches(final T t) {
                return predicate.test(t);
            }

            public boolean isDefault() {
                return false;
            }
        };
    }

    public static <T, X> CaseConsumer<T, X> _case(Class<X> clazz, Consumer<X> consumer) {
        return new CaseConsumerImpl<>(clazz, t -> true, consumer, false);
    }

    public static <T, X> CaseConsumer<T, X> _case(Class<X> clazz, Predicate<X> predicate, Consumer<X> f) {
        return new CaseConsumerImpl<>(clazz, predicate, f, false);
    }

    public static <T> CaseConsumer<T, T> _default(Consumer<T> consumer) {
        return new CaseConsumerImpl<>(Object.class, t -> true, consumer, true);
    }

    private static class CasesConsumer<T> implements Consumer<T> {
        private final MatchingStrategy strategy;
        private List<CaseConsumer<T, ?>> cases;

        private CasesConsumer(List<CaseConsumer<T, ?>> cases) {
            this(ALL, cases);
        }

        private CasesConsumer(MatchingStrategy strategy, List<CaseConsumer<T, ?>> cases) {
            this.strategy = strategy;
            this.cases = cases;
        }

        @Override
        public void accept(T t) {
            boolean match = false;
            for(CaseConsumer<T, ?> _case : cases) {
                if(_case.matches(t)) {
                    match = true;
                    _case.accept(t);
                    if(strategy == MAX_ONCE) {
                        break;
                    }
                }
            }
            //only apply defaults if there was no match
            if(!match) {
                cases.stream()
                        .filter(CaseConsumer::isDefault)
                        .forEach(_case -> _case.accept(t));
            }
        }
    }

    public interface CaseConsumer<T, X> extends Consumer<T> {
        boolean matches(T t);

        boolean isDefault();
    }

    @SuppressWarnings("unchecked")
    public static class CaseConsumerImpl<T, X> implements CaseConsumer<T, X> {
        private final Class<? super X> clazz;
        private final Predicate<X> predicate;
        private final Consumer<X> consumer;
        private boolean isDefault;

        private CaseConsumerImpl(Class<? super X> clazz,
                                 Predicate<X> predicate,
                                 Consumer<X> consumer,
                                 boolean isDefault) {
            this.clazz = clazz;
            this.predicate = predicate;
            this.consumer = consumer;
            this.isDefault = isDefault;
        }

        @Override
        public boolean matches(T t) {
            return !isDefault && clazz.isAssignableFrom(t.getClass()) && predicate.test((X) t);
        }

        @Override
        public boolean isDefault() {
            return isDefault;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void accept(T t) {
            consumer.accept((X) t);
        }
    }

    @SuppressWarnings("unchecked")
    public static class EitherCaseConsumer<T, X> implements CaseConsumer<T, X> {
        private final Either<Class<X>, Class<X>> either;
        private final Predicate<X> predicate;
        private final Consumer<X> consumer;
        private boolean isDefault;

        private EitherCaseConsumer(Either<Class<X>, Class<X>> either,
                                   Predicate<X> predicate,
                                   Consumer<X> consumer,
                                   boolean isDefault) {
            this.either = either;
            this.predicate = predicate;
            this.consumer = consumer;
            this.isDefault = isDefault;
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
        public boolean isDefault() {
            return isDefault;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void accept(T t) {
            match_(
                    _case(Either.Left.class, l -> consumer.accept((X) l.getLeft().get())),
                    _case(Either.Right.class, r -> consumer.accept((X) r.getRight().get()))
            ).accept(t);
        }
    }
}
