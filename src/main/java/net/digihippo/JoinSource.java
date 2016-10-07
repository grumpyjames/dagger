package net.digihippo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

class JoinSource<S1, S2, T> implements OneSource<T> {

    private final OneSource<S1> sourceOne;
    private final OneSource<S2> sourceTwo;
    private final BiFunction<S1, S2, T> bif;

    JoinSource(OneSource<S1> sourceOne, OneSource<S2> sourceTwo, BiFunction<S1, S2, T> bif) {
        this.sourceOne = sourceOne;
        this.sourceTwo = sourceTwo;
        this.bif = bif;
    }

    @Override
    public CompletableFuture<Result<T>> asyncExec(Executor executor) {
        final CompletableFuture<Result<S1>> resultOne = sourceOne.asyncExec(executor);
        final CompletableFuture<Result<S2>> resultTwo = sourceTwo.asyncExec(executor);

        return executor.mapTwo(resultOne, resultTwo, bif);
    }

    @Override
    public CompletableFuture<Result<T>> asyncExec(Executor executor, Duration timeout) {
        return null;
    }
}
