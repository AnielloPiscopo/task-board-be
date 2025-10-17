package com.example.task_board_be.pojo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Board extends BaseEntity{
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true , fetch = FetchType.LAZY)
    private List<Task> taskList = new ArrayList<>();

    public Board() {
        super();
    }

    public Board(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Board(Long id, String name, String description) {
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

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public void addTask(Task task){
        if (taskList == null) taskList = new ArrayList<>();
        taskList.add(task);
        task.setBoard(this);
    }

    public void removeTask(Task task){
        if (taskList == null) return;
        taskList.remove(task);
        task.setBoard(null);
    }

    public void addTaskList(List<Task> taskList){
        if (this.taskList == null) this.taskList = new ArrayList<>();
        if (taskList == null || taskList.isEmpty()) return;
        this.taskList.addAll(taskList);
        taskList.forEach(t -> t.setBoard(this));
    }

    public void removeTaskList(List<Task> taskList){
        if (this.taskList == null || taskList == null || taskList.isEmpty()) return;
        this.taskList.removeAll(taskList);
    }

    public void clearTaskList(){
        if (taskList == null) { taskList = new ArrayList<>(); return; }
        taskList.clear();
    }

    @Override
    public String toString() {
        return "Board{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                "} " + super.toString();
    }
}
