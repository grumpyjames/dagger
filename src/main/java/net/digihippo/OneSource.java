package net.digihippo;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class OneSource<T> {
    private final Supplier<T> supplier;

    OneSource(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    void consume(Consumer<T> consumer)
    {
        consumer.accept(supplier.get());
    }

    <U> OneSource<U> map(Function<T, U> f) {
        return new OneSource<>(() -> f.apply(supplier.get()));
    }

    <U1, U2> TwoSource<U1, U2> mapTwo(Function<T, U1> f1, Function<T, U2> f2) {
        return new TwoSource<>(
                new OneSource<>(() -> f1.apply(supplier.get())),
                new OneSource<>(() -> f2.apply(supplier.get())));
    }
}
