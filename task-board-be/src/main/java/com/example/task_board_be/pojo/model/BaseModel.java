package com.example.task_board_be.pojo.model;

import java.time.LocalDateTime;

public abstract class BaseModel {
    private Long id;
    private boolean isArchived = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public BaseModel() {
    }

    public BaseModel(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setIsArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "BaseModel{" +
                "id=" + id +
                ", archived=" + isArchived +
                ", createdAt=" + createdAt +
                ", updateAt=" + updatedAt +
                '}';
    }
}
