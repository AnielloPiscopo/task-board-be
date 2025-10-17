package com.example.task_board_be.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Richiesta per la creazione di una board")
public class CreateBoardRequest {
    @Schema(description = "Nome board" , example = "New board" )
    @Size(max = 200 , message = "Il nome non può superare 255 caratteri")
    private String name;

    @Schema(description = "Descrizione board" , example = "asdadasdasdadasdadadasdadadsadasdadasxsdasadasdadasdasd")
    @Size(max = 255 , message = "La descrizione non può superare 255 caratteri")
    private String description;

    public CreateBoardRequest() {
    }

    public CreateBoardRequest(String name, String description) {
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
        return "CreateBoardRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
