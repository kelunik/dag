package com.kelunik.dag;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

final class Runner {
    @SuppressWarnings("unchecked")
    static <T> CompletableFuture<T> run(Task<T> task, Map<Task<?>, CompletableFuture<?>> futures, ExecutorService executor) {
        return (CompletableFuture<T>) futures.computeIfAbsent(task, key -> {
            // Run dependencies first
            List<Task<?>> dependencies = task.getDependencies();
            CompletableFuture<?>[] dependencyResults = dependencies.stream() //
                    .map(dep -> run(dep, futures, executor)) //
                    .toArray(CompletableFuture[]::new);

            // Build dependency results map and run task
            return CompletableFuture.allOf(dependencyResults).thenApplyAsync(v -> {
                Map<Task<?>, Object> results = dependencies.stream().collect(Collectors.toMap(d -> d, d -> futures.get(d).join()));
                return task.run(new Results(results));
            }, executor);
        });
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