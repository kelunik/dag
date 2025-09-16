package com.kelunik.dag;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskListRun {
    final Map<Task<?>, TaskRun<?>> taskList = new ConcurrentHashMap<>();
}
