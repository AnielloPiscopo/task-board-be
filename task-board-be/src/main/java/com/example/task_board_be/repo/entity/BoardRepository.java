package com.example.task_board_be.repo.entity;

import com.example.task_board_be.pojo.entity.Board;
import com.example.task_board_be.repo.BaseRepo;
import com.example.task_board_be.repo.fragment.PurgeRepo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends
        BaseRepo<Board , Long>,
        PurgeRepo<Board , Long> ,
        JpaSpecificationExecutor<Board> {
    @Query("select b.name from Board b where lower(b.name) like lower(concat(:prefix, '%'))")
    List<String> findNamesByPrefix(@Param("prefix") String prefix);
}
