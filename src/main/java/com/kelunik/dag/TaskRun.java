package com.kelunik.dag;

import java.util.concurrent.CompletableFuture;

public final class TaskRun<R> {
    private final Task<R> task;
    private final CompletableFuture<R> result;
    private final TaskRunTiming timing;

    TaskRun(Task<R> task, CompletableFuture<R> result, TaskRunTiming timing) {
        this.task = task;
        this.result = result;
        this.timing = timing;
    }

    public Task<R> getTask() {
        return task;
    }

    public CompletableFuture<R> getResult() {
        return result;
    }

    public TaskRunTiming getTiming() {
        return timing;
    }
}
