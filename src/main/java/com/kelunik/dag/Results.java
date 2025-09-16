package com.kelunik.dag;

import java.util.Map;

public final class Results {
    private final Map<Task<?>, Object> results;

    Results(Map<Task<?>, Object> results) {
        this.results = results;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Task<T> task) {
        return (T) results.get(task);
    }
}
