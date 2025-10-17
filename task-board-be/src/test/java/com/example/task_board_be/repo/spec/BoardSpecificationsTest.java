package com.example.task_board_be.repo.spec;

import com.example.task_board_be.pojo.entity.Board;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BoardSpecificationsTest {

    @Test
    void testHasArchived_buildsEqualPredicate() {
        Root<Board> root = mock(Root.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> archivedPath = mock(Path.class);
        Predicate pred = mock(Predicate.class);

        when(root.get("isArchived")).thenReturn(archivedPath);
        when(cb.equal(archivedPath, true)).thenReturn(pred);

        Specification<Board> spec = BoardSpecifications.hasArchived(true);
        Predicate out = spec.toPredicate(root, cq, cb);

        assertSame(pred, out);
        verify(cb).equal(archivedPath, true);
    }

    @Test
    void testNameContainsIgnoreCase_blank_returnsNull() {
        assertNull(BoardSpecifications.nameContainsIgnoreCase(null));
        assertNull(BoardSpecifications.nameContainsIgnoreCase("   "));
    }

    @Test
    void testNameContainsIgnoreCase_buildsLowerLike() {
        Root<Board> root = mock(Root.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<String> namePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> lowerExpr = (Expression<String>) mock(Expression.class);
        Predicate likePred = mock(Predicate.class);

        // root.get(..) ritorna un Path<?>: castiamo a (Path) per soddisfare il compilatore
        when(root.get("name")).thenReturn((Path) namePath);
        when(cb.lower(namePath)).thenReturn(lowerExpr);
        when(cb.like(lowerExpr, "%foo%")).thenReturn(likePred);

        Specification<Board> spec = BoardSpecifications.nameContainsIgnoreCase(" Foo ");
        Predicate out = spec.toPredicate(root, cq, cb);

        assertSame(likePred, out);
        verify(cb).like(lowerExpr, "%foo%");
    }

    @Test
    void testFilterBoards_combinesSpecs() {
        Root<Board> root = mock(Root.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Object> archivedPath = mock(Path.class);
        Predicate archivedPred = mock(Predicate.class);

        Path<String> namePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> lowerExpr = (Expression<String>) mock(Expression.class);
        Predicate likePred = mock(Predicate.class);
        Predicate combined = mock(Predicate.class);

        when(root.get("isArchived")).thenReturn(archivedPath);
        when(cb.equal(archivedPath, false)).thenReturn(archivedPred);

        when(root.get("name")).thenReturn((Path) namePath);
        when(cb.lower(namePath)).thenReturn(lowerExpr);
        when(cb.like(lowerExpr, "%bar%")).thenReturn(likePred);

        when(cb.and(archivedPred, likePred)).thenReturn(combined);

        Specification<Board> spec = BoardSpecifications.filterBoards("bar", false);
        Predicate out = spec.toPredicate(root, cq, cb);

        assertSame(combined, out);
        verify(cb).and(archivedPred, likePred);
    }
}
