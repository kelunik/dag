package com.kelunik.dag;

import java.util.List;
import java.util.concurrent.CompletionException;
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
        TaskListRun taskListRun = new TaskListRun();

        try {
            return Runner.run(this, taskListRun, executor).join();
        } catch (CompletionException completionException) {
            throw Runner.unwrapCompletionException(completionException);
        }
    }

    R run(Results results) {
        return runner.apply(results);
    }
}