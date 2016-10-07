package net.digihippo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.digihippo.Result.success;

class Supply<T> implements OneSource<T> {
    private final Supplier<T> supplier;

    Supply(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void consume(Consumer<Result<T>> consumer)
    {
        consumer.accept(success(supplier.get()));
    }

    @Override
    public <U> OneSource<U> map(Function<T, U> f) {
        return new OneMapSource<>(this, f);
    }

    @Override
    public <U1, U2> TwoSource<U1, U2> mapTwo(Function<T, U1> f1, Function<T, U2> f2) {
        return new TwoSource<>(
                new Supply<>(() -> f1.apply(supplier.get())),
                new Supply<>(() -> f2.apply(supplier.get())));
    }

    @Override
    public CompletableFuture<Result<T>> asyncExec(Executor executor) {
        return executor.supplyAsync(supplier);
    }

    @Override
    public CompletableFuture<Result<T>> asyncExec(Executor executor, Duration timeout) {
        return executor.supplyAsync(supplier, timeout);
    }
}
