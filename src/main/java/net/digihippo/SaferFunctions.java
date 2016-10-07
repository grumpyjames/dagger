package net.digihippo;

import java.util.function.BiFunction;
import java.util.function.Function;

import static net.digihippo.Suppliers.wrapExceptions;

final class SaferFunctions {
    static <S, T> Function<S, Result<T>> safer(final Function<S, T> f) {
        return s -> wrapExceptions(() -> f.apply(s)).get();
    }

    static <S1, S2, T> BiFunction<S1, S2, Result<T>> safer(final BiFunction<S1, S2, T> b)
    {
        return (s1, s2) -> wrapExceptions(() -> b.apply(s1, s2)).get();
    }

    private SaferFunctions() {}
}
