package com.example.task_board_be.enums.task;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TaskStatus {
    NONE,
    TODO,
    IN_PROGRESS,
    DONE,
    BLOCKED,
    CANCELED;

    @JsonCreator
    public static TaskStatus fromValue(String value) {
        return TaskStatus.valueOf(value.toUpperCase());
    }
}
