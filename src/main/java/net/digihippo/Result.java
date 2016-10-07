package net.digihippo;

import java.util.function.Consumer;
import java.util.function.Function;

// right biased either
public abstract class Result<S> {
    static <S> Result<S> success(final S s) {
        return new Success<>(s);
    }

    static <S> Result<S> failure(final Exception e) {
        return new Failure<>(e);
    }

    public abstract <T> T fold(final Function<Exception, T> onError, final Function<S, T> onSuccess);

    public abstract void consume(final Consumer<Exception> onError, final Consumer<S> onSuccess);

    public abstract void consumeOrThrow(final Consumer<S> consumer) throws Exception;

    <T> Result<T> map(Function<S, T> f) {
        return fold(Result::failure, s -> success(f.apply(s)));
    }

    private static final class Success<S> extends Result<S> {
        private final S s;

        private Success(S s) {
            this.s = s;
        }

        @Override
        public <T> T fold(Function<Exception, T> onError, Function<S, T> onSuccess) {
            return onSuccess.apply(s);
        }

        @Override
        public void consume(Consumer<Exception> onError, Consumer<S> onSuccess) {
            onSuccess.accept(s);
        }

        @Override
        public void consumeOrThrow(Consumer<S> consumer) {
            consumer.accept(s);
        }
    }

    private static final class Failure<S> extends Result<S> {
        private final Exception e;

        private Failure(Exception e) {
            this.e = e;
        }

        @Override
        public <T> T fold(Function<Exception, T> onError, Function<S, T> onSuccess) {
            return onError.apply(e);
        }

        @Override
        public void consume(Consumer<Exception> onError, Consumer<S> onSuccess) {
            onError.accept(e);
        }

        @Override
        public void consumeOrThrow(Consumer<S> consumer) throws Exception {
            throw e;
        }
    }
}
