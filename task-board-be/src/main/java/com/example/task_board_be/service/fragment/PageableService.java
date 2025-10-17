package com.example.task_board_be.service.fragment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PageableService<T> {
    Page<T> getPage(String filterStr ,boolean isArchived , Pageable p );
}
