package net.digihippo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

class SharedSupply<T> implements OneSource<T> {
    private final OneSource<T> oneSource;

    private CompletableFuture<Result<T>> started = null;

    SharedSupply(OneSource<T> oneSource) {
        this.oneSource = oneSource;
    }

    @Override
    public CompletableFuture<Result<T>> asyncExec(Executor executor) {
        synchronized (this)
        {
            if (started == null)
            {
                started = oneSource.asyncExec(executor);
            }
        }
        return started;
    }

    @Override
    public CompletableFuture<Result<T>> asyncExec(Executor executor, Duration timeout) {
        synchronized (this)
        {
            if (started == null)
            {
                started = oneSource.asyncExec(executor, timeout);
            }
        }
        return started;
    }
}
