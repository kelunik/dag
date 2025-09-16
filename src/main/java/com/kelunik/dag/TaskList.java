package com.kelunik.dag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.function.*;

public final class TaskList {

    private final Collection<Task<?>> tasks = new ArrayList<>();

    public TaskListRun run(ExecutorService executor) {
        TaskListRun taskListRun = new TaskListRun();

        try {
            CompletableFuture.allOf(tasks.stream().map(task -> Runner.run(task, taskListRun, executor)).toArray(CompletableFuture[]::new)).join();

            return taskListRun;
        } catch (CompletionException completionException) {
            throw Runner.unwrapCompletionException(completionException);
        }
    }

    public <T> Task<T> add(String id, Runnable task) {
        return addInternal(new Task<>(id, List.of(), deps -> {
            task.run();

            return null;
        }));
    }

    public <R> Task<R> add(String id, List<Task<?>> dependencies, Function<Results, R> task) {
        return addInternal(new Task<>(id, dependencies, task));
    }

    public <R> Task<R> add(String id, Supplier<R> task) {
        return addInternal(new Task<>(id, List.of(), results -> task.get()));
    }

    public <R> Task<R> add(String id, List<Task<?>> dependencies, Consumer<Results> task) {
        return addInternal(new Task<>(id, dependencies, results -> {
            task.accept(results);

            return null;
        }));
    }

    public <T1, R> Task<R> add(String id, Task<T1> dependency, Function<T1, R> task) {
        return addInternal(new Task<>(id, List.of(dependency), results -> task.apply(results.get(dependency))));
    }

    public <T1, T2> Task<Void> add(String id, Task<T1> dependency1, Task<T2> dependency2, BiConsumer<T1, T2> task) {
        return addInternal(new Task<>(id, List.of(dependency1, dependency2), results -> {
            task.accept(results.get(dependency1), results.get(dependency2));

            return null;
        }));
    }

    public <T1, T2, R> Task<R> add(String id, Task<T1> dependency1, Task<T2> dependency2, BiFunction<T1, T2, R> task) {
        return addInternal(new Task<>(id, List.of(dependency1, dependency2), results -> task.apply(results.get(dependency1), results.get(dependency2))));
    }

    private <T> Task<T> addInternal(Task<T> task) {
        tasks.add(task);

        return task;
    }
}
