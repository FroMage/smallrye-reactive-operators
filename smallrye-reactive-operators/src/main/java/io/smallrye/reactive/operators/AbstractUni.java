package io.smallrye.reactive.operators;

import io.smallrye.reactive.Multi;
import io.smallrye.reactive.Uni;
import io.smallrye.reactive.groups.*;
import io.smallrye.reactive.tuples.Tuple2;

import java.util.concurrent.Executor;
import java.util.function.Predicate;

import static io.smallrye.reactive.helpers.ParameterValidation.nonNull;

public abstract class AbstractUni<T> implements Uni<T> {

    protected abstract void subscribing(UniSerializedSubscriber<? super T> subscriber);

    @Override
    public UniSubscribe<T> subscribe() {
        return new UniSubscribe<>(this);
    }

    @Override
    public UniOnItem<T> onItem() {
        return new UniOnItem<>(this);
    }

    @Override
    public UniOnTimeout<T> onNoItem() {
        return new UniOnTimeout<>(this, null, null);
    }

    @Override
    public UniOnFailure<T> onFailure() {
        return new UniOnFailure<>(this, null);
    }

    @Override
    public UniOnFailure<T> onFailure(Predicate<? super Throwable> predicate) {
        return new UniOnFailure<>(this, predicate);
    }

    @Override
    public UniOnFailure<T> onFailure(Class<? extends Throwable> typeOfFailure) {
        return new UniOnFailure<>(this, typeOfFailure::isInstance);
    }

    @Override
    public UniAndGroup<T> and() {
        return new UniAndGroup<>(this);
    }

    @Override
    public <T2> Uni<Tuple2<T, T2>> and(Uni<T2> other) {
        return new UniAndGroup<>(this).uni(nonNull(other, "other")).asTuple();
    }

    @Override
    public UniOr or() {
        return new UniOr<>(this);
    }

    @Override
    public UniAwait<T> await() {
        return new UniAwait<>(this);
    }

    @Override
    public Uni<T> emitOn(Executor executor) {
        return new UniEmitOn<>(this, nonNull(executor, "executor"));
    }

    @Override
    public Uni<T> subscribeOn(Executor executor) {
        return new UniCallSubscribeOn<>(this, nonNull(executor, "executor"));
    }

    @Override
    public Uni<T> cache() {
        return new UniCache<>(this);
    }

    @Override
    public UniAdapt<T> adapt() {
        return new UniAdapt<>(this);
    }

    @Override
    public Multi<T> toMulti() {
        return Multi.createFrom().publisher(adapt().toPublisher());
    }

    @Override
    public UniOnEvent<T> on() {
        return new UniOnEvent<>(this);
    }
}
