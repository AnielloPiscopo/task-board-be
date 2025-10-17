package com.example.task_board_be.service.fragment;

import java.util.List;

public interface ArchivableService<T,ID> {
    int archiveList(List<ID> idList);
    T archiveEl(ID id);
}
