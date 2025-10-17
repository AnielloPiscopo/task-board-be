package com.example.task_board_be.service;

import java.util.List;

public interface BaseService<T , ID>{
    List<T> getList(boolean isArchived);
    T getEl(ID id , boolean isArchived);
    T create(T model);
    T update(T model);
    int delete(ID id);
    int deleteList(List<ID> idList);
}
