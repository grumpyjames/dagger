package net.digihippo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

interface OneSource<T> {
    default void consume(Consumer<Result<T>> consumer) {
        asyncConsume(new ImmediateExecutor(), consumer);
    }

    <U> OneSource<U> map(Function<T, U> f);

    <U1, U2> TwoSource<U1, U2> mapTwo(Function<T, U1> f1, Function<T, U2> f2);

    default void asyncConsume(Executor executor, Consumer<Result<T>> c) {
        asyncExec(executor).thenAccept(c);
    }

    CompletableFuture<Result<T>> asyncExec(Executor executor);

    CompletableFuture<Result<T>> asyncExec(Executor executor, Duration timeout);

    class ImmediateExecutor implements Executor {
        @Override
        public <S, T> CompletableFuture<Result<T>> map(CompletableFuture<Result<S>> futureS, Function<S, T> f) {
            return futureS.thenApply(r -> r.map(f));
        }

        @Override
        public <T> CompletableFuture<Result<T>> supplyAsync(Supplier<T> supplier) {
            return CompletableFuture.completedFuture(Result.success(supplier.get()));
        }

        @Override
        public <T> CompletableFuture<Result<T>> supplyAsync(Supplier<T> supplier, Duration timeout) {
            return CompletableFuture.completedFuture(Result.success(supplier.get()));
        }
    }
}
