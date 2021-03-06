package io.smallrye.reactive.operators;

import io.reactivex.Flowable;
import io.smallrye.reactive.Multi;

public abstract class MultiOperator<I, O> extends AbstractMulti<O> {

    private final Multi<I> upstream;

    public MultiOperator(Multi<I> upstream) {
        // NOTE: upstream can be null. It's null when creating a "source".
        this.upstream = upstream;
    }

    public Multi<I> upstream() {
        return upstream;
    }

    @SuppressWarnings("unchecked")
    protected Flowable<I> upstreamAsFlowable() {
        if (upstream instanceof AbstractMulti) {
            return ((AbstractMulti<I>) upstream).flowable();
        }
        return Flowable.fromPublisher(upstream);
    }

}
