package net.digihippo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.digihippo.Suppliers.wrapExceptions;

class ImmediateExecutor implements Executor {
    @Override
    public <S, T> CompletableFuture<Result<T>> map(CompletableFuture<Result<S>> futureS, Function<S, T> f) {
        return futureS.thenApply(r -> r.map(f));
    }

    @Override
    public <T> CompletableFuture<Result<T>> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.completedFuture(wrapExceptions(supplier).get());
    }

    @Override
    public <T> CompletableFuture<Result<T>> supplyAsync(Supplier<T> supplier, Duration timeout) {
        return CompletableFuture.completedFuture(wrapExceptions(supplier).get());
    }

    @Override
    public <T, S1, S2> CompletableFuture<Result<T>> mapTwo(
            CompletableFuture<Result<S1>> resultOne,
            CompletableFuture<Result<S2>> resultTwo,
            BiFunction<S1, S2, T> bif) {
        return resultOne.thenCombine(resultTwo, (r1, r2) -> r1.flatMap(s1 -> r2.map(s2 -> bif.apply(s1, s2))));
    }
}
