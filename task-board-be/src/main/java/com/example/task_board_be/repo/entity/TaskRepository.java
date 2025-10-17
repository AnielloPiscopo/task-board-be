package com.example.task_board_be.repo.entity;

import com.example.task_board_be.pojo.entity.Task;
import com.example.task_board_be.repo.BaseRepo;
import com.example.task_board_be.repo.fragment.ArchivableRepo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends
        BaseRepo<Task, Long>,
        ArchivableRepo<Task , Long> ,
        JpaSpecificationExecutor<Task> {
    @Query("""
           select t.name
           from Task t
           where t.board.id = :boardId
             and t.isArchived = false
             and lower(t.name) like lower(concat(:prefix, '%'))
           """)
    List<String> findNamesByPrefixForBoard(@Param("boardId") Long boardId,
                                           @Param("prefix") String prefix);
}
