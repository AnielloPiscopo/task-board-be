package com.example.task_board_be.exception.custom;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String resource, Object id) {
        super(resource + " not found: id=" + id);
    }
}
