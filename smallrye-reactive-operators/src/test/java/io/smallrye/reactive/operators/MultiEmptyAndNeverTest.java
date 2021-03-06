package io.smallrye.reactive.operators;

import io.smallrye.reactive.Multi;
import org.junit.Test;

public class MultiEmptyAndNeverTest {

    @Test
    public void testEmpty() {
        Multi<String> nothing = Multi.createFrom().empty();
        nothing.subscribe().withSubscriber(MultiAssertSubscriber.create())
                .assertCompletedSuccessfully()
                .assertHasNotReceivedAnyItem();
    }

    @Test
    public void testNever() {
        Multi<String> nothing = Multi.createFrom().nothing();
        nothing.subscribe().withSubscriber(MultiAssertSubscriber.create())
                .assertNotTerminated()
                .request(2)
                .assertNotTerminated();
    }

}
