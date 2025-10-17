package com.example.task_board_be.service.entity;

import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.service.BaseService;
import com.example.task_board_be.service.fragment.ArchivableService;
import com.example.task_board_be.service.fragment.PageableService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface TaskService extends BaseService<TaskModel , Long> ,
        ArchivableService<TaskModel , Long> ,
        PageableService<TaskModel> {
    Page<TaskModel> getPage(String filterStr ,Long boardId, boolean isArchived , Pageable p );
    List<TaskModel> getList(Long boardId, boolean isArchived);
    TaskModel toggleStateEl(Long id , boolean isArchived);
    TaskModel restoreEl(Long id);
    int restoreList(List<Long> idList);
    int toggleStateList(List<Long> idList, boolean isArchived);
}
