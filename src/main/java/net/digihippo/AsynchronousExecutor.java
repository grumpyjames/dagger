package net.digihippo;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.digihippo.Result.failure;
import static net.digihippo.Result.success;
import static net.digihippo.Suppliers.wrapExceptions;

final class AsynchronousExecutor implements Executor {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    @Override
    public <S, T> CompletableFuture<Result<T>> map(CompletableFuture<Result<S>> futureS, Function<S, T> f) {
        return futureS.thenApplyAsync(r -> r.map(f), executorService);
    }

    @Override
    public <T> CompletableFuture<Result<T>> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(wrapExceptions(supplier), executorService);
    }

    @Override
    public <T> CompletableFuture<Result<T>> supplyAsync(Supplier<T> supplier, Duration timeout) {
        CompletableFuture<Result<T>> result =
                CompletableFuture.supplyAsync(wrapExceptions(supplier), executorService);
        executorService.schedule(new Timeout<>(result), timeout.get(ChronoUnit.NANOS), TimeUnit.NANOSECONDS);

        return result;
    }

    private final class Timeout<T> implements Runnable {
        private final CompletableFuture<Result<T>> result;

        Timeout(CompletableFuture<Result<T>> result) {
            this.result = result;
        }

        @Override
        public void run() {
            result.complete(failure(new TimeoutException("bad luck, timed out")));
        }
    }
}
