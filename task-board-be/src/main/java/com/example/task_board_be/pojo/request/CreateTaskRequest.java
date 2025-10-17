package com.example.task_board_be.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Richiesta per la creazione di una task")
public class CreateTaskRequest {
    @Schema(description = "Nome task" , example = "New task" )
    @Size(max = 200 , message = "Il nome non può superare 200 caratteri")
    private String name;

    @Schema(description = "Descrizione task" , example = "asdadasdasdadasdadadasdadadsadasdadasxsdasadasdadasdasd")
    @Size(max = 255 , message = "La descrizione non può superare 255 caratteri")
    private String description;

    @Schema(description = "Stato della task" , example = "TODO")
    private String status;

    @Schema(description = "Icona della task" , example = "BUG")
    private String icon;

    @Schema(description = "Id della board in cui è presente la task" , example = "3")
    @Column(nullable = false)
    @NotNull(message="L'id della board associata deve essere obbligatorio")
    private Long boardId;

    public CreateTaskRequest() {
    }

    public CreateTaskRequest(String name, String description, String status, String icon, Long boardId) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.icon = icon;
        this.boardId = boardId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
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
        return "CreateTaskRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", icon='" + icon + '\'' +
                ", boardId=" + boardId +
                '}';
    }
}
