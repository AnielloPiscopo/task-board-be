package com.example.task_board_be.pojo.model;

import com.example.task_board_be.enums.task.TaskIcon;
import com.example.task_board_be.enums.task.TaskStatus;

public class TaskModel extends BaseModel {
    private String name;
    private String description;
    private TaskStatus status;
    private TaskIcon icon;

    private BoardModel boardModel;

    public TaskModel() {
        super();
    }

    public TaskModel(String name, String description, TaskStatus status, TaskIcon icon) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.icon = icon;
    }

    public TaskModel(Long id, String name, String description, TaskStatus status, TaskIcon icon) {
        super(id);
        this.name = name;
        this.description = description;
        this.status = status;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskIcon getIcon() {
        return icon;
    }

    public void setIcon(TaskIcon icon) {
        this.icon = icon;
    }

    public BoardModel getBoardModel() {
        return boardModel;
    }

    public void setBoardModel(BoardModel boardModel) {
        this.boardModel = boardModel;
    }

    @Override
    public String toString() {
        return "TaskModel{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", icon=" + icon +
                "} " + super.toString();
    }
}
