package net.digihippo;

import java.util.function.Consumer;
import java.util.function.Supplier;

class Source<T> {
    private final Supplier<T> supplier;

    Source(Supplier<T> supplier) {

        this.supplier = supplier;
    }

    void consume(Consumer<T> consumer)
    {
        consumer.accept(supplier.get());
    }
}
