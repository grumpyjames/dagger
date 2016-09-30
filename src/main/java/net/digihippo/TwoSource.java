package net.digihippo;

import java.util.function.Consumer;
import java.util.function.Function;

class TwoSource<T1, T2> {
    private final OneSource<T1> sourceOne;
    private final OneSource<T2> sourceTwo;

    TwoSource(OneSource<T1> sourceOne, OneSource<T2> sourceTwo) {
        this.sourceOne = sourceOne;
        this.sourceTwo = sourceTwo;
    }

    <U1> TwoSource<U1, T2> mapFirst(final Function<T1, U1> f)
    {
        return new TwoSource<>(sourceOne.map(f), sourceTwo);
    }

    void consume(final Consumer<T1> c1, final Consumer<T2> c2)
    {
        sourceOne.consume(c1);
        sourceTwo.consume(c2);
    }

    void asyncConsume(final Executor executor, final Consumer<T1> c1, final Consumer<T2> c2)
    {
        sourceOne.asyncExec(executor).thenAccept(c1);
        sourceTwo.asyncExec(executor).thenAccept(c2);
    }
}
