package net.digihippo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

interface OneSource<T> {
    default void consume(Consumer<T> consumer) {
        asyncConsume(new ImmediateExecutor(), consumer);
    }

    <U> OneSource<U> map(Function<T, U> f);

    <U1, U2> TwoSource<U1, U2> mapTwo(Function<T, U1> f1, Function<T, U2> f2);

    default void asyncConsume(Executor executor, Consumer<T> c) {
        asyncExec(executor).thenAccept(c);
    }

    CompletableFuture<T> asyncExec(Executor executor);

    CompletableFuture<T> asyncExec(Executor executor, Duration timeout);

    class ImmediateExecutor implements Executor {
        @Override
        public <S, T> CompletableFuture<T> map(CompletableFuture<S> futureS, Function<S, T> f) {
            return futureS.thenApply(f);
        }

        @Override
        public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
            return CompletableFuture.completedFuture(supplier.get());
        }

        @Override
        public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, Duration timeout) {
            return CompletableFuture.completedFuture(supplier.get());
        }
    }
}
