package com.example.task_board_be.mapping.assembler;

import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.request.CreateBoardRequest;
import com.example.task_board_be.pojo.request.UpdateBoardRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class BoardAssembler {
    public BoardModel assembleModel(CreateBoardRequest request){
        String name = request.getName();
        String description = request.getDescription();
        BoardModel boardModel = new BoardModel();

        boardModel.setName(name);
        boardModel.setDescription(description);

        return boardModel;
    }

    public BoardModel assembleModel(UpdateBoardRequest request , Long id){
        String name = request.getName();
        String description = request.getDescription();
        BoardModel boardModel = new BoardModel();

        boardModel.setId(id);
        boardModel.setName(name);
        boardModel.setDescription(description);

        return boardModel;
    }
}
