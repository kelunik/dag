package com.kelunik.dag;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

public final class Task<R> {
    private final String id;
    private final List<Task<?>> dependencies;
    private final Function<Results, R> runner;

    Task(String id, List<Task<?>> dependencies, Function<Results, R> runner) {
        this.id = id;
        this.dependencies = List.copyOf(dependencies);
        this.runner = runner;
    }

    public String getId() {
        return id;
    }

    public List<Task<?>> getDependencies() {
        return dependencies;
    }

    public R run(ExecutorService executor) {
        Map<Task<?>, CompletableFuture<?>> futures = new ConcurrentHashMap<>();

        try {
            return Runner.run(this, futures, executor).join();
        } catch (CompletionException completionException) {
            throw Runner.unwrapCompletionException(completionException);
        }
    }

    R run(Results results) {
        return runner.apply(results);
    }
}