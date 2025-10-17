package com.example.task_board_be.enums.task;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TaskIcon {
    NONE,
    BUG,
    FEATURE,
    DOCUMENTATION,
    REFACTOR,
    TEST,
    MAINTENANCE;

    @JsonCreator
    public static TaskIcon fromValue(String value) {
        return TaskIcon.valueOf(value.toUpperCase());
    }
}
