package io.smallrye.reactive.operators;

import io.smallrye.reactive.Multi;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class MultiCollectTest {

    @Test
    public void testCollectFirstAndLast() {
        Multi<Integer> results = Multi.createFrom().results(1, 2, 3);
        results
                .collect().first()
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .await()
                .assertResult(1);

        results
                .collect().last()
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .await()
                .assertResult(3);
    }

    @Test
    public void testCollectWithEmpty() {
        Multi<Integer> results = Multi.createFrom().empty();
        results
                .collect().first()
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .await()
                .assertResult(null);

        results
                .collect().last()
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .await()
                .assertResult(null);
    }

    @Test
    public void testCollectFirstAndLastOnFailure() {
        Multi<Integer> failing = Multi.createFrom().failure(new IOException("boom"));
        failing
                .collect().first()
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .await()
                .assertFailure(IOException.class, "boom");

        failing
                .collect().last()
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .await()
                .assertFailure(IOException.class, "boom");
    }

    @Test
    public void testAsList() {
        UniAssertSubscriber<List<Integer>> subscriber = Multi.createFrom().results(1, 2, 3)
                .collect().asList()
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .await();

        assertThat(subscriber.getResult()).containsExactly(1, 2, 3);
    }

    @Test
    public void testCollectIn() {
        UniAssertSubscriber<LinkedList<Integer>> subscriber = Multi.createFrom().range(1, 10)
                .collect().in(LinkedList<Integer>::new, LinkedList::add)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .await();

        assertThat(subscriber.getResult()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9).isInstanceOf(LinkedList.class);
    }

    @Test
    public void testCollectInWithSupplierThrowingException() {
        Multi.createFrom().range(1, 10)
                .collect().in(() -> {
            throw new IllegalArgumentException("boom");
        }, (x, y) -> {
        })
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailure(IllegalArgumentException.class, "boom");
    }

    @Test
    public void testCollectInWithAccumulatorThrowingException() {
        Multi.createFrom().range(1, 10)
                .collect().in(LinkedList<Integer>::new, (list, res) -> {
            list.add(res);
            if (res == 5) {
                throw new IllegalArgumentException("boom");
            }
        })
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailure(IllegalArgumentException.class, "boom");
    }

    @Test
    public void testCollectInWithSupplierReturningNull() {
        Multi.createFrom().range(1, 10)
                .collect().in(() -> null, (x, y) -> {
        })
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailure(NullPointerException.class, "supplier");
    }


    static class Person {

        private final String firstName;
        private final long id;

        Person(String firstName, long id) {
            this.firstName = firstName;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Person person = (Person) o;
            return id == person.id &&
                    firstName.equals(person.firstName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstName, id);
        }
    }

    private Multi<Person> persons = Multi.createFrom().results(
            new Person("bob", 1),
            new Person("alice", 2),
            new Person("rob", 3),
            new Person("matt", 4)
    );

    private Multi<Person> personsWithDuplicates = Multi.createFrom().results(
            new Person("bob", 1),
            new Person("alice", 2),
            new Person("rob", 3),
            new Person("matt", 4),
            new Person("bob", 5),
            new Person("rob", 6)
    );

    @Test
    public void testCollectIntoMap() {
        UniAssertSubscriber<Map<String, Person>> subscriber = persons
                .collect().asMap(p -> p.firstName)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompletedSuccessfully();

        assertThat(subscriber.getResult())
                .hasSize(4)
                .contains(
                        entry("bob", new Person("bob", 1)),
                        entry("alice", new Person("alice", 2)),
                        entry("rob", new Person("rob", 3)),
                        entry("matt", new Person("matt", 4))
                );
    }

    @Test
    public void testCollectAsMapWithEmpty() {
        UniAssertSubscriber<Map<String, Person>> subscriber = Multi.createFrom().<Person>empty()
                .collect().asMap(p -> p.firstName)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompletedSuccessfully();

        assertThat(subscriber.getResult()).isEmpty();
    }


    @Test
    public void testCollectAsMultiMap() {
        UniAssertSubscriber<Map<String, Collection<Person>>> subscriber = personsWithDuplicates
                .collect().asMultiMap(p -> p.firstName)
                .subscribe().withSubscriber(new UniAssertSubscriber<>())
                .assertCompletedSuccessfully();

        assertThat(subscriber.getResult()).hasSize(4);
        assertThat(subscriber.getResult().get("alice")).containsExactly(new Person("alice", 2));
        assertThat(subscriber.getResult().get("rob")).hasSize(2)
                .contains(new Person("rob", 3), new Person("rob", 6));

    }


    @Test
    public void testCollectAsMultiMapOnEmpty() {
        UniAssertSubscriber<Map<String, Collection<Person>>> subscriber = Multi.createFrom().<Person>empty()
                .collect().asMultiMap(p -> p.firstName)
                .subscribe().withSubscriber(new UniAssertSubscriber<>())
                .assertCompletedSuccessfully();
        assertThat(subscriber.getResult()).hasSize(0);

    }

    @Test
    public void testSumCollector() {
        Multi.createFrom().range(1, 5).collect().with(Collectors.summingInt(value -> value))
                .subscribe().withSubscriber(new UniAssertSubscriber<>()).assertCompletedSuccessfully().assertResult(10);
    }

}