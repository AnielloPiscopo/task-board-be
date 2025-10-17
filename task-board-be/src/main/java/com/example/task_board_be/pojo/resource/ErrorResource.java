package com.example.task_board_be.pojo.resource;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Risorsa di risposta per un errore")
public class ErrorResource {

    @Schema(description = "Codice di stato HTTP dell'errore", example = "404")
    private int status;

    @Schema(description = "Messaggio descrittivo dell'errore", example = "Board not found with id: 1")
    private String message;

    @Schema(description = "Timestamp in cui l'errore è avvenuto", example = "2025-08-22T11:45:00")
    private LocalDateTime timestamp;

    public ErrorResource() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResource(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ErrorResource{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
