package net.digihippo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

interface Executor {
    <S, T> CompletableFuture<Result<T>> map(CompletableFuture<Result<S>> futureS, Function<S, T> f);

    <T> CompletableFuture<Result<T>> supplyAsync(Supplier<T> supplier);

    <T> CompletableFuture<Result<T>> supplyAsync(Supplier<T> supplier, Duration timeout);

    <T, S1, S2> CompletableFuture<Result<T>> mapTwo(
            CompletableFuture<Result<S1>> resultOne,
            CompletableFuture<Result<S2>> resultTwo,
            BiFunction<S1, S2, T> bif);
}
