package io.smallrye.reactive.groups;

import io.smallrye.reactive.Uni;
import io.smallrye.reactive.infrastructure.Infrastructure;
import io.smallrye.reactive.operators.UniDelayOnItem;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import static io.smallrye.reactive.helpers.ParameterValidation.nonNull;

public class UniOnResultDelay<T> {

    private final Uni<T> upstream;
    private ScheduledExecutorService executor;

    public UniOnResultDelay(Uni<T> upstream, ScheduledExecutorService executor) {
        this.upstream = upstream;
        this.executor = executor == null ? Infrastructure.getDefaultWorkerPool() : executor;
    }

    public UniOnResultDelay<T> onExecutor(ScheduledExecutorService executor) {
        this.executor = nonNull(executor, "executor");
        return this;
    }

    public Uni<T> by(Duration duration) {
        return Infrastructure.onUniCreation(new UniDelayOnItem<>(upstream, duration, executor));
    }

    public Uni<T> until(Function<? super T, ? extends Uni<?>> function) {
        throw new UnsupportedOperationException("to be implemented");
    }

}
