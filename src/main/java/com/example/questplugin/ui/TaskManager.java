package com.example.questplugin.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TaskManager {
    private static final Map<UUID, Integer> taskMap = new HashMap<>();

    public static void addTask(UUID uuid, int taskId) {
        taskMap.put(uuid, taskId);
    }

    public static int getTaskId(UUID uuid) {
        return taskMap.getOrDefault(uuid, -1);
    }

    public static void removeTask(UUID uuid) {
        taskMap.remove(uuid);
    }
}
