package io.smallrye.reactive.operators;

import io.smallrye.reactive.Multi;
import io.smallrye.reactive.Uni;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiFilterTest {

    @Test(expected = IllegalArgumentException.class)
    public void testThatPredicateCannotBeNull() {
        Multi.createFrom().range(1, 4)
                .onItem().filterWith(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThatFunctionCannotBeNull() {
        Multi.createFrom().range(1, 4)
                .onItem().testWith(null);
    }

    @Test
    public void testFilteringWithPredicate() {
        Predicate<Integer> test = x -> x % 2 != 0;
        assertThat(Multi.createFrom().range(1, 4)
                .onItem().filterWith(test)
                .collect().asList()
                .await().indefinitely()).containsExactly(1, 3);
    }

    @Test
    public void testFilteringWithUni() {
        assertThat(Multi.createFrom().range(1, 4)
                .onItem()
                .testWith(x -> Uni.createFrom()
                        .deferredCompletionStage(() -> CompletableFuture.supplyAsync(() -> x % 2 != 0))
                )
                .collect().asList()
                .await().indefinitely()).containsExactly(1, 3);
    }

}
