package io.smallrye.reactive.operators;

import io.reactivex.Flowable;
import io.smallrye.reactive.Multi;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static io.smallrye.reactive.operators.MultiCollector.getFlowable;

public class MultiBroadcaster {

    public static <T> Multi<T> publish(Multi<T> upstream, int numberOfSubscribers, boolean cancelWhenNoOneIsListening,
            Duration delayAfterLastDeparture) {

        Flowable<T> flowable = getFlowable(upstream);

        if (numberOfSubscribers > 0) {
            if (cancelWhenNoOneIsListening) {
                if (delayAfterLastDeparture != null) {
                    return new DefaultMulti<>(flowable.publish().refCount(numberOfSubscribers, delayAfterLastDeparture.toMillis(), TimeUnit.MILLISECONDS));
                } else {
                    return new DefaultMulti<>(flowable.publish().refCount(numberOfSubscribers));
                }
            } else {
                return new DefaultMulti<>(flowable.publish().autoConnect(numberOfSubscribers));
            }
        } else {
            if (cancelWhenNoOneIsListening) {
                if (delayAfterLastDeparture != null) {
                    return new DefaultMulti<>(flowable.publish().refCount(delayAfterLastDeparture.toMillis(), TimeUnit.MILLISECONDS));
                } else {
                    return new DefaultMulti<>(flowable.publish().refCount());
                }
            } else {
                return new DefaultMulti<>(flowable.publish().autoConnect());
            }
        }
    }

    private MultiBroadcaster() {
        // Avoid direct instantiation.
    }
}
