# Bonjava
The fun fun functional java library!

**zero dependencies™**

**Get it from Maven Central**<br>
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jebl01/bonjava.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:io.github.jebl01%20AND%20a:bonjava)

```xml
<dependency>
    <groupId>io.github.jebl01</groupId>
    <artifactId>bonjava</artifactId>
    <version>0.1</version>
</dependency>
```

* [Tuple](#tuple)
* [Either](#either)
* [Optionals](#optionals)
* [SideEffects](#sideeffects)
* [ExceptionHandling](#exceptionhandling)
* [SneakyThrow](#sneakythrow)
* [Retry](#retry)
* [Matching](#matching)

## Tuple
Supported tuples are Tuple2 -> Tuple10

**Operations**
* `map(Function<TupleN, R> f) : R`
* `TupleN.split(String string, String regex, boolean trim) : TupleN<String, String, String>`

For *Tuple2* there is an alternative *map* method accepting a *BiFunction* (`map(BiFunction<T1, T2, R> f) : R`).

```java
Tuple3<String, Boolean, Long> tuple = Tuple.of("String", true, 1L);
```

## Either
An Either can be either left or right. Right is used to communicate success and left is used to communicate an error (or a warning, bad karma etc).

**Operations**
* `isLeft() : Boolean`
* `isRight() : Boolean`
* `getLeft() : Optional<L>`
* `getRight() : Optional<R>`
* `map(Function<R, T> f) : Either<L, T>`
* `flatMap(Function<R, Either<L, T> f) : Either<L, T>`
* `consume(Consumer<Either<L, R>> consumer): void`
* `get(Function<? super L, Optional<T>> leftSink, Function<? super R, Optional<T>> rightSink): Optional<T>`
* `ifLeft(Consumer<L> consumer): void`
* `ifRight(Consumer<R> consumer): void`
* `thisOrThat(<Either<L, R>> thiz, Supplier<Either<L, R>> thatSupplier): Either<L, R>`
* `getOrThrow(Function<L, Throwable> f): R`
* Static factory methods
* `right(R) : Either<L, R>`
* `left(L) : Either<L, R>`
* `fromNullable(R value, Supplier<L> leftSupplier) : Either<L, R>`
* `fromOptional(Optional<R> value, Supplier<L> leftSupplier) : Either<L, R>`


### The basics
You can create an Either using the two factory methods `left(T value)` or `right(T value)`.
If the value might be null, use `fromNullable` and provide a left side supplier.

```java
Either<String, User> result = right(new User());
```

or

```java
Either<String, User> result = left("boo-boo");
```

or

```java
Either<String, User> result = fromNullable(userObjectThatMightBeNull, () -> "boo-boo");
```

An Either is "right biased" which means that operations (e.g. `map()`) on the Either will be performed on the right side value.

### Motivation

When mapping over e.g. an Optional, the only way to communicate an error is to throw an exception. An other way to solve this is to flat map and return empty when there is an error.
The problem with the former solution is that you use exceptions to communicate what could be a fully normal situation. Hence using exceptions as flow control.
The problem with the latter solution is of course that you lose the possibility to e.g. log what happened.

The following code tries to read a system variable and then parse it as an integer. Errors are communicated using exceptions.

```java
try {
    Optional<Integer> result = Optional.of("PROPERTY")
        .map(propertyName -> {
            String value = System.getProperty(propertyName);
            if (value == null) {
                throw new RuntimeException("bad property name: " + propertyName);
            }
            return value;
        })
        .map(value -> {
            try {
                return Integer.valueOf(value);
            }
            catch (NumberFormatException e) {
                throw new RuntimeException("property value is not numeric: " + value);
            }
        });
}
catch(RuntimeException e) {
    log.warning("failed to read system property as int", e);
}
```

The next example has the same goal, but since we flat map, we have to log in every step, or we will lose the reason why we failed.

```java
Optional<Integer> result = Optional.of("PROPERTY")
        .flatMap(propertyName -> Optional.ofNullable(System.getProperty(propertyName)))
        .flatMap(value -> {
            try {
                return Optional.of(Integer.valueOf(value));
            }
            catch (NumberFormatException e) {
                return Optional.empty();
            }
        });

if(!result.isPresent()) {
    //log a generic log message, since we don't know the exact reason
    log.warning("failed to read system property as int")
}
```

With Either, we can eject without exceptions while still communicating what went wrong!

```java
public Either<String, Integer> getIntProperty(String propertyName) {
    return fromNullable(
        System.getProperty(propertyName),
        () -> "property not found: " + propertyName)
        .flatMap(propertyValue -> {
            try {
                return right(Integer.valueOf(propertyValue));
            } catch (NumberFormatException e) {
                return left("property is not numeric: " + propertyName);
            }
        });
}
```
See "[exception handling](#exceptionhandling)" for an even neater way to write the above code!

## Optionals
When Oracle released Java 8, they finally provided an Optional class. Sadly the implementation falls a bit short. You can map and flat map, but that's more or less it.

Optionals is a helper class making it easier to do real work with java's Optional class!

**Operations**
* `map2(BiFunction<T, U, R> f) : BiFunction<Optional<T>, Optional<U>, Optional<R>>`
* `flatMap2(BiFunction<T, U, Optional<R>> f) : BiFunction<Optional<T>, Optional<U>, Optional<R>>`
* `toStream(Function<T, Stream<R>> f) : Function<Optional<T>, Stream<R>>`
* `toStream(Optional<T> o) : Stream<T>`
* `toStream(Optional<T>... o) : Stream<T>`
* `toStream(Iterable<Optional<T>> o) : Stream<T>`
* `mapToEither(Function<T, Either<L, R>> f, Supplier<L> leftSupplier) : Function<Optional<T>, Either<L, R>>`
* `map2ToEither(BiFunction<T, U, Either<L, R>> f, Supplier<L> leftSupplier) : BiFunction<Optional<T>, Optional<U>, Either<L, R>>`
* `toTuple(Optional<A> a, Optional<B> b): Optional<Tuple2<A, B>>`
* `thisOrThat(Optional<T> thiz, Supplier<T> thatSupplier) : Optional<T>`
* `ifPresent(Optional<T> optional, Consumer<T> consumer) : Optional<T>`
* `ifPresentOrElse(Optional<T> optional, Consumer<T> consumer, Runnable elseRunner) : void`

### The basics
While the `map` and `flatMap` functions are a big leap forward, there is no way to combine Optional's in Oracle's implementation.
With `map2` and `flatMap2` you can combine two Optional's to produce a third. Much like `zip` for *Observables* (RxJava) or `lift2` in the eminent *fuge* library (Atlassian).
The short description of map2 (and even for flatMap2) is actually the same as for `lift2` in *fugue*;

*"Returns a function that will lift a function that takes a T and a U and returns a R into a function that takes an Optional of T and an Optional of U and returns an Optional of R."*

```java
Optional<String> optFirst = Optional.of("Carl");
Optional<String> optLast = Optional.of("Hamilton");

Optional<String> result = map2((String first, String last) -> first + " " + last).apply(optFirst, optLast);

//if either optFirst or optLast (or both) was empty, result would be empty.
```

`toStream` does just what it says. It will lift a function that takes a T and returns a Stream<R> into a function that takes an Optional of T and returns a Stream of R.
If the Optional to apply the returned function to is empty, an empty Stream of R will be returned.

```java
Optional<String> withValue = Optional.of("1,2,3,4,5");

Stream<String> result = toStream((String value) -> Stream.of(value.split(","))).apply(withValue);

//if withValue was empty, result would be an empty Stream.
```

`toStream` also has a **more simple** implementation to be used when all that is wanted is to transform one or more Optionals into a stream of entities of the same type:

```java
Optional<String> withValue1 = Optional.of("a value");
Optional<String> withValue2 = Optional.of("a value");
Optional<String> withoutValue = Optional.empty();
Iterable<Optional<String>> optionals = Arrays.asList(withValue1, withoutValue, withValue2);

Stream<String> result1 = toStream(withValue1); // will produce a stream of length 1
Stream<String> result2 = toStream(withoutValue); // will produce a stream of length 0
Stream<String> result3 = toStream(withValue1, withoutValue, withValue2); // will produce a stream of length 2
Stream<String> result4 = toStream(optionals); // will produce a stream of length 2
```

`mapToEither` is used to map an `Optional` into an `Either`.
`map2ToEither` is like a combination of `map2` and `mapToEither`.
It will lift a function that takes a T and a U and returns an Either of L and R into a function that takes an Optional of T, an Optional of U and returns an Either of L and R.
If either (or both) of the Optional's are empty, the `leftSupplier` will be used to provide a left side value.

```java
Optional<String> optWithValueA = Optional.of("A");

Either<String, String> result = mapToEither((String v) -> right(v + ":" + v), () -> "fail").apply(optWithValueA);

//if optWithValueA was empty, result would be a "left" containing the string "fail".
```

`toTuple` is a convenience method that utilizes `map2` to create an `Optional<Tuple2>` of two input Optionals.

`thisOrThat` will either return the given optional if present or exercise the provided supplier.

```java
Optional<String> result = thisOrThat(thisOptional, thatProvider::getOptional);
```

### Motivation
Instead of having code like this:
```java
Optional<Oxygen> oxygen = getOxygen();
Optional<Hydrogen> hydrogen = getHydrogen();

if(oxygen.isPresent()) {
    if(hydrogen.isPresent()) {
        return Optional.of(makeWater(oxygen.get(), hydrogen.get()));
    }
}
return Optional.empty();
```

You could simply write:
```java
Optional<Oxygen> oxygen = getOxygen();
Optional<Hydrogen> hydrogen = getHydrogen();

return map2((Oxygen o, Hydrogen h) -> makeWater(o, h)).apply(oxygen, hydrogen);
```

## SideEffects
Since functions like mapping operations is not supposed to have side effects (a side effect can be e.g. logging), it's more communicative to wrap the function in a `side effect` in case a side effect is nonetheless desired.
In the pure form a side effect can only perform some sort of operation on the returned result from a function.

**Operations**
* `withSideEffect(Function<T, R> f, Consumer<R> effect) : Function<T, R>`
* `withSideEffect2(BiFunction<T, U, R> f, Consumer<R> effect) : BiFunction<T, U, R>`
* `withSideEffectSupply(Supplier<T> supplier, Consumer<T> effect) : Supplier<T>`

### The basics
The most basic example shows how a side effect can output the result of the mapping to the console.
The format is:
`<sideEffectFunction>(<originalFunction>, <sideEffectConsumer>)`


**When mapping over an Optional**
```java
Optional<String> result = Optional.of("test").map(
                withSideEffect(
                        String::toUpperCase,
                        System.out::println));

//result will contain the string "TEST"
//console output:
//TEST
```

**When mapping over a Stream**
```java
String result = Stream.of("stream", "of", "values").map(
                    withSideEffect(
                            String::toUpperCase,
                            System.out::println)).collect(Collectors.joining(","));

//result will contain the string "STREAM,OF,VALUES"
//console output:
// STREAM
// OF
// VALUES
```

**BiFunction version**
```java
Optional<String> value1 = Optional.of("A");
Optional<String> value2 = Optional.of("B");

Optional<String> result = map2(
        withSideEffect2(
                (String v1, String v2) -> v1 + ":" + v2,
                System.out::println)).apply(value1, value2);

//result will contain the string "A:B"
//console output:
// A:B
```

## ExceptionHandling

The `withExceptionHandler` functions are used to wrap functions with an exception handler.
Much like side effects can "trap" the returned value from any function (or biFunction) and perform a side effect on it, withExceptionHandler(*) can trap thrown exceptions. But not only can it perform a side effect on the exception, it can also rethrow it (or an other exception) or decide to return a value to the outer scope.

> Tip! Read about how to rethrow checked exceptions in the [SneakyThrow](#sneakythrow) section.

**Operations**
* `withExceptionHandler(Function<T, R> f, Function<Throwable, R> exceptionHandler) : Function<T, R>`
* `withExceptionHandler2(BiFunction<T, U, R> f, Function<Throwable, R> exceptionHandler) : BiFunction<T, U, R>`
* `withExceptionHandlerSupply(Supplier<T> f, Function<Throwable, T> exceptionHandler) : Supplier<T>`
* `withExceptionHandlerRun(Runnable f, Consumer<Throwable> exceptionHandler) : Runnable`
* `withExceptionHandlerConsume(Consumer<T> f, Consumer<Throwable> exceptionHandler) : Consumer<T>`
* `withExceptionHandlerConsume2(BiConsumer<T, U> f, Consumer<Throwable> exceptionHandler) : BiConsumer<T, U>`

### Motivation
Take the previous example from the [Either](#either) section:

```java
public Either<String, Integer> getIntProperty(String propertyName) {
    return fromNullable(
        System.getProperty(propertyName),
        () -> "property not found: " + propertyName)
        .flatMap(propertyValue -> {
            try {
                return right(Integer.valueOf(propertyValue));
            } catch (NumberFormatException e) {
                return left("property is not numeric: " + propertyName);
            }
        });
}
```

The second mapping operation can clearly throw exceptions. A cleaner approach would be to wrap the mapping function in a `withExceptionHandler` wrapper, and provide a handler.

Same example as above, but with an ExceptionHandler:

```java
public Either<String, Integer> getIntProperty(String propertyName) {
    return fromNullable(
        System.getProperty(propertyName),
        () -> "property not found: " + propertyName)
        .flatMap(
            withExceptionHandler(
                propertyValue -> right(Integer.valueOf(propertyValue)),
                e -> left("property is not numeric: " + propertyName)));
}
```


## SneakyThrow
Ok, checked exceptions. Good or bad?
Let's not go into that here and now, but only conclude that they in the context of Java lambdas are particularly bad.
There is namely no way that you will be able to throw them from within a Function. Not without a neat little (type erasure) trick anyway. Enters - SneakyThrow

Read more about sneaky throws here:
* https://projectlombok.org/features/SneakyThrows.html
* http://proofbyexample.com/sneakythrow-avoid-checked-exceptions.html

**Operations**
* `withSneakyExceptions(SneakyFunction<T, R> f) : Function<T, R>`
* `withSneakyExceptions2(SneakyBiFunction<T, U, R> f) : BiFunction<T, U, R>`
* `withSneakyExceptionsRun(SneakyRunnable f) : Runnable`
* `withSneakyExceptionsConsume(SneakyConsumer<T> f) : Consumer<T>`
* `withSneakyExceptionsConsume2(SneakyBiConsumer<T> f) : Consumer<T>`
* `withSneakyExceptionsSupply(SneakySupplier<T> f) : Supplier<T>`

### The basics
The SneakyThrow helper class exposes alternative functional interfaces that allows checked exceptions to be thrown. They are just cogs in the machinery that you don't have to think about.

`withSneakyExceptions` will accept a `SneakyFunction` that takes a T and returns an R and lift it into a Function that takes a T and returns an R while providing shading of checked exceptions. The rest of the operations operate in a similar manner.


### Motivation
Consider the code from the [ExceptionHandling](#exceptionhandling) section:
```java
public Either<String, Integer> getIntProperty(String propertyName) {
    return fromNullable(
        System.getProperty(propertyName),
        () -> "property not found: " + propertyName)
        .flatMap(
            withExceptionHandler(
                propertyValue -> right(Integer.valueOf(propertyValue)), 
                e -> left("property is not numeric: " + propertyName)));
}
```

This can only look this neat thanks to the fact that `Integer.valueOf(value)` throws an unchecked exception (`NumberFormatException`). If `NumberFormatException` was checked we would get a compile error. This is the case in the following example:

```java
public Either<String, Integer> getUrlFromProperty(String propertyName) {
    return fromNullable(
        System.getProperty(propertyName),
        () -> "property not found: " + propertyName)
        .flatMap(
            withExceptionHandler(
                propertyValue -> right(new URL(value)), //Error on this line!!!!
                e -> left("property is not a valid URL: " + propertyName)));
}
```

This example wouldn't compile. You will get an error stating: *"java: unreported exception java.net.MalformedURLException; must be caught or declared to be thrown"*.

This boo-boo can be solved by wrapping the function `propertyValue -> right(new URL(value))` in a `withSneakyExceptions` function.
Malformed URL's will still throw `MalformedURLException` and if you don't want an exception handler like in our example, you can still catch the exception at some other level with a `catch (MalformedURLException e)` clause.

This is how the above would look with a `withSneakyExceptions` wrapper!

```java
public Either<String, URL> getUrlFromProperty(String propertyName) {
    return fromNullable(
        System.getProperty(propertyName),
        () -> "property not found: " + propertyName)
        .flatMap(
            withExceptionHandler(
                withSneakyExceptions(propertyValue -> right(new URL(propertyValue))),
                e -> left("property is not a valid URL: " + propertyName)));
}
```

## Retry
`Retry` contains wrapper functions that allow for code to execute with retries. Methods returning `Either` are convenience methods that accepts an errorMapper which produces a left side result. If error handling is desired for the other methods, wrap them in a [*withExceptionHandler*](#exceptionhandling) wrapper.

**Operations**
* `withRetries(Function<T, R> f, Predicate<R> p, int retries, long millisecWait, double backingOffMultiplier) : Function<T, R>`
* `withRetries(Function<T, R> f, Predicate<R> p, final Function<Exception, L> errorMapper, int retries, long millisecWait, double backingOffMultiplier) : Function<T, Either<L, R>>`
* `withRetriesSupply(Supplier<T> f, Predicate<T> p, int retries, long millisecWait, double backingOffMultiplier) : Supplier<T>`
* `withRetriesSupply(Supplier<R> f, Predicate<R> p, Function<Exception, L> errorMapper, int retries, long millisecWait, double backingOffMultiplier) : Supplier<Either<L, R>>`
* `withRetriesRun(Runnable f, Consumer<Throwable> errorHandler, int retries, long millisecWait, double backingOffMultiplier) : Runnable`

The example below uses the previously explored `getUrlFromProperty` to get a `URL` from a property, it then tries to load an image from the remote using a retry wrapper function.

```java
public Either<String, BufferedImage> getImgage(String propertyName, int minWidth) {
    return getUrlFromProperty(propertyName).flatMap(
        withRetries(
            this::getImage, //load the image
            image -> image.getWidth() >= minWidth, //verify the response (a bit awkward perhaps :-)
            error -> "failed to get image from remote: " + error.getMessage(), //provide a left side in case of an error
            5, //do five retries
            1000, //wait for 1000 milliseconds before retrying
            2)); //backoff multiplier (wait for the initial 1000 milliseconds before retrying the first time, then 2 * 1000, then 2 * 2000 etc)
}
```

## Matching
In Java there is *if/else*, *switch* and *try/catch* at hand if you want to do any kind of matching and catching. None of these are pariculary useful when going "functional" in java. What you really want is something like Scalas pattern matching. But is that possible in Java? Not really, but we can do quite a lot.

Matching in *bonjava* is split into three types, *matching function*, *matching consumer* and *matching predicate*
The API is really simple, all you have is three static methods `match_`, `_case` and `_default`, each in *"MatchingFunction"*, *"MatchingConsumer"* and *"MatchingPredicate"*.

A typical matching function where we match on different exceptions could look like this:
```java
match_(
    _case(IOException.class, e -> "io"),
    _case(RuntimeException.class, e -> "runtime"),
    _case(IllegalArgumentException.class, e -> "illegal")
    _default(e -> "unknown exception"));
```

When matching using a *matching function* or a *matching predicate*, the ordering of `_case` statements is important, since the matching is done from top to bottom.

When matching using a *matching consumer*, **all matching cases are executed**. The default is only matched if no other matches could be made, this is true for all matchers.
This behaviour can be overridden be setting the *strategy* to *MAX_ONCE* (default is *ALL*).
Types are matched using `isAssignableFrom`, so even super types will match (e.g. `_case(Animal.class,...)` will match objects of type Dog, if Animal is a super type of Dog).

*matching function* and *matching predicate* will throw an exception if no match could be found, so **don't forget** your ***_default***!

**Example with matching function**
```java
public static abstract class Animal {
    public final String name;

    public Animal(String name) {
        this.name = name;
    }
}

public static class Dog extends Animal {
    public Dog(final String name) {
        super(name);
    }
}

public static class Cat extends Animal {
    public Cat(final String name) {
        super(name);
    }
}

public static class SnowMan extends Animal {
    public SnowMan(final String name) {
        super(name);
    }
}

public static class Flower {}

public void matchTest() {
    Stream.of(new Dog("Fido"), new Cat("Missie"), new SnowMan("Coco"), new Dog("Ralf"), new Cat("Kissie"), new Flower())
        .map(
            match_(
                _case(Dog.class, dog -> dog.name + " is a dog!"),
                _case(Cat.class, cat -> cat.name + " is a cat!"),
                _case(Animal.class, animal -> animal.name + " is a " + animal.getClass().getSimpleName().toLowerCase() + "!"),
                _default(unknown -> unknown.getClass().getSimpleName() + " is not an animal!")
            )
        ).forEach(System.out::println);
}

//Console output:
//Fido is a dog!
//Missie is a cat!
//Coco is a snowman!
//Ralf is a dog!
//Kissie is a cat!
//Flower is not an animal!
```

### Matching on types with predicates
> Do not confuse this with *matching predicate*!

Standard *type matching* can be extended with predicates. This makes it possible to also test some property of the object matched.

**Example of matching consumer with predicates**

Note that all matching cases are executed!

```java
Stream.of(1, 2, 1L, 2L, 1f, 2f, 1d, 2d)
    .forEach(match_(
        _case(Number.class, n -> System.out.print("here comes an ")),
        _case(Integer.class, i -> i % 2 == 0, i -> System.out.println("even int: " + i)),
        _case(Integer.class, i -> i % 2 == 1, i -> System.out.println("odd int: " + i)),
        _case(Long.class, l -> l % 2 == 0, l -> System.out.println("even long: " + l)),
        _case(Long.class, l -> l % 2 == 1, l -> System.out.println("odd long: " + l)),
        _case(Float.class, f -> f % 2 == 0, f -> System.out.println("even float: " + f)),
        _case(Float.class, f -> f % 2 == 1, f -> System.out.println("odd float: " + f)),
        _case(Double.class, d -> d % 2 == 0, d -> System.out.println("even double: " + d)),
        _case(Double.class, d -> d % 2 == 1, d -> System.out.println("odd double: " + d)),
        _default(o -> System.out.println("Since all objects can be matched, we won't see this"))
    ));

//Console output:
//here comes an odd int: 1
//here comes an even int: 2
//here comes an odd long: 1
//here comes an even long: 2
//here comes an odd float: 1.0
//here comes an even float: 2.0
//here comes an odd double: 1.0
//here comes an even double: 2.0
```

To handle errors, just wrap the `match_` statement in an [exception handler](#exceptionhandling), or for more fine grained control, wrap the consumer or function inside the `_case` statement.

**Example**
```java
Stream.of(0, 1, 2)
    .forEach(match_(
        _case(Number.class, n -> System.out.println("Matched a number: " + n)),
        _case(Integer.class, withExceptionHandlerConsume(
            i -> System.out.println("\t10 / " + i + " = " + 10 / i),
            e -> System.out.println("\tCould not devide 10 with provided number")))
    ));

//Console output:
//Matched a number: 0
//	Could not devide 10 with provided number
//Matched a number: 1
//	10 / 1 = 10
//Matched a number: 2
//	10 / 2 = 5
```
### Matching using only predicates
This works just like [Matching on types with predicates](#matching-on-types-with-predicates), but without the type matching. This can be a great alternative when you know the type!

**Example**
```java
final Function<Tuple2<String, String>, String> f = match_(
    _case(s -> s.v1.startsWith("a"), s -> s.v1 + s.v2 + ":first"),
    _case(s -> s.v1.startsWith("b"), s -> s.v1 + s.v2 + ":second"),
    _case(s -> s.v1.startsWith("c"), s -> s.v1 + s.v2 + ":third"),
    _default(s -> s.v1 + s.v2 + ":default")
);

assertEquals("a_test:first", f.apply(Tuple.of("a_", "test")));
assertEquals("b_test:second", f.apply(Tuple.of("b_", "test")));
assertEquals("c_test:third", f.apply(Tuple.of("c_", "test")));
assertEquals("no_match:default", f.apply(Tuple.of("no_", "match")));
```

#### Matching on [Either](#either)
Either has a special status when it comes to matching. Alongside just matching on *Left* and *Right* types, it's possible to match on both *Left* and *Right* as well on the generic type. When matching is done on the generic type, the predicate (if used) and the wrapped function / consumer operates on the contained value of the matched *Either*

**Example without predicate**
```java
final Either<String, String> rightStringA = right("A");
final Either<String, String> leftStringA = left("A");
final Either<String, Integer> rightInt1 = right(1);
final Either<Integer, Integer> leftInt1 = left(1);

final Function<Either, String> f = match_(
    _case(right(String.class), s -> "right string " + s),
    _case(left(String.class), s -> "left string " + s),
    _case(right(Integer.class), i -> "right int " + i),
    _case(left(Integer.class), i -> "left int " + i),
    _default(s -> s + "no match")
);

System.out.println(f.apply(rightStringA));
System.out.println(f.apply(leftStringA));
System.out.println(f.apply(rightInt1));
System.out.println(f.apply(leftInt1));

//Console output:
//right string A
//left string A
//right int 1
//left int 1
```

**Example with predicate**
```java
Either<String, String> rightStringA = right("A");
Either<String, String> rightStringB = right("B");
Either<String, String> leftStringA = left("A");
Either<String, String> leftStringB = left("B");
Either<String, Integer> rightInt1 = right(1);
Either<String, Integer> rightInt2 = right(2);
Either<Integer, Integer> leftInt1 = left(1);
Either<Integer, Integer> leftInt2 = left(2);

final Function<Either, String> f = match_(
    _case(right(String.class), "A"::equals, s -> "right string A"),
    _case(right(String.class), "B"::equals, s -> "right string B"),
    _case(left(String.class), "A"::equals, s -> "left string A"),
    _case(left(String.class), "B"::equals, s -> "left string B"),
    _case(right(Integer.class), i -> i == 1, i -> "right int 1"),
    _case(right(Integer.class), i -> i == 2, i -> "right int 2"),
    _case(left(Integer.class), i -> i == 1, i -> "left int 1"),
    _case(left(Integer.class), i -> i == 2, i -> "left int 2"),
    _default(s -> s + "no match")
);

System.out.println(f.apply(rightStringA));
System.out.println(f.apply(rightStringB));
System.out.println(f.apply(leftStringA));
System.out.println(f.apply(leftStringB));
System.out.println(f.apply(rightInt1));
System.out.println(f.apply(rightInt2));
System.out.println(f.apply(leftInt1));
System.out.println(f.apply(leftInt2));

//Console output:
//right string A
//right string B
//left string A
//left string B
//right int 1
//right int 2
//left int 1
//left int 2
```

A typlical use case for matching could be to match exceptions by type in an exception handler:

```java
public Either<String, Integer> divideByProperty(String propertyName, int value) {
    return fromNullable(
        System.getProperty(propertyName),
        () -> "property not found: " + propertyName)
        .flatMap(
            withExceptionHandler(
                propertyValue -> right(value / Integer.valueOf(propertyValue)),
                match_(
                    _case(NumberFormatException.class, e -> left("property is not numeric: " + propertyName)),
                    _case(ArithmeticException.class, e -> left("division by zero")),
                    _default(e -> Either.<String, Integer>left("unknown error"))
            )));
}
```
