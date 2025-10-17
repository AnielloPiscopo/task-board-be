package com.example.task_board_be.mapping.assembler;

import com.example.task_board_be.enums.task.TaskIcon;
import com.example.task_board_be.enums.task.TaskStatus;
import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.pojo.request.CreateTaskRequest;
import com.example.task_board_be.pojo.request.UpdateTaskRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class TaskAssembler {
    public TaskModel assembleModel(CreateTaskRequest request) {
        TaskModel taskModel = new TaskModel();

        taskModel.setName(request.getName());
        taskModel.setDescription(request.getDescription());
        taskModel.setStatus(parseStatusOrNone(request.getStatus()));
        taskModel.setIcon(parseIconOrNone(request.getIcon()));

        if (request.getBoardId() != null) {
            BoardModel boardModel = new BoardModel();
            boardModel.setId(request.getBoardId());

            taskModel.setBoardModel(boardModel);
        }

        return taskModel;
    }

    public TaskModel assembleModel(UpdateTaskRequest request, Long id) {
        TaskModel taskModel = new TaskModel();

        taskModel.setId(id);
        taskModel.setName(request.getName());
        taskModel.setDescription(request.getDescription());
        taskModel.setStatus(parseStatusOrNone(request.getStatus()));
        taskModel.setIcon(parseIconOrNone(request.getIcon()));

        return taskModel;
    }

    private TaskStatus parseStatusOrNone(String raw) {
        if (StringUtils.isBlank(raw)) return TaskStatus.NONE;
        String s = raw.trim();
        try {
            return TaskStatus.fromValue(s);
        } catch (Exception ignored) {
            try {
                return TaskStatus.valueOf(s.toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return TaskStatus.NONE;
            }
        }
    }

    private TaskIcon parseIconOrNone(String raw) {
        if (StringUtils.isBlank(raw)) return TaskIcon.NONE;
        String s = raw.trim();
        try {
            return TaskIcon.fromValue(s);
        } catch (Exception ignored) {
            try {
                return TaskIcon.valueOf(s.toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return TaskIcon.NONE;
            }
        }
    }

}
