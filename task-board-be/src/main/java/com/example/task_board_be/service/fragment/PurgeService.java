package com.example.task_board_be.service.fragment;

import java.util.List;

public interface PurgeService<T , ID> extends ArchivableService<T , ID> {
    int archiveList();
    T restoreEl(ID id , boolean cascade);
    int restoreList(boolean cascade);
    int restoreList(List<ID> idList , boolean cascade);
    int clear();
}
