package net.digihippo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

interface Executor {
    <S, T> CompletableFuture<T> map(CompletableFuture<S> futureS, Function<S, T> f);

    <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier);

    <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, Duration timeout);
}
