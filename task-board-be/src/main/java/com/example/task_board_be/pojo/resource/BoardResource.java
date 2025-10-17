package com.example.task_board_be.pojo.resource;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Risorsa di risposta per i metodi di archive/restore/delete")
public class BoardResource {
    @Schema(description = "ID univoco della board" , example = "1")
    private Long id;

    @Schema(description = "Nome della board" , example = "New board")
    private String name;

    @Schema(description = "Descrizione della board" , example = "asdddddddddddddddcasssssssssssssssssssssssssscafsdfasasf")
    private String description;

    @Schema(description = "Lista delle task")
    private List<TaskResource> taskResourceList;

    public BoardResource() {
    }

    public BoardResource(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public BoardResource(Long id, String name, String description) {
        this(name , description);
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

    public List<TaskResource> getTaskResourceList() {
        return taskResourceList;
    }

    public void setTaskResourceList(List<TaskResource> taskResourceList) {
        this.taskResourceList = taskResourceList;
    }

    @Override
    public String toString() {
        return "BoardResource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
