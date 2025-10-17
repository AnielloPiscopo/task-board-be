package com.example.task_board_be.mapping.mapper;

import com.example.task_board_be.pojo.entity.Board;
import com.example.task_board_be.pojo.entity.Task;
import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.pojo.resource.BoardResource;
import com.example.task_board_be.pojo.resource.TaskResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class BoardMapper implements GenericMapper<BoardModel , Board , BoardResource>{
    private final TaskMapper mapper;

    @Autowired
    public BoardMapper(TaskMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Board toEntity(BoardModel boardModel) {
        Board board = new Board();

        List<TaskModel> taskModels = boardModel.getTaskModelList();
        List<Task> taskList = (taskModels == null ? Collections.<TaskModel>emptyList() : taskModels)
                .stream()
                .filter(Objects::nonNull)
                .map(mapper::toEntity)
                .toList();

        if (boardModel.getId() != null) {
            board.setId(boardModel.getId());
        }

        board.setDescription(boardModel.getDescription());
        board.setName(boardModel.getName());
        board.addTaskList(taskList);

        return board;
    }

    @Override
    public BoardModel toModel(Board board) {
        BoardModel boardModel = new BoardModel();

        boardModel.setId(board.getId());
        boardModel.setName(board.getName());
        boardModel.setDescription(board.getDescription());

        return boardModel;
    }

    @Override
    public BoardResource toResource(BoardModel boardModel) {
        BoardResource boardResource = new BoardResource();

        List<TaskModel> taskModels = boardModel.getTaskModelList();
        List<TaskResource> taskResourceList = (taskModels == null ? Collections.<TaskModel>emptyList() : taskModels)
                .stream()
                .filter(Objects::nonNull)
                .map(mapper::toResource)
                .toList();

        boardResource.setId(boardModel.getId());
        boardResource.setName(boardModel.getName());
        boardResource.setDescription(boardModel.getDescription());
        boardResource.setTaskResourceList(taskResourceList);

        return boardResource;
    }

    @Override
    public List<Board> toEntityList(List<BoardModel> boardModels) {
        if (boardModels == null || boardModels.isEmpty()) return List.of();
        return boardModels.stream().filter(Objects::nonNull).map(this::toEntity).toList();
    }

    @Override
    public List<BoardModel> toModelList(List<Board> boards) {
        if (boards == null || boards.isEmpty()) return List.of();
        return boards.stream().filter(Objects::nonNull).map(this::toModel).toList();
    }

    @Override
    public List<BoardResource> toResourceList(List<BoardModel> boardModels) {
        if (boardModels == null || boardModels.isEmpty()) return List.of();
        return boardModels.stream().filter(Objects::nonNull).map(this::toResource).toList();
    }

    public BoardModel toModelWithCascade(Board board){
        BoardModel boardModel = new BoardModel();

        List<Task> tasks = board.getTaskList();
        List<TaskModel> taskModelList = (tasks == null ? Collections.<Task>emptyList() : tasks)
                .stream()
                .filter(Objects::nonNull)
                .map(mapper::toModel)
                .toList();

        boardModel.setId(board.getId());
        boardModel.setName(board.getName());
        boardModel.setDescription(board.getDescription());
        boardModel.setTaskModelList(taskModelList);

        return boardModel;
    }
}
