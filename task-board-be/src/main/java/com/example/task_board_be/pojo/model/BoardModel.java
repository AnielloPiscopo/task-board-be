package com.example.task_board_be.pojo.model;

import java.util.ArrayList;
import java.util.List;

public class BoardModel extends BaseModel {
    private String name;
    private String description;
    private List<TaskModel> taskModelList = new ArrayList<>();

    public BoardModel() {
        super();
    }

    public BoardModel(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public BoardModel(Long id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
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

    public List<TaskModel> getTaskModelList() {
        return taskModelList;
    }

    public void setTaskModelList(List<TaskModel> taskModelList) {
        this.taskModelList = taskModelList;
    }

    @Override
    public String toString() {
        return "BoardModel{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                "} " + super.toString();
    }
}
