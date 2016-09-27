package net.digihippo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class DaggerTest {
    private final List<String> output = new ArrayList<>();

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

    private OneSource<String> source(Supplier<String> stringSupplier) {
        return new OneSource<>(stringSupplier);
    }

    private String firstWord(final String input)
    {
        return input.split(" ")[0];
    }
}
