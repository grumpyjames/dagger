package net.digihippo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

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

}
