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
    public <U> OneSource<U> map(Function<T, U> f) {
        return new OneMapSource<>(this, f);
    }

    @Override
    public <U1, U2> TwoSource<U1, U2> mapTwo(Function<T, U1> f1, Function<T, U2> f2) {
        return null;
    }

    @Override
    public CompletableFuture<T> asyncExec(Executor executor) {
        final CompletableFuture<S> futureS = oneSource.asyncExec(executor);
        return executor.map(futureS, f);
    }

    @Override
    public CompletableFuture<T> asyncExec(Executor executor, Duration timeout) {
        final CompletableFuture<S> source = oneSource.asyncExec(executor, timeout);
        return executor.map(source, f);
    }
}
