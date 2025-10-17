package com.example.task_board_be.service.entity;

import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.service.BaseService;
import com.example.task_board_be.service.fragment.PageableService;
import com.example.task_board_be.service.fragment.PurgeService;
import org.springframework.stereotype.Component;

@Component
public interface BoardService extends BaseService<BoardModel , Long> ,
        PurgeService<BoardModel , Long>,
        PageableService<BoardModel> {
}
