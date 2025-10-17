package com.example.task_board_be.pojo.resource;

import com.example.task_board_be.enums.task.TaskIcon;
import com.example.task_board_be.enums.task.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Risorsa di risposta di una task")
public class TaskResource {
    @Schema(description = "ID univoco della task" , example = "1")
    private Long id;

    @Schema(description = "Nome della task" , example = "New task")
    private String name;

    @Schema(description = "Descrizione della task" , example = "afdafasfafafasfasfasfasfasfafsafsasfasfasfasfasfasxcdss")
    private String description;

    @Schema(description = "Status della task" , example = "TODO")
    private TaskStatus status;

    @Schema(description = "Icon della task" , example = "TODO")
    private TaskIcon icon;

    @Schema(description = "ID della board associata alla task" , example = "1")
    private Long boardId;

    public TaskResource() {
    }

    public TaskResource(String name, String description, TaskStatus status, TaskIcon icon) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.icon = icon;
    }

    public TaskResource(Long id, String name, String description, TaskStatus status, TaskIcon icon) {
        this(name , description , status , icon);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    @Override
    public String toString() {
        return "TaskResource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", icon=" + icon +
                '}';
    }
}
