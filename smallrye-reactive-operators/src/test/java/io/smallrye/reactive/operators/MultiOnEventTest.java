package io.smallrye.reactive.operators;

import io.smallrye.reactive.CompositeException;
import io.smallrye.reactive.Multi;
import io.smallrye.reactive.operators.MultiAssertSubscriber;
import org.junit.Test;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiOnEventTest {
    @Test
    public void testCallbacksWhenResultIsEmitted() {
        MultiAssertSubscriber<Integer> ts = MultiAssertSubscriber.create();

        AtomicReference<Subscription> subscription = new AtomicReference<>();
        AtomicReference<Integer> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean completion = new AtomicBoolean();
        AtomicLong requests = new AtomicLong();
        AtomicBoolean termination = new AtomicBoolean();
        AtomicBoolean cancellation = new AtomicBoolean();

        Multi.createFrom().result(1)
                .on().subscription(subscription::set)
                .on().result().peek(result::set)
                .on().failure().peek(failure::set)
                .on().completion(() -> completion.set(true))
                .on().termination((f, c) -> termination.set(f == null && !c))
                .on().request(requests::set)
                .on().cancellation(() -> cancellation.set(true))
                .subscribe(ts);

        ts
                .request(20)
                .assertCompletedSuccessfully()
                .assertReceived(1);

        assertThat(subscription.get()).isNotNull();
        assertThat(result.get()).isEqualTo(1);
        assertThat(failure.get()).isNull();
        assertThat(completion.get()).isTrue();
        assertThat(termination.get()).isTrue();
        assertThat(requests.get()).isEqualTo(20);
        assertThat(cancellation.get()).isFalse();
    }

    @Test
    public void testCallbacksOnFailure() {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        AtomicReference<Integer> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean completion = new AtomicBoolean();
        AtomicLong requests = new AtomicLong();
        AtomicBoolean termination = new AtomicBoolean();
        AtomicBoolean cancellation = new AtomicBoolean();

        Multi.createFrom().<Integer>failure(new IOException("boom"))
                .on().subscription(subscription::set)
                .on().result().peek(result::set)
                .on().failure().peek(failure::set)
                .on().completion(() -> completion.set(true))
                .on().termination((f, c) -> termination.set(f != null))
                .on().request(requests::set)
                .on().cancellation(() -> cancellation.set(true))
                .subscribe().withSubscriber(MultiAssertSubscriber.create())
                .assertHasFailedWith(IOException.class, "boom");

        assertThat(subscription.get()).isNotNull();
        assertThat(result.get()).isNull();
        assertThat(failure.get()).isInstanceOf(IOException.class).hasMessageContaining("boom");
        assertThat(completion.get()).isFalse();
        assertThat(termination.get()).isTrue();
        assertThat(requests.get()).isEqualTo(0);
        assertThat(cancellation.get()).isFalse();
    }

    @Test
    public void testCallbacksOnFailureWhenPredicateMatches() {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        AtomicReference<Integer> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean completion = new AtomicBoolean();
        AtomicLong requests = new AtomicLong();
        AtomicBoolean termination = new AtomicBoolean();
        AtomicBoolean cancellation = new AtomicBoolean();

        Multi.createFrom().<Integer>failure(new IOException("boom"))
                .on().subscription(subscription::set)
                .on().result().peek(result::set)
                .on().failure(IOException.class).peek(failure::set)
                .on().completion(() -> completion.set(true))
                .on().termination((f, c) -> termination.set(f != null))
                .on().request(requests::set)
                .on().cancellation(() -> cancellation.set(true))
                .subscribe().withSubscriber(MultiAssertSubscriber.create())
                .assertHasFailedWith(IOException.class, "boom");

        assertThat(subscription.get()).isNotNull();
        assertThat(result.get()).isNull();
        assertThat(failure.get()).isInstanceOf(IOException.class).hasMessageContaining("boom");
        assertThat(completion.get()).isFalse();
        assertThat(termination.get()).isTrue();
        assertThat(requests.get()).isEqualTo(0);
        assertThat(cancellation.get()).isFalse();
    }

    @Test
    public void testCallbacksOnFailureWhenPredicateDoesNotPass() {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        AtomicReference<Integer> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean completion = new AtomicBoolean();
        AtomicLong requests = new AtomicLong();
        AtomicBoolean termination = new AtomicBoolean();
        AtomicBoolean cancellation = new AtomicBoolean();

        Multi.createFrom().<Integer>failure(new IOException("boom"))
                .on().subscription(subscription::set)
                .on().result().peek(result::set)
                .on().failure(f -> f.getMessage().contains("missing")).peek(failure::set)
                .on().completion(() -> completion.set(true))
                .on().termination((f, c) -> termination.set(f != null))
                .on().request(requests::set)
                .on().cancellation(() -> cancellation.set(true))
                .subscribe().withSubscriber(MultiAssertSubscriber.create())
                .assertHasFailedWith(IOException.class, "boom");

        assertThat(subscription.get()).isNotNull();
        assertThat(result.get()).isNull();
        assertThat(failure.get()).isNull();
        assertThat(completion.get()).isFalse();
        assertThat(termination.get()).isTrue();
        assertThat(requests.get()).isEqualTo(0);
        assertThat(cancellation.get()).isFalse();
    }

    @Test
    public void testCallbacksOnFailureWhenPredicateThrowsAnException() {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        AtomicReference<Integer> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean completion = new AtomicBoolean();
        AtomicLong requests = new AtomicLong();
        AtomicBoolean termination = new AtomicBoolean();
        AtomicBoolean cancellation = new AtomicBoolean();

        Predicate<? super Throwable> boom = f -> {
            throw new IllegalStateException("bigboom");
        };

        Multi.createFrom().<Integer>failure(new IOException("smallboom"))
                .on().subscription(subscription::set)
                .on().result().peek(result::set)
                .on().failure(boom).peek(failure::set)
                .on().completion(() -> completion.set(true))
                .on().termination((f, c) -> termination.set(f != null))
                .on().request(requests::set)
                .on().cancellation(() -> cancellation.set(true))
                .subscribe().withSubscriber(MultiAssertSubscriber.create())
                .assertHasFailedWith(CompositeException.class, "bigboom")
                .assertHasFailedWith(CompositeException.class, "smallboom");

        assertThat(subscription.get()).isNotNull();
        assertThat(result.get()).isNull();
        assertThat(failure.get()).isNull();
        assertThat(completion.get()).isFalse();
        assertThat(termination.get()).isTrue();
        assertThat(requests.get()).isEqualTo(0);
        assertThat(cancellation.get()).isFalse();
    }

    @Test
    public void testCallbacksOnCompletion() {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        AtomicReference<Integer> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean completion = new AtomicBoolean();
        AtomicLong requests = new AtomicLong();
        AtomicBoolean termination = new AtomicBoolean();
        AtomicBoolean cancellation = new AtomicBoolean();

        Multi.createFrom().<Integer>empty()
                .on().subscription(subscription::set)
                .on().result().peek(result::set)
                .on().failure().peek(failure::set)
                .on().completion(() -> completion.set(true))
                .on().termination((f, c) -> termination.set(f == null && !c))
                .on().request(requests::set)
                .on().cancellation(() -> cancellation.set(true))
                .subscribe().withSubscriber(MultiAssertSubscriber.create())
                .assertCompletedSuccessfully()
                .assertHasNoResults();


        assertThat(subscription.get()).isNotNull();
        assertThat(result.get()).isNull();
        assertThat(failure.get()).isNull();
        assertThat(completion.get()).isTrue();
        assertThat(termination.get()).isTrue();
        assertThat(requests.get()).isEqualTo(0);
        assertThat(cancellation.get()).isFalse();
    }


    @Test
    public void testWithNoEvents() {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        AtomicReference<Integer> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean completion = new AtomicBoolean();
        AtomicLong requests = new AtomicLong();
        AtomicBoolean termination = new AtomicBoolean();
        AtomicBoolean cancellation = new AtomicBoolean();

        MultiAssertSubscriber<Integer> subscriber = Multi.createFrom().<Integer>nothing()
                .on().subscription(subscription::set)
                .on().result().peek(result::set)
                .on().failure().peek(failure::set)
                .on().completion(() -> completion.set(true))
                .on().termination((f, c) -> termination.set(f == null && c))
                .on().request(requests::set)
                .on().cancellation(() -> cancellation.set(true))
                .subscribe().withSubscriber(MultiAssertSubscriber.create(10))
                .assertNotTerminated()
                .assertHasNoResults();


        assertThat(subscription.get()).isNotNull();
        assertThat(result.get()).isNull();
        assertThat(failure.get()).isNull();
        assertThat(completion.get()).isFalse();
        assertThat(termination.get()).isFalse();
        assertThat(requests.get()).isEqualTo(10);
        assertThat(cancellation.get()).isFalse();

        subscriber.cancel();
        assertThat(termination.get()).isTrue();
        assertThat(cancellation.get()).isTrue();
    }


    @Test
    public void testWhenOnResultPeekThrowsExceptions() {
        MultiAssertSubscriber<Integer> ts = MultiAssertSubscriber.create(1);

        Multi.createFrom().result(1)
                .on().result().peek(i -> {
            throw new IllegalArgumentException("boom");
        })
                .subscribe().withSubscriber(ts)
                .assertTerminated()
                .assertHasFailedWith(IllegalArgumentException.class, "boom");
    }

    @Test
    public void testWhenOnFailurePeekThrowsExceptions() {
        MultiAssertSubscriber<Integer> ts = MultiAssertSubscriber.create(1);

        Multi.createFrom().<Integer>failure(new IOException("source"))
                .on().failure().peek(f -> {
            throw new IllegalArgumentException("boom");
        })
                .subscribe().withSubscriber(ts)
                .assertTerminated()
                .assertHasFailedWith(CompositeException.class, "boom")
                .assertHasFailedWith(CompositeException.class, "source");
    }

    @Test
    public void testWhenOnCompletionPeekThrowsExceptions() {
        MultiAssertSubscriber<Integer> ts = MultiAssertSubscriber.create(1);

        Multi.createFrom().results(1, 2)
                .on().completion(() -> {
            throw new IllegalArgumentException("boom");
        })
                .subscribe().withSubscriber(ts)
                .assertNotTerminated()
                .assertReceived(1)
                .request(1)
                .assertTerminated()
                .assertHasFailedWith(IllegalArgumentException.class, "boom")
                .assertReceived(1, 2);
    }


    @Test
    public void testWhenBothOnResultAndOnFailureThrowsException() {
        Multi.createFrom().result(1)
                .on().result().peek(i -> {
            throw new IllegalArgumentException("boom1");
        }).on().failure().peek(t -> {
            throw new IllegalArgumentException("boom2");
        }).subscribe().withSubscriber(MultiAssertSubscriber.create(1))
                .assertTerminated()
                .assertHasFailedWith(CompositeException.class, "boom1")
                .assertHasFailedWith(CompositeException.class, "boom2");
    }

    @Test
    public void testThatAFailureInTerminationDoesNotRunTerminationTwice() {
        AtomicInteger called = new AtomicInteger();
        Multi.createFrom().result(1)
                .on().termination((f, c) -> {
            called.incrementAndGet();
            throw new IllegalArgumentException("boom");
        }).subscribe().withSubscriber(MultiAssertSubscriber.create(1))
                .assertHasFailedWith(IllegalArgumentException.class, "boom");

        assertThat(called).hasValue(1);
    }


}