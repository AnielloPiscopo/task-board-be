package com.example.task_board_be.exception.custom;

public class StateMismatchException extends RuntimeException{
    public StateMismatchException(String resource , String correctState , Long id) {
        super("The '"+resource+"' with id " + id + " is not " + correctState);
    }

    public StateMismatchException(String resource , Long id , boolean shouldBeArchived){
        this(resource , (shouldBeArchived) ? "archived" : "active" , id);
    }
}
