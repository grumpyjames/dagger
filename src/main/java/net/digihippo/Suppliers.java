package net.digihippo;

import java.util.function.Supplier;

import static net.digihippo.Result.failure;
import static net.digihippo.Result.success;

final class Suppliers {
    static <T> Supplier<Result<T>> wrapExceptions(Supplier<T> supplier) {
        return () -> {
            try {
                return success(supplier.get());
            } catch (Exception e) {
                return failure(e);
            }
        };
    }

    private Suppliers() {}
}
