package com.kelunik.dag;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public final class TaskRunTiming {
    private final Instant queued;
    private final AtomicReference<Instant> started;
    private final AtomicReference<Instant> finished;

    TaskRunTiming(Instant queued, AtomicReference<Instant> started, AtomicReference<Instant> finished) {
        this.queued = queued;
        this.started = started;
        this.finished = finished;
    }

    public Instant getQueued() {
        return queued;
    }

    public Instant getStarted() {
        return started.get();
    }

    public Instant getFinished() {
        return finished.get();
    }
}
