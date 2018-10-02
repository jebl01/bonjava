package io.github.jebl01.babaco;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Tuple<T extends Tuple> {
    public <R> R map(Function<T, R> f) {
        return f.apply((T) this);
    }

    private static String[] splitInternal(final String string, final String regex, final int limit) {
        final String[] parts = string.split(regex);
        if(parts.length < limit) {
            throw new IllegalArgumentException("not enough parts");
        }
        return parts.length == limit ? parts : Arrays.copyOf(parts, limit);
    }

    private static String[] trim(final String[] parts, final boolean trim) {
        if(trim) {
            for(int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }
        }
        return parts;
    }

    public static <T1, T2> Tuple2<T1, T2> of(T1 v1, T2 v2) {
        return new Tuple2<>(v1, v2);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 v1, T2 v2, T3 v3) {
        return new Tuple3<>(v1, v2, v3);
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 v1, T2 v2, T3 v3, T4 v4) {
        return new Tuple4<>(v1, v2, v3, v4);
    }

    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> of(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
        return new Tuple5<>(v1, v2, v3, v4, v5);
    }

    public static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> of(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6) {
        return new Tuple6<>(v1, v2, v3, v4, v5, v6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> of(T1 v1,
                                                                                     T2 v2,
                                                                                     T3 v3,
                                                                                     T4 v4,
                                                                                     T5 v5,
                                                                                     T6 v6,
                                                                                     T7 v7) {
        return new Tuple7<>(v1, v2, v3, v4, v5, v6, v7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> of(T1 v1,
                                                                                             T2 v2,
                                                                                             T3 v3,
                                                                                             T4 v4,
                                                                                             T5 v5,
                                                                                             T6 v6,
                                                                                             T7 v7,
                                                                                             T8 v8) {
        return new Tuple8<>(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> of(T1 v1,
                                                                                                     T2 v2,
                                                                                                     T3 v3,
                                                                                                     T4 v4,
                                                                                                     T5 v5,
                                                                                                     T6 v6,
                                                                                                     T7 v7,
                                                                                                     T8 v8,
                                                                                                     T9 v9) {
        return new Tuple9<>(v1, v2, v3, v4, v5, v6, v7, v8, v9);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of(T1 v1,
                                                                                                                T2 v2,
                                                                                                                T3 v3,
                                                                                                                T4 v4,
                                                                                                                T5 v5,
                                                                                                                T6 v6,
                                                                                                                T7 v7,
                                                                                                                T8 v8,
                                                                                                                T9 v9,
                                                                                                                T10 v10) {
        return new Tuple10<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
    }

    public static class Tuple2<T1, T2> extends Tuple<Tuple2<T1, T2>> {
        public final T1 v1;
        public final T2 v2;

        public Tuple2(T1 v1, T2 v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        public static Tuple2<String, String> split(final String string, final String regex, final boolean trim) {
            final String[] parts = trim(splitInternal(string, regex, 2), trim);
            return of(parts[0], parts[1]);
        }

        public <R> R map(BiFunction<T1, T2, R> f) {
            return f.apply(v1, v2);
        }

        @Override
        public boolean equals(Object o) {
          if(this == o) {
            return true;
          }
          if(o == null || getClass() != o.getClass()) {
            return false;
          }
            Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
            return Objects.equals(v1, tuple2.v1) &&
                    Objects.equals(v2, tuple2.v2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v1, v2);
        }
    }

    public static class Tuple3<T1, T2, T3> extends Tuple<Tuple3<T1, T2, T3>> {
        public final T1 v1;
        public final T2 v2;
        public final T3 v3;

        public Tuple3(T1 v1, T2 v2, T3 v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        public static Tuple3<String, String, String> split(final String string,
                                                           final String regex,
                                                           final boolean trim) {
            final String[] parts = trim(splitInternal(string, regex, 3), trim);
            return of(parts[0], parts[1], parts[2]);
        }

        @Override
        public boolean equals(Object o) {
          if(this == o) {
            return true;
          }
          if(o == null || getClass() != o.getClass()) {
            return false;
          }
            Tuple3<?, ?, ?> tuple3 = (Tuple3<?, ?, ?>) o;
            return Objects.equals(v1, tuple3.v1) &&
                    Objects.equals(v2, tuple3.v2) &&
                    Objects.equals(v3, tuple3.v3);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v1, v2, v3);
        }
    }

    public static class Tuple4<T1, T2, T3, T4> extends Tuple<Tuple4<T1, T2, T3, T4>> {
        public final T1 v1;
        public final T2 v2;
        public final T3 v3;
        public final T4 v4;

        public Tuple4(T1 v1, T2 v2, T3 v3, T4 v4) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
        }

        public static Tuple4<String, String, String, String> split(final String string,
                                                                   final String regex,
                                                                   final boolean trim) {
            final String[] parts = trim(splitInternal(string, regex, 4), trim);
            return of(parts[0], parts[1], parts[2], parts[3]);
        }

        @Override
        public boolean equals(Object o) {
          if(this == o) {
            return true;
          }
          if(o == null || getClass() != o.getClass()) {
            return false;
          }
            Tuple4<?, ?, ?, ?> tuple4 = (Tuple4<?, ?, ?, ?>) o;
            return Objects.equals(v1, tuple4.v1) &&
                    Objects.equals(v2, tuple4.v2) &&
                    Objects.equals(v3, tuple4.v3) &&
                    Objects.equals(v4, tuple4.v4);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v1, v2, v3, v4);
        }
    }

    public static class Tuple5<T1, T2, T3, T4, T5> extends Tuple<Tuple5<T1, T2, T3, T4, T5>> {
        public final T1 v1;
        public final T2 v2;
        public final T3 v3;
        public final T4 v4;
        public final T5 v5;

        public Tuple5(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
        }

        public static Tuple5<String, String, String, String, String> split(final String string,
                                                                           final String regex,
                                                                           final boolean trim) {
            final String[] parts = trim(splitInternal(string, regex, 5), trim);
            return of(parts[0], parts[1], parts[2], parts[3], parts[4]);
        }

        @Override
        public boolean equals(Object o) {
          if(this == o) {
            return true;
          }
          if(o == null || getClass() != o.getClass()) {
            return false;
          }
            Tuple5<?, ?, ?, ?, ?> tuple5 = (Tuple5<?, ?, ?, ?, ?>) o;
            return Objects.equals(v1, tuple5.v1) &&
                    Objects.equals(v2, tuple5.v2) &&
                    Objects.equals(v3, tuple5.v3) &&
                    Objects.equals(v4, tuple5.v4) &&
                    Objects.equals(v5, tuple5.v5);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v1, v2, v3, v4, v5);
        }
    }

    public static class Tuple6<T1, T2, T3, T4, T5, T6> extends Tuple<Tuple6<T1, T2, T3, T4, T5, T6>> {
        public final T1 v1;
        public final T2 v2;
        public final T3 v3;
        public final T4 v4;
        public final T5 v5;
        public final T6 v6;

        public Tuple6(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
        }

        public static Tuple6<String, String, String, String, String, String> split(final String string,
                                                                                   final String regex,
                                                                                   final boolean trim) {
            final String[] parts = trim(splitInternal(string, regex, 6), trim);
            return of(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
        }

        @Override
        public boolean equals(Object o) {
          if(this == o) {
            return true;
          }
          if(o == null || getClass() != o.getClass()) {
            return false;
          }
            Tuple6<?, ?, ?, ?, ?, ?> tuple6 = (Tuple6<?, ?, ?, ?, ?, ?>) o;
            return Objects.equals(v1, tuple6.v1) &&
                    Objects.equals(v2, tuple6.v2) &&
                    Objects.equals(v3, tuple6.v3) &&
                    Objects.equals(v4, tuple6.v4) &&
                    Objects.equals(v5, tuple6.v5) &&
                    Objects.equals(v6, tuple6.v6);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v1, v2, v3, v4, v5, v6);
        }
    }

    public static class Tuple7<T1, T2, T3, T4, T5, T6, T7> extends Tuple<Tuple7<T1, T2, T3, T4, T5, T6, T7>> {
        public final T1 v1;
        public final T2 v2;
        public final T3 v3;
        public final T4 v4;
        public final T5 v5;
        public final T6 v6;
        public final T7 v7;

        public Tuple7(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
        }

        public static Tuple7<String, String, String, String, String, String, String> split(final String string,
                                                                                           final String regex,
                                                                                           final boolean trim) {
            final String[] parts = trim(splitInternal(string, regex, 7), trim);
            return of(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
        }

        @Override
        public boolean equals(Object o) {
          if(this == o) {
            return true;
          }
          if(o == null || getClass() != o.getClass()) {
            return false;
          }
            Tuple7<?, ?, ?, ?, ?, ?, ?> tuple7 = (Tuple7<?, ?, ?, ?, ?, ?, ?>) o;
            return Objects.equals(v1, tuple7.v1) &&
                    Objects.equals(v2, tuple7.v2) &&
                    Objects.equals(v3, tuple7.v3) &&
                    Objects.equals(v4, tuple7.v4) &&
                    Objects.equals(v5, tuple7.v5) &&
                    Objects.equals(v6, tuple7.v6) &&
                    Objects.equals(v7, tuple7.v7);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v1, v2, v3, v4, v5, v6, v7);
        }
    }

    public static class Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> extends Tuple<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> {
        public final T1 v1;
        public final T2 v2;
        public final T3 v3;
        public final T4 v4;
        public final T5 v5;
        public final T6 v6;
        public final T7 v7;
        public final T8 v8;

        public Tuple8(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
        }

        public static Tuple8<String, String, String, String, String, String, String, String> split(final String string,
                                                                                                   final String regex,
                                                                                                   final boolean trim) {
            final String[] parts = trim(splitInternal(string, regex, 8), trim);
            return of(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        }

        @Override
        public boolean equals(Object o) {
          if(this == o) {
            return true;
          }
          if(o == null || getClass() != o.getClass()) {
            return false;
          }
            Tuple8<?, ?, ?, ?, ?, ?, ?, ?> tuple8 = (Tuple8<?, ?, ?, ?, ?, ?, ?, ?>) o;
            return Objects.equals(v1, tuple8.v1) &&
                    Objects.equals(v2, tuple8.v2) &&
                    Objects.equals(v3, tuple8.v3) &&
                    Objects.equals(v4, tuple8.v4) &&
                    Objects.equals(v5, tuple8.v5) &&
                    Objects.equals(v6, tuple8.v6) &&
                    Objects.equals(v7, tuple8.v7) &&
                    Objects.equals(v8, tuple8.v8);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v1, v2, v3, v4, v5, v6, v7, v8);
        }
    }

    public static class Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> extends Tuple<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> {
        public final T1 v1;
        public final T2 v2;
        public final T3 v3;
        public final T4 v4;
        public final T5 v5;
        public final T6 v6;
        public final T7 v7;
        public final T8 v8;
        public final T9 v9;

        public Tuple9(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
            this.v9 = v9;
        }

        public static Tuple9<String, String, String, String, String, String, String, String, String> split(final String string,
                                                                                                           final String regex,
                                                                                                           final boolean trim) {
            final String[] parts = trim(splitInternal(string, regex, 9), trim);
            return of(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]);
        }

        @Override
        public boolean equals(Object o) {
          if(this == o) {
            return true;
          }
          if(o == null || getClass() != o.getClass()) {
            return false;
          }
            Tuple9<?, ?, ?, ?, ?, ?, ?, ?, ?> tuple9 = (Tuple9<?, ?, ?, ?, ?, ?, ?, ?, ?>) o;
            return Objects.equals(v1, tuple9.v1) &&
                    Objects.equals(v2, tuple9.v2) &&
                    Objects.equals(v3, tuple9.v3) &&
                    Objects.equals(v4, tuple9.v4) &&
                    Objects.equals(v5, tuple9.v5) &&
                    Objects.equals(v6, tuple9.v6) &&
                    Objects.equals(v7, tuple9.v7) &&
                    Objects.equals(v8, tuple9.v8) &&
                    Objects.equals(v9, tuple9.v9);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v1, v2, v3, v4, v5, v6, v7, v8, v9);
        }
    }

    public static class Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> extends Tuple<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>> {
        public final T1 v1;
        public final T2 v2;
        public final T3 v3;
        public final T4 v4;
        public final T5 v5;
        public final T6 v6;
        public final T7 v7;
        public final T8 v8;
        public final T9 v9;
        public final T10 v10;

        public Tuple10(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
            this.v9 = v9;
            this.v10 = v10;
        }

        public static Tuple10<String, String, String, String, String, String, String, String, String, String> split(
                final String string,
                final String regex,
                final boolean trim) {
            final String[] parts = trim(splitInternal(string, regex, 10), trim);
            return of(parts[0],
                    parts[1],
                    parts[2],
                    parts[3],
                    parts[4],
                    parts[5],
                    parts[6],
                    parts[7],
                    parts[8],
                    parts[9]);
        }

        @Override
        public boolean equals(Object o) {
          if(this == o) {
            return true;
          }
          if(o == null || getClass() != o.getClass()) {
            return false;
          }
            Tuple10<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> tuple10 = (Tuple10<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) o;
            return Objects.equals(v1, tuple10.v1) &&
                    Objects.equals(v2, tuple10.v2) &&
                    Objects.equals(v3, tuple10.v3) &&
                    Objects.equals(v4, tuple10.v4) &&
                    Objects.equals(v5, tuple10.v5) &&
                    Objects.equals(v6, tuple10.v6) &&
                    Objects.equals(v7, tuple10.v7) &&
                    Objects.equals(v8, tuple10.v8) &&
                    Objects.equals(v9, tuple10.v9) &&
                    Objects.equals(v10, tuple10.v10);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
        }
    }
}
