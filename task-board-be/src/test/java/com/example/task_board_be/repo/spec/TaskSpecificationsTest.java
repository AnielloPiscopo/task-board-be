package com.example.task_board_be.repo.spec;

import com.example.task_board_be.pojo.entity.Task;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskSpecificationsTest {

    @Test
    void testHasArchived_buildsEqualPredicate() {
        Root<Task> root = mock(Root.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> archivedPath = mock(Path.class);
        Predicate pred = mock(Predicate.class);

        when(root.get("isArchived")).thenReturn(archivedPath);
        when(cb.equal(archivedPath, false)).thenReturn(pred);

        Specification<Task> spec = TaskSpecifications.hasArchived(false);
        Predicate out = spec.toPredicate(root, cq, cb);

        assertSame(pred, out);
        verify(cb).equal(archivedPath, false);
    }

    @Test
    void testHasBoardId_null_returnsSpecWhosePredicateIsNull() {
        Specification<Task> spec = TaskSpecifications.hasBoardId(null);
        assertNotNull(spec);
        Root<Task> root = mock(Root.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        assertNull(spec.toPredicate(root, cq, cb));
    }


    @Test
    void testHasBoardId_buildsEqualNested() {
        Root<Task> root = mock(Root.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> boardPath = mock(Path.class);
        Path<Object> idPath = mock(Path.class);
        Predicate pred = mock(Predicate.class);

        when(root.get("board")).thenReturn(boardPath);
        when(boardPath.get("id")).thenReturn(idPath);
        when(cb.equal(idPath, 1L)).thenReturn(pred);

        Specification<Task> spec = TaskSpecifications.hasBoardId(1L);
        Predicate out = spec.toPredicate(root, cq, cb);

        assertSame(pred, out);
        verify(cb).equal(idPath, 1L);
    }

    @Test
    void testNameContainsIgnoreCase_blank_returnsNull() {
        assertNull(TaskSpecifications.nameContainsIgnoreCase(null));
        assertNull(TaskSpecifications.nameContainsIgnoreCase("  "));
    }

    @Test
    void testNameContainsIgnoreCase_buildsLike() {
        Root<Task> root = mock(Root.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<String> namePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> lowerExpr = (Expression<String>) mock(Expression.class);
        Predicate likePred = mock(Predicate.class);

        when(root.get("name")).thenReturn((Path) namePath);
        when(cb.lower(namePath)).thenReturn(lowerExpr);
        when(cb.like(lowerExpr, "%todo%")).thenReturn(likePred);

        Specification<Task> spec = TaskSpecifications.nameContainsIgnoreCase(" ToDo ");
        Predicate out = spec.toPredicate(root, cq, cb);

        assertSame(likePred, out);
        verify(cb).like(lowerExpr, "%todo%");
    }

    @Test
    void testFilterTasksOfBoard_combinesAll() {
        Root<Task> root = mock(Root.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Object> archivedPath = mock(Path.class);
        Predicate p1 = mock(Predicate.class);

        Path<Object> boardPath = mock(Path.class);
        Path<Object> idPath = mock(Path.class);
        Predicate p2 = mock(Predicate.class);

        Path<String> namePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> lowerExpr = (Expression<String>) mock(Expression.class);
        Predicate p3 = mock(Predicate.class);

        Predicate combined = mock(Predicate.class);

        when(root.get("isArchived")).thenReturn(archivedPath);
        when(cb.equal(archivedPath, true)).thenReturn(p1);

        when(root.get("board")).thenReturn(boardPath);
        when(boardPath.get("id")).thenReturn(idPath);
        when(cb.equal(idPath, 10L)).thenReturn(p2);

        when(root.get("name")).thenReturn((Path) namePath);
        when(cb.lower(namePath)).thenReturn(lowerExpr);
        when(cb.like(lowerExpr, "%foo%")).thenReturn(p3);

        // 👇 intercetta entrambe le firme possibili
        when(cb.and(any(Predicate[].class))).thenReturn(combined);
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(combined);

        Specification<Task> spec = TaskSpecifications.filterTasksOfBoard(10L, " Foo ", true);
        Predicate out = spec.toPredicate(root, cq, cb);

        assertSame(combined, out); // ora out è sempre il nostro combined

        // opzionale: verifica che i predicate di base siano stati costruiti
        verify(cb).equal(archivedPath, true);
        verify(cb).equal(idPath, 10L);
        verify(cb).like(lowerExpr, "%foo%");
    }

}