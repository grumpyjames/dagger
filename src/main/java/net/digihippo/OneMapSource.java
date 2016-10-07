package net.digihippo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

class OneMapSource<S, T> implements OneSource<T> {
    private final OneSource<S> oneSource;
    private final Function<S, T> f;

    OneMapSource(OneSource<S> oneSource, Function<S, T> f) {
        this.oneSource = oneSource;
        this.f = f;
    }

    @Override
    public CompletableFuture<Result<T>> asyncExec(Executor executor) {
        final CompletableFuture<Result<S>> futureS = oneSource.asyncExec(executor);
        return executor.map(futureS, f);
    }

    @Override
    public CompletableFuture<Result<T>> asyncExec(Executor executor, Duration timeout) {
        final CompletableFuture<Result<S>> source = oneSource.asyncExec(executor, timeout);
        return executor.map(source, f);
    }
}
