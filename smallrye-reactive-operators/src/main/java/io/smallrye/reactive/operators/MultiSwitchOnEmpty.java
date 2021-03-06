package io.smallrye.reactive.operators;

import io.reactivex.Flowable;
import io.smallrye.reactive.Multi;
import org.reactivestreams.Publisher;

import java.util.function.Supplier;

import static io.smallrye.reactive.helpers.ParameterValidation.SUPPLIER_PRODUCED_NULL;

public class MultiSwitchOnEmpty<T> extends MultiOperator<T, T> {
    private final Supplier<Publisher<? extends T>> supplier;

    public MultiSwitchOnEmpty(Multi<T> upstream, Supplier<Publisher<? extends T>> supplier) {
        super(upstream);
        this.supplier = supplier;
    }

    @Override
    protected Flowable<T> flowable() {
        return upstreamAsFlowable().switchIfEmpty(Flowable.defer(() -> {
            Publisher<? extends T> publisher;
            try {
                publisher = supplier.get();
            } catch (Exception e) {
                return Flowable.error(e);
            }
            if (publisher == null) {
                return Flowable.error(new NullPointerException(SUPPLIER_PRODUCED_NULL));
            }
            return publisher;
        }));
    }
}
