package com.example.task_board_be.pojo.resource;

import com.example.task_board_be.enums.BulkOperation;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Esito di un'operazione massiva su più board")
public class BulkResource {
    @Schema(description = "Tipo di operazione eseguita" , example = "ARCHIVE")
    private BulkOperation operation;

    @Schema(description = "Numero di record interessati" , example = "5")
    private int updatedRow;

    public BulkResource() {
    }

    public BulkResource(BulkOperation operation, int updatedRow) {
        this.operation = operation;
        this.updatedRow = updatedRow;
    }

    public BulkOperation getOperation() {
        return operation;
    }

    public void setOperation(BulkOperation operation) {
        this.operation = operation;
    }

    public int getUpdatedRow() {
        return updatedRow;
    }

    public void setUpdatedRow(int updatedRow) {
        this.updatedRow = updatedRow;
    }

    @Override
    public String toString() {
        return "BulkResource{" +
                "operation='" + operation + '\'' +
                ", updatedRow=" + updatedRow +
                '}';
    }
}
