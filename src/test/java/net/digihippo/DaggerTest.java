package net.digihippo;

import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;
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
        src.consume(assertSuccessAnd(output::add));

        assertEquals(singletonList("hello world"), output);
    }

    @Test
    public void can_map_to_a_different_type()
    {
        final OneSource<String> src = source(() -> "hello world");
        src.map(String::length).consume(assertSuccessAnd(l -> output.add(Long.toString(l))));

        assertEquals(singletonList("11"), output);
    }

    @Test
    public void can_map_to_two_different_types()
    {
        final OneSource<String> src = source(() -> "hello world");
        src.mapTwo(String::length, this::firstWord)
                .consume(
                        assertSuccessAnd(l -> output.add(Long.toString(l))),
                        assertSuccessAnd(output::add));

        assertEquals(asList("11", "hello"), output);
    }

    @Test
    public void map_only_half_of_a_two_source()
    {
        final OneSource<String> src = source(() -> "hello world");
        src.mapTwo(String::length, this::firstWord)
                .mapFirst(l -> l + 15)
                .consume(assertSuccessAnd(l -> output.add(Long.toString(l))),
                        assertSuccessAnd(output::add));

        assertEquals(asList("26", "hello"), output);
    }

    @Test
    public void detect_an_error_in_the_supply()
    {
        source(() -> "short string".charAt(101))
                .consume(assertErrorAnd(e -> assertEquals(StringIndexOutOfBoundsException.class, e.getClass())));
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
                .asyncConsume(
                        executor,
                        assertSuccessAnd(l -> asyncOutput.add(Long.toString(l))),
                        assertSuccessAnd(asyncOutput::add));

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
                        assertSuccessAnd(l -> asyncOutput.add(Long.toString(l))),
                        assertSuccessAnd(asyncOutput::add));

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

    private <T> Consumer<Result<T>> assertSuccessAnd(Consumer<T> consumer) {
        return (r -> {
            try {
                r.consumeOrThrow(consumer);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });
    }

    private <T, U> BlockingFunction<T, U> block(Function<T, U> f) {
        return new BlockingFunction<>(f);
    }

    private <T> OneSource<T> source(Supplier<T> stringSupplier) {
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

    private <T> Consumer<Result<T>> assertErrorAnd(final Consumer<Exception> c)
    {
        return (r) -> r.consume(c, fail());
    }

    private <T> Consumer<T> fail() {
        return c -> {
            throw new AssertionError("Should have thrown, but succeeded with " + c);
        };
    }
}
