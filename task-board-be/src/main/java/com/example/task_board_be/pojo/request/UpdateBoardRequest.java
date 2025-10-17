package com.example.task_board_be.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Richiesta per l'aggiornamento di una board")
public class UpdateBoardRequest {
    @Schema(description = "Nome board" , example = "New board" )
    private String name;

    @Schema(description = "Descrizione board" , example = "asdfsdfsdfsdfsdfsdfdsfdsf" )
    @Size(max = 255 , message = "La descrizione non può superare 255 caratteri")
    private String description;

    public UpdateBoardRequest() {
    }

    public UpdateBoardRequest(String name, String description) {
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

    @Override
    public String toString() {
        return "UpdateBoardRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
