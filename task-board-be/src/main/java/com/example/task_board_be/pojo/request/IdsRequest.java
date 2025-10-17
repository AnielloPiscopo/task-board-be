package com.example.task_board_be.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "Richiesta contenente lista di id")
public class IdsRequest {
    @Schema(description = "Lista di id" , example = "[1,2,3]" )
    @NotEmpty
    private List<@NotNull @Positive Long> idList;

    public IdsRequest() {
    }

    public IdsRequest(List<Long> idList) {
        this.idList = idList;
    }

    public List<Long> getIdList() {
        return idList;
    }

    public void setIdList(List<Long> idList) {
        this.idList = idList;
    }

    @Override
    public String toString() {
        return "IdsRequest{" +
                "idList=" + idList +
                '}';
    }
}
