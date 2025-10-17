package com.example.task_board_be.repo.fragment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

@NoRepositoryBean
public interface ArchivableRepo<T,ID> extends JpaRepository<T , ID> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update #{#entityName} e set e.isArchived = true where e.isArchived = false")
    int archiveAllActive();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update #{#entityName} e set e.isArchived = false where e.isArchived = true")
    int restoreAllArchived();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update #{#entityName} e set e.isArchived = true " +
           "where e.id in :ids and e.isArchived = false")
    int archiveByIds(@Param("ids") Collection<ID> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update #{#entityName} e set e.isArchived = false " +
           "where e.id in :ids and e.isArchived = true")
    int restoreByIds(@Param("ids") Collection<ID> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from #{#entityName} e where e.id in :ids and e.isArchived = true")
    int deleteByIdsIfArchived(@Param("ids") Collection<ID> ids);
}
