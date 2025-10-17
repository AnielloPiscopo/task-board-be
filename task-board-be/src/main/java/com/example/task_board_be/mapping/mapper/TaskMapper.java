package com.example.task_board_be.mapping.mapper;

import com.example.task_board_be.pojo.entity.Task;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.pojo.resource.TaskResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class TaskMapper implements GenericMapper<TaskModel, Task, TaskResource> {
    @Override
    public Task toEntity(TaskModel taskModel) {
        Task task = new Task();
        task.setId(taskModel.getId());
        task.setDescription(taskModel.getDescription());
        task.setIcon(taskModel.getIcon());
        task.setName(taskModel.getName());
        task.setStatus(taskModel.getStatus());
        return task;
    }

    @Override
    public TaskModel toModel(Task task) {
        TaskModel taskModel = new TaskModel();
        taskModel.setId(task.getId());
        taskModel.setDescription(task.getDescription());
        taskModel.setIcon(task.getIcon());
        taskModel.setName(task.getName());
        taskModel.setStatus(task.getStatus());
        return taskModel;
    }

    @Override
    public TaskResource toResource(TaskModel taskModel) {
        TaskResource taskResource = new TaskResource();
        taskResource.setId(taskModel.getId());
        taskResource.setName(taskModel.getName());
        taskResource.setIcon(taskModel.getIcon());
        taskResource.setStatus(taskModel.getStatus());
        taskResource.setDescription(taskModel.getDescription());
        if (taskModel.getBoardModel() != null) {
            taskResource.setBoardId(taskModel.getBoardModel().getId());
        }
        return taskResource;
    }

    @Override
    public List<Task> toEntityList(List<TaskModel> taskModels) {
        if (taskModels == null || taskModels.isEmpty()) return List.of();
        return taskModels.stream().filter(Objects::nonNull).map(this::toEntity).toList();
    }

    @Override
    public List<TaskModel> toModelList(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return List.of();
        return tasks.stream().filter(Objects::nonNull).map(this::toModel).toList();
    }

    @Override
    public List<TaskResource> toResourceList(List<TaskModel> taskModels) {
        if (taskModels == null || taskModels.isEmpty()) return List.of();
        return taskModels.stream().filter(Objects::nonNull).map(this::toResource).toList();
    }
}
