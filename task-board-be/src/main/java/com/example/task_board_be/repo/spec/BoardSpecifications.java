package com.example.task_board_be.repo.spec;

import com.example.task_board_be.pojo.entity.Board;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class BoardSpecifications {
    private BoardSpecifications() {}

    public static Specification<Board> hasArchived(boolean archived) {
        return (root, cq, cb) -> cb.equal(root.get("isArchived"), archived);
    }

    public static Specification<Board> nameContainsIgnoreCase(String searchText) {
        if (StringUtils.isBlank(searchText)) return null;
        String pattern = "%" + searchText.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, cq, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Board> filterBoards(String searchText, boolean archived) {
        return Specification.allOf(hasArchived(archived),nameContainsIgnoreCase(searchText));
    }
}
