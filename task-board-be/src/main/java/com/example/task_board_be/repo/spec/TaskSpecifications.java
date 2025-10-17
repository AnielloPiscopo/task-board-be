package com.example.task_board_be.repo.spec;

import com.example.task_board_be.pojo.entity.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class TaskSpecifications {
    private TaskSpecifications() {
    }

    public static Specification<Task> hasArchived(boolean archived) {
        return (root, cq, cb) -> cb.equal(root.get("isArchived"), archived);
    }

    public static Specification<Task> hasBoardId(Long boardId) {
        return (root, cq, cb) -> boardId == null ? null : cb.equal(root.get("board").get("id"), boardId);
    }

    public static Specification<Task> nameContainsIgnoreCase(String searchText) {
        if (StringUtils.isBlank(searchText)) return null;
        String pattern = "%" + searchText.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, cq, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Task> filterTasks(String searchText, boolean archived) {
        return Specification.allOf(
                hasArchived(archived),
                nameContainsIgnoreCase(searchText)
        );
    }

    public static Specification<Task> filterTasksOfBoard(Long boardId, String searchText, boolean archived) {
        return Specification.allOf(
                hasArchived(archived),
                hasBoardId(boardId),
                nameContainsIgnoreCase(searchText)
        );
    }

}
