package com.example.task_board_be.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface BaseRepo<T,ID> extends JpaRepository<T , ID> {
    List<T> findAllByIsArchivedFalse();
    List<T> findAllByIsArchivedTrue();
}
