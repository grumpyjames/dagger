package net.digihippo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
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
}
