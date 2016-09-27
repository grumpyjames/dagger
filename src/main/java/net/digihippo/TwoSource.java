package net.digihippo;

import java.util.function.Consumer;
import java.util.function.Supplier;

class TwoSource<T1, T2> {
    private final Supplier<T1> supplierOne;
    private final Supplier<T2> supplierTwo;

    TwoSource(Supplier<T1> supplierOne, Supplier<T2> supplierTwo) {
        this.supplierOne = supplierOne;
        this.supplierTwo = supplierTwo;
    }

    void consume(final Consumer<T1> c1, final Consumer<T2> c2)
    {
        c1.accept(supplierOne.get());
        c2.accept(supplierTwo.get());
    }
}
