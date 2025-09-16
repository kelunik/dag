package com.kelunik.dag;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskListTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    void singleSupplier() {
        var tasks = new TaskList();
        Task<String> hello = tasks.add("a", () -> "hello");

        assertThat(hello.run(executor)).isEqualTo("hello");
    }

    @Test
    void dependency() {
        var tasks = new TaskList();
        Task<Integer> t1 = tasks.add("a", () -> 1);
        Task<Integer> t2 = tasks.add("b", () -> 2);
        Task<Integer> t3 = tasks.add("c", t1, t2, Integer::sum);

        assertThat(t3.run(executor)).isEqualTo(3);
    }

    @Test
    void dependencyException() {
        var tasks = new TaskList();
        Task<Integer> t1 = tasks.add("a", () -> 1);
        Task<Integer> t2 = tasks.add("b", () -> {
            throw new IllegalArgumentException("invalid args");
        });

        AtomicBoolean t3Executed = new AtomicBoolean(false);
        Task<Void> t3 = tasks.add("c", t1, t2, (BiConsumer<Integer, Integer>) (r1, r2) -> t3Executed.set(true));

        assertThrows(IllegalArgumentException.class, () -> t3.run(executor));
        assertThat(t3Executed.get()).isFalse();
    }

    @Test
    void runAll() {
        Set<String> tasksRun = ConcurrentHashMap.newKeySet();

        var tasks = new TaskList();
        Task<Integer> t1 = tasks.add("a", () -> {
            tasksRun.add("a");

            return 1;
        });

        Task<Integer> t2 = tasks.add("b", () -> {
            tasksRun.add("b");

            return 2;
        });

        tasks.add("c", t1, t2, (a, b) -> {
            tasksRun.add("c");

            return Integer.sum(a, b);
        });

        Task<Void> t4 = tasks.add("d", () -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            tasksRun.add("d");
        });

        TaskListRun run = tasks.run(executor);

        assertThat(tasksRun).containsExactlyInAnyOrder("a", "b", "c", "d");
        assertThat(run.taskList.values().stream().sorted(Comparator.comparing(taskRun -> taskRun.getTiming().getQueued())).map(taskRun -> taskRun.getTask().getId()).collect(Collectors.joining(" > "))).isEqualTo("a > b > c > d");
        assertThat(Duration.between(run.taskList.get(t4).getTiming().getStarted(), run.taskList.get(t4).getTiming().getFinished())).isGreaterThanOrEqualTo(Duration.ofSeconds(1));
        assertThat(Duration.between(run.taskList.get(t4).getTiming().getStarted(), run.taskList.get(t4).getTiming().getFinished())).isLessThan(Duration.ofSeconds(2));
    }
}