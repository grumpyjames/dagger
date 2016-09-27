package net.digihippo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class DaggerTest {
    private final List<String> output = new ArrayList<>();

    @Test
    public void can_collect_from_a_single_source()
    {
        final Source<String> src = source(() -> "hello world");
        src.consume(output::add);

        assertEquals(singletonList("hello world"), output);
    }

    private Source<String> source(Supplier<String> stringSupplier) {
        return new Source<>(stringSupplier);
    }
}
