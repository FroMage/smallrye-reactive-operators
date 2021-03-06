package io.smallrye.reactive.operators;

import io.reactivex.Flowable;
import io.smallrye.reactive.Multi;

import java.util.function.BiFunction;

public class MultiScan<T> extends MultiOperator<T, T> {

    private final BiFunction<T, T, T> scanner;

    public MultiScan(Multi<T> upstream, BiFunction<T, T, T> scanner) {
        super(upstream);
        this.scanner = scanner;
    }

    @Override
    protected Flowable<T> flowable() {
        return upstreamAsFlowable().scan(scanner::apply);
    }

}
