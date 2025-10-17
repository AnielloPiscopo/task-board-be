package com.example.task_board_be.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Richiesta per l'aggiornamento di una task")
public class UpdateTaskRequest {
    @Schema(description = "Nome task" , example = "New task" )
    private String name;

    @Schema(description = "Descrizione task" , example = "asdfsdfsdfsdfsdfsdfdsfdsf" )
    @Size(max = 255 , message = "La descrizione non può superare 255 caratteri")
    private String description;

    @Schema(description = "Stato della task" , example = "TODO")
    private String status;

    @Schema(description = "Icona della task" , example = "FEATURE" )
    private String icon;

    public UpdateTaskRequest() {
    }

    public UpdateTaskRequest(String name, String description, String status, String icon) {
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

    @Override
    public String toString() {
        return "UpdateTaskRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
