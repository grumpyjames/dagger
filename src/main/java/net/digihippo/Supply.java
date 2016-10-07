package net.digihippo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.digihippo.Suppliers.wrapExceptions;

class Supply<T> implements OneSource<T> {
    private final Supplier<T> supplier;

    Supply(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void consume(Consumer<Result<T>> consumer)
    {
        consumer.accept(wrapExceptions(supplier).get());
    }

    @Override
    public <U1, U2> TwoSource<U1, U2> mapTwo(Function<T, U1> f1, Function<T, U2> f2) {
        final OneSource<T> shared = new SharedSupply<>(this);
        return new TwoSource<>(shared.map(f1), shared.map(f2));
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
