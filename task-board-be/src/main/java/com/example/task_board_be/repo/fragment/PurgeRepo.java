package com.example.task_board_be.repo.fragment;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface PurgeRepo<T,ID> extends ArchivableRepo<T , ID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from #{#entityName} e where e.isArchived = true")
    int deleteAllByIsArchivedTrue();
}
