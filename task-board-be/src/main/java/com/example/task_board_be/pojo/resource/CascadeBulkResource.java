package com.example.task_board_be.pojo.resource;

import com.example.task_board_be.enums.BulkOperation;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Risorsa di risposta per i metodi di archive/restore/delete")
public class CascadeBulkResource extends BulkResource{
    @Schema(description = "Flag usata per vedere se l'operazione ha influito su altre entita")
    private Boolean cascade;

    public CascadeBulkResource() {
    }

    public CascadeBulkResource(BulkOperation operation, int updatedRow, Boolean cascade) {
        super(operation, updatedRow);
        this.cascade = cascade;
    }

    public Boolean getCascade() {
        return cascade;
    }

    public void setCascade(Boolean cascade) {
        this.cascade = cascade;
    }

    @Override
    public String toString() {
        return "CascadeBulkResource{" +
                "cascade=" + cascade +
                "} " + super.toString();
    }
}
