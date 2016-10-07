package net.digihippo;

import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DaggerTest {
    private final List<String> output = new ArrayList<>();
    private final BlockingQueue<String> asyncOutput = new LinkedBlockingQueue<>();

    @Test
    public void can_collect_from_a_single_source()
    {
        final OneSource<String> src = source(() -> "hello world");
        src.consume(output::add);

        assertEquals(singletonList("hello world"), output);
    }

    @Test
    public void can_map_to_a_different_type()
    {
        final OneSource<String> src = source(() -> "hello world");
        src.map(String::length)
           .consume(l -> output.add(Long.toString(l)));

        assertEquals(singletonList("11"), output);
    }

    @Test
    public void can_map_to_two_different_types()
    {
        final OneSource<String> src = source(() -> "hello world");
        src.mapTwo(String::length, this::firstWord)
                .consume(l -> output.add(Long.toString(l)), output::add);

        assertEquals(asList("11", "hello"), output);
    }

    @Test
    public void map_only_half_of_a_two_source()
    {
        final OneSource<String> src = source(() -> "hello world");
        src.mapTwo(String::length, this::firstWord)
            .mapFirst(l -> l + 15)
            .consume(l -> output.add(Long.toString(l)), output::add);

        assertEquals(asList("26", "hello"), output);
    }

    @Test
    public void defer_each_task_and_execute_appropriately() throws InterruptedException {
        final OneSource<String> src = source(() -> "hello world");
        final AsynchronousExecutor executor = new AsynchronousExecutor();
        BlockingFunction<String, Integer> blockOne = block(String::length);
        BlockingFunction<String, String> blockTwo = block(this::firstWord);
        BlockingFunction<Integer, Integer> blockThree = block(l -> l + 15);
        src.mapTwo(blockOne, blockTwo)
                .mapFirst(blockThree)
                .asyncConsume(executor, l -> asyncOutput.add(Long.toString(l)), asyncOutput::add);

        assertNull(asyncOutput.poll());

        blockTwo.unblock();

        final Set<String> results = new HashSet<>();
        results.add(asyncOutput.poll(1, TimeUnit.SECONDS));
        assertEquals(new HashSet<>(singletonList("hello")), results);

        blockOne.unblock();
        blockThree.unblock();

        results.add(asyncOutput.poll(1, TimeUnit.SECONDS));
        assertEquals(new HashSet<>(asList("26", "hello")), results);
    }

    @Test
    public void timeouts_can_happen() throws InterruptedException {
        final OneSource<String> src = source(() -> "hello world");
        final AsynchronousExecutor executor = new AsynchronousExecutor();
        BlockingFunction<String, Integer> blockOne = block(String::length);
        BlockingFunction<String, String> blockTwo = block(this::firstWord);
        BlockingFunction<Integer, Integer> blockThree = block(l -> l + 15);
        src.mapTwo(blockOne, blockTwo)
                .mapFirst(blockThree)
                .asyncConsume(
                        executor,
                        Duration.of(25, ChronoUnit.MILLIS),
                        l -> asyncOutput.add(Long.toString(l)),
                        asyncOutput::add);

        assertNull(asyncOutput.poll());

        blockTwo.unblock();

        final Set<String> results = new HashSet<>();
        results.add(asyncOutput.poll(1, TimeUnit.SECONDS));
        assertEquals(new HashSet<>(singletonList("hello")), results);

        blockOne.unblock();
        blockThree.unblock();

        results.add(asyncOutput.poll(1, TimeUnit.SECONDS));
        assertEquals(new HashSet<>(asList("26", "hello")), results);
    }


    private <T, U> BlockingFunction<T, U> block(Function<T, U> f) {
        return new BlockingFunction<>(f);
    }

    private OneSource<String> source(Supplier<String> stringSupplier) {
        return new Supply<>(stringSupplier);
    }

    private String firstWord(final String input)
    {
        return input.split(" ")[0];
    }


    private class BlockingFunction<T, U> implements Function<T, U> {
        private final Function<T, U> f;
        private final CountDownLatch l = new CountDownLatch(1);

        BlockingFunction(Function<T, U> f) {
            this.f = f;
        }

        @Override
        public U apply(T t) {
            try {
                l.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return f.apply(t);
        }

        void unblock() {
            l.countDown();
        }
    }

    private static final class AsynchronousExecutor implements Executor {
        private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        
        @Override
        public <S, T> CompletableFuture<T> map(CompletableFuture<S> futureS, Function<S, T> f) {
            return futureS.thenApplyAsync(f, executorService);
        }

        @Override
        public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
            return CompletableFuture.supplyAsync(supplier, executorService);
        }

        @Override
        public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, Duration timeout) {
            CompletableFuture<T> result = CompletableFuture.supplyAsync(supplier, executorService);
            executorService.schedule(new Timeout<>(result), timeout.get(ChronoUnit.NANOS), TimeUnit.NANOSECONDS);

            return result;
        }

        private final class Timeout<T> implements Runnable {
            private final CompletableFuture<T> result;

            Timeout(CompletableFuture<T> result) {
                this.result = result;
            }

            @Override
            public void run() {
                result.completeExceptionally(new TimeoutException("bad luck, timed out"));
            }
        }
    }
}
