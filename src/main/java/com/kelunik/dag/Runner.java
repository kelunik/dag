package com.kelunik.dag;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

final class Runner {
    @SuppressWarnings("unchecked")
    static <T> CompletableFuture<T> run(Task<T> task, TaskListRun taskListRun, ExecutorService executor) {
        return (CompletableFuture<T>) taskListRun.taskList.computeIfAbsent(task, key -> {
            // Run dependencies first
            List<Task<?>> dependencies = task.getDependencies();
            CompletableFuture<?>[] dependencyResults = dependencies.stream() //
                    .map(dep -> run(dep, taskListRun, executor)) //
                    .toArray(CompletableFuture[]::new);

            // Determine after dependencies have been triggered, so we can use it for sorting
            Instant queued = Instant.now();
            AtomicReference<Instant> started = new AtomicReference<>();
            AtomicReference<Instant> finished = new AtomicReference<>();

            // Build dependency results map and run task
            CompletableFuture<T> result = CompletableFuture.allOf(dependencyResults).thenApplyAsync(v -> {
                Map<Task<?>, Object> results = dependencies.stream().collect(Collectors.toMap(d -> d, d -> taskListRun.taskList.get(d).getResult().join()));

                started.set(Instant.now());

                try {
                    return task.run(new Results(results));
                } finally {
                    finished.set(Instant.now());
                }
            }, executor);

            return new TaskRun<T>(task, result, new TaskRunTiming(queued, started, finished));
        }).getResult();
    }

    static RuntimeException unwrapCompletionException(CompletionException completionException) {
        Throwable cause = completionException.getCause();

        if (cause instanceof RuntimeException runtimeException) {
            return runtimeException;
        }

        if (cause instanceof Error error) {
            throw error;
        }

        return new RuntimeException(cause);
    }
}