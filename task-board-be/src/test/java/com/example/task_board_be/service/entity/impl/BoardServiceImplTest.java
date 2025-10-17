package com.example.task_board_be.service.entity.impl;

import com.example.task_board_be.exception.custom.NotFoundException;
import com.example.task_board_be.exception.custom.StateMismatchException;
import com.example.task_board_be.mapping.mapper.BoardMapper;
import com.example.task_board_be.pojo.entity.Board;
import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.repo.entity.BoardRepository;
import com.example.task_board_be.service.entity.TaskService;
import com.example.task_board_be.service.entity.impl.BoardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {

    @Mock
    private BoardRepository repo;
    @Mock
    private BoardMapper mapper;
    @Mock
    private TaskService taskService;

    @InjectMocks
    private BoardServiceImpl service;

    @Test
    void testGetPage_ok() {
        Pageable p = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Board b = mock(Board.class);
        BoardModel m = mock(BoardModel.class);

        when(repo.findAll(ArgumentMatchers.<Specification<Board>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(b), p, 1));
        when(mapper.toModel(b)).thenReturn(m);

        Page<BoardModel> page = service.getPage("foo", false, p);

        assertEquals(1, page.getNumberOfElements());
        verify(repo).findAll(ArgumentMatchers.<Specification<Board>>any(), eq(p));
        verify(mapper).toModel(b);
    }

    @Test
    void testGetList_active_ok() {
        Board b = mock(Board.class);
        BoardModel m = mock(BoardModel.class);

        when(repo.findAllByIsArchivedFalse()).thenReturn(List.of(b));
        when(mapper.toModelList(List.of(b))).thenReturn(List.of(m));

        List<BoardModel> out = service.getList(false);

        assertEquals(1, out.size());
        verify(repo).findAllByIsArchivedFalse();
        verify(mapper).toModelList(List.of(b));
    }

    @Test
    void testGetList_archived_ok() {
        Board b = mock(Board.class);
        BoardModel m = mock(BoardModel.class);

        when(repo.findAllByIsArchivedTrue()).thenReturn(List.of(b));
        when(mapper.toModelList(List.of(b))).thenReturn(List.of(m));

        List<BoardModel> out = service.getList(true);

        assertEquals(1, out.size());
        verify(repo).findAllByIsArchivedTrue();
    }

    @Test
    void testGetEl_ok_active() {
        Board b = mock(Board.class);
        when(b.isArchived()).thenReturn(false);
        when(repo.findById(10L)).thenReturn(Optional.of(b));

        BoardModel m = mock(BoardModel.class);
        when(mapper.toModelWithCascade(b)).thenReturn(m);

        BoardModel out = service.getEl(10L, false);

        assertSame(m, out);
    }

    @Test
    void testGetEl_notFound_throws() {
        when(repo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getEl(999L, false));
    }

    @Test
    void testGetEl_stateMismatch_throws() {
        Board b = mock(Board.class);
        when(b.isArchived()).thenReturn(true);
        when(repo.findById(5L)).thenReturn(Optional.of(b));

        assertThrows(StateMismatchException.class, () -> service.getEl(5L, false));
    }

    @Test
    void testCreate_ok() {
        BoardModel in = mock(BoardModel.class);
        Board ent = mock(Board.class);
        Board saved = mock(Board.class);
        BoardModel outM = mock(BoardModel.class);

        when(mapper.toEntity(in)).thenReturn(ent);
        when(repo.save(ent)).thenReturn(saved);
        when(mapper.toModel(saved)).thenReturn(outM);

        BoardModel res = service.create(in);

        assertSame(outM, res);
        verify(repo).save(ent);
    }

    @Test
    void testUpdate_ok_setsName() {
        BoardModel in = mock(BoardModel.class);
        when(in.getId()).thenReturn(7L);
        when(in.getName()).thenReturn("New Name");
        when(in.getDescription()).thenReturn("Desc");

        Board existing = mock(Board.class);
        when(existing.isArchived()).thenReturn(false);
        when(repo.findById(7L)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        BoardModel outM = mock(BoardModel.class);
        when(mapper.toModelWithCascade(existing)).thenReturn(outM);

        BoardModel res = service.update(in);

        assertSame(outM, res);
        verify(existing).setName("New Name");
        verify(existing).setDescription("Desc");
        verify(repo).save(existing);
    }

    @Test
    void testCreate_whenNameIsBlank_setsDefaultName() {
        when(repo.findNamesByPrefix("New Board"))
                .thenReturn(Arrays.asList("New Board", "New Board 2"));

        BoardModel in = mock(BoardModel.class);
        Board entity = mock(Board.class);
        when(entity.getName()).thenReturn("   ");
        when(mapper.toEntity(in)).thenReturn(entity);

        Board saved = mock(Board.class);
        BoardModel out = mock(BoardModel.class);
        when(repo.save(entity)).thenReturn(saved);
        when(mapper.toModel(saved)).thenReturn(out);

        BoardModel res = service.create(in);

        assertSame(out, res);
        verify(entity).setName("New Board 3");
        verify(repo).findNamesByPrefix("New Board");
        verify(repo).save(entity);
    }

    @Test
    void testCreate_whenNameIsNotBlank_doesNotChangeName() {
        BoardModel in = mock(BoardModel.class);
        Board entity = mock(Board.class);
        when(entity.getName()).thenReturn("Existing");
        when(mapper.toEntity(in)).thenReturn(entity);

        Board saved = mock(Board.class);
        BoardModel out = mock(BoardModel.class);
        when(repo.save(entity)).thenReturn(saved);
        when(mapper.toModel(saved)).thenReturn(out);

        BoardModel res = service.create(in);

        assertSame(out, res);
        verify(entity, never()).setName(anyString());
        verify(repo, never()).findNamesByPrefix(anyString());
        verify(repo).save(entity);
    }


    @Test
    void testUpdate_ok_skipBlankName() {
        BoardModel in = mock(BoardModel.class);
        when(in.getId()).thenReturn(8L);
        when(in.getName()).thenReturn("   ");
        when(in.getDescription()).thenReturn(null);

        Board existing = mock(Board.class);
        when(existing.isArchived()).thenReturn(false);
        when(repo.findById(8L)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        BoardModel outM = mock(BoardModel.class);
        when(mapper.toModelWithCascade(existing)).thenReturn(outM);

        BoardModel res = service.update(in);

        assertSame(outM, res);
        verify(existing, never()).setName(anyString());
        verify(repo).save(existing);
    }

    @Test
    void testArchiveEl_ok() {
        when(repo.archiveByIds(List.of(5L))).thenReturn(1);
        Board after = mock(Board.class);
        when(repo.findById(5L)).thenReturn(Optional.of(after));
        BoardModel outM = mock(BoardModel.class);
        when(mapper.toModelWithCascade(after)).thenReturn(outM);

        BoardModel res = service.archiveEl(5L);

        assertSame(outM, res);
        verify(repo).archiveByIds(List.of(5L));
    }

    @Test
    void testArchiveEl_zeroUpdated_notFound_throws() {
        when(repo.archiveByIds(List.of(50L))).thenReturn(0);
        when(repo.existsById(50L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.archiveEl(50L));
    }

    @Test
    void testArchiveEl_zeroUpdated_stateMismatch_throws() {
        when(repo.archiveByIds(List.of(51L))).thenReturn(0);
        when(repo.existsById(51L)).thenReturn(true);

        assertThrows(StateMismatchException.class, () -> service.archiveEl(51L));
    }

    @Test
    void testArchiveEl_afterUpdate_missing_illegalArgument() {
        when(repo.archiveByIds(List.of(70L))).thenReturn(1);
        when(repo.findById(70L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.archiveEl(70L));
    }

    @Test
    void testArchiveList_empty_returnsZero() {
        int res = service.archiveList(List.of());
        assertEquals(0, res);
    }

    @Test
    void testArchiveList_zeroUpdated_returnsZero() {
        when(repo.archiveByIds(List.of(1L, 2L))).thenReturn(0);
        int res = service.archiveList(List.of(1L, 2L));
        assertEquals(0, res);
    }

    @Test
    void testArchiveList_ok_returnsCount() {
        when(repo.archiveByIds(List.of(3L, 4L))).thenReturn(2);
        int res = service.archiveList(List.of(3L, 4L));
        assertEquals(2, res);
    }

    @Test
    void testArchiveAll_zeroUpdated_returnsZero() {
        when(repo.archiveAllActive()).thenReturn(0);
        int res = service.archiveList();
        assertEquals(0, res);
    }

    @Test
    void testArchiveAll_afterUpdate_noBoards_returnsZero() {
        when(repo.archiveAllActive()).thenReturn(5);
        when(repo.findAllByIsArchivedTrue()).thenReturn(List.of());
        int res = service.archiveList();
        assertEquals(0, res);
    }

    @Test
    void testArchiveAll_ok_setsFlagAndReturnsCount() {
        when(repo.archiveAllActive()).thenReturn(3);
        Board b1 = mock(Board.class);
        Board b2 = mock(Board.class);
        when(repo.findAllByIsArchivedTrue()).thenReturn(List.of(b1, b2));

        int res = service.archiveList();

        assertEquals(3, res);
        verify(b1).setIsArchived(true);
        verify(b2).setIsArchived(true);
    }

    @Test
    void testRestoreEl_ok_withTasksFalse() {
        when(repo.restoreByIds(List.of(9L))).thenReturn(1);
        Board after = mock(Board.class);
        when(repo.findById(9L)).thenReturn(Optional.of(after));
        BoardModel outM = mock(BoardModel.class);
        when(mapper.toModelWithCascade(after)).thenReturn(outM);

        BoardModel res = service.restoreEl(9L, false);

        assertSame(outM, res);
        verify(taskService, never()).getList(anyLong(), anyBoolean());
        verify(taskService, never()).restoreList(anyList());
    }

    @Test
    void testRestoreEl_ok_withTasksTrue_emptyTaskList() {
        when(repo.restoreByIds(List.of(10L))).thenReturn(1);
        when(taskService.getList(10L, true)).thenReturn(List.of());
        Board after = mock(Board.class);
        when(repo.findById(10L)).thenReturn(Optional.of(after));
        when(mapper.toModelWithCascade(after)).thenReturn(mock(BoardModel.class));

        BoardModel res = service.restoreEl(10L, true);

        assertNotNull(res);
        verify(taskService).getList(10L, true);
        verify(taskService, never()).restoreList(anyList());
    }

    @Test
    void testRestoreEl_ok_withTasksTrue_withTasksRestored() {
        when(repo.restoreByIds(List.of(11L))).thenReturn(1);
        TaskModel tm1 = mock(TaskModel.class);
        when(tm1.getId()).thenReturn(101L);
        TaskModel tm2 = mock(TaskModel.class);
        when(tm2.getId()).thenReturn(102L);
        when(taskService.getList(11L, true)).thenReturn(List.of(tm1, tm2));
        Board after = mock(Board.class);
        when(repo.findById(11L)).thenReturn(Optional.of(after));
        when(mapper.toModelWithCascade(after)).thenReturn(mock(BoardModel.class));

        service.restoreEl(11L, true);

        verify(taskService).restoreList(List.of(101L, 102L));
    }

    @Test
    void testRestoreEl_zeroUpdated_notFound_throws() {
        when(repo.restoreByIds(List.of(60L))).thenReturn(0);
        when(repo.existsById(60L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.restoreEl(60L, true));
    }

    @Test
    void testRestoreEl_zeroUpdated_stateMismatch_throws() {
        when(repo.restoreByIds(List.of(61L))).thenReturn(0);
        when(repo.existsById(61L)).thenReturn(true);

        assertThrows(StateMismatchException.class, () -> service.restoreEl(61L, true));
    }

    @Test
    void testRestoreEl_afterUpdate_missing_illegalArgument() {
        when(repo.restoreByIds(List.of(62L))).thenReturn(1);
        when(repo.findById(62L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.restoreEl(62L, true));
    }

    @Test
    void testRestoreList_ids_nullOrEmpty_returnsZero() {
        assertEquals(0, service.restoreList(null, true));
        assertEquals(0, service.restoreList(List.of(), true));
    }

    @Test
    void testRestoreList_ids_zeroUpdated_returnsZero() {
        when(repo.restoreByIds(List.of(1L, 2L))).thenReturn(0);
        int res = service.restoreList(List.of(1L, 2L), true);
        assertEquals(0, res);
    }

    @Test
    void testRestoreList_ids_withTasksFalse_ok() {
        when(repo.restoreByIds(List.of(3L, 4L))).thenReturn(2);

        int res = service.restoreList(List.of(3L, 4L), false);

        assertEquals(2, res);
        verify(taskService, never()).getList(anyLong(), anyBoolean());
        verify(taskService, never()).restoreList(anyList());
    }

    @Test
    void testRestoreList_ids_withTasksTrue_aggregatesAndRestores() {
        when(repo.restoreByIds(List.of(5L, 6L))).thenReturn(2);

        TaskModel t1 = mock(TaskModel.class);
        when(t1.getId()).thenReturn(201L);
        TaskModel t2 = mock(TaskModel.class);
        when(t2.getId()).thenReturn(202L);
        when(taskService.getList(5L, true)).thenReturn(List.of(t1));
        when(taskService.getList(6L, true)).thenReturn(List.of(t2));

        int res = service.restoreList(List.of(5L, 6L), true);

        assertEquals(2, res);
        verify(taskService).restoreList(List.of(201L, 202L));
    }

    @Test
    void testRestoreList_ids_withTasksTrue_emptyTaskList() {
        when(repo.restoreByIds(List.of(10L, 20L))).thenReturn(2);

        when(taskService.getList(anyLong(), eq(true))).thenReturn(List.of());

        int result = service.restoreList(List.of(10L, 20L), true);

        assertEquals(2, result);
        verify(taskService, times(2)).getList(anyLong(), eq(true));
        verify(taskService, never()).restoreList(anyList());
    }


    @Test
    void testRestoreAll_zeroUpdated_returnsZero() {
        when(repo.restoreAllArchived()).thenReturn(0);
        int res = service.restoreList(false);
        assertEquals(0, res);
    }

    @Test
    void testRestoreAll_afterUpdate_noBoards_returnsZero() {
        when(repo.restoreAllArchived()).thenReturn(3);
        when(repo.findAllByIsArchivedFalse()).thenReturn(List.of());

        int res = service.restoreList(true);

        assertEquals(0, res);
    }

    @Test
    void testRestoreAll_withTasksFalse_setsFlagAndReturns() {
        when(repo.restoreAllArchived()).thenReturn(4);

        Board b1 = mock(Board.class);
        Board b2 = mock(Board.class);
        when(repo.findAllByIsArchivedFalse()).thenReturn(List.of(b1, b2));

        int res = service.restoreList(false);

        assertEquals(4, res);
        verify(b1).setIsArchived(false);
        verify(b2).setIsArchived(false);
        verify(taskService, never()).restoreList(anyList());
    }

    @Test
    void testRestoreAll_withTasksTrue_restoresTasks() {
        when(repo.restoreAllArchived()).thenReturn(2);
        Board b1 = mock(Board.class);
        when(b1.getId()).thenReturn(30L);
        Board b2 = mock(Board.class);
        when(b2.getId()).thenReturn(31L);
        when(repo.findAllByIsArchivedFalse()).thenReturn(List.of(b1, b2));

        TaskModel t1 = mock(TaskModel.class);
        when(t1.getId()).thenReturn(301L);
        TaskModel t2 = mock(TaskModel.class);
        when(t2.getId()).thenReturn(302L);
        when(taskService.getList(30L, true)).thenReturn(List.of(t1));
        when(taskService.getList(31L, true)).thenReturn(List.of(t2));

        int res = service.restoreList(true);

        assertEquals(2, res);
        verify(taskService).restoreList(List.of(301L, 302L));
        verify(b1).setIsArchived(false);
        verify(b2).setIsArchived(false);
    }

    @Test
    void testRestoreList_all_withTasksTrue_emptyTaskList() {
        when(repo.restoreAllArchived()).thenReturn(2);

        Board b1 = mock(Board.class);
        Board b2 = mock(Board.class);
        when(b1.getId()).thenReturn(1L);
        when(b2.getId()).thenReturn(2L);
        when(repo.findAllByIsArchivedFalse()).thenReturn(List.of(b1, b2));

        when(taskService.getList(anyLong(), eq(true))).thenReturn(List.of());

        int result = service.restoreList(true);

        assertEquals(2, result);
        verify(taskService, times(2)).getList(anyLong(), eq(true));
        verify(taskService, never()).restoreList(anyList());
        verify(b1).setIsArchived(false);
        verify(b2).setIsArchived(false);
    }


    @Test
    void testDelete_ok_returnsCount() {
        when(repo.deleteByIdsIfArchived(List.of(9L))).thenReturn(1);

        int count = service.delete(9L);

        assertEquals(1, count);
        verify(repo).deleteByIdsIfArchived(List.of(9L));
    }

    @Test
    void testDelete_zeroUpdated_notFound_throws() {
        when(repo.deleteByIdsIfArchived(List.of(90L))).thenReturn(0);
        when(repo.existsById(90L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.delete(90L));
    }

    @Test
    void testDelete_zeroUpdated_stateMismatch_throws() {
        when(repo.deleteByIdsIfArchived(List.of(91L))).thenReturn(0);
        when(repo.existsById(91L)).thenReturn(true);

        assertThrows(StateMismatchException.class, () -> service.delete(91L));
    }

    @Test
    void testDeleteList_ok_returnsCount() {
        when(repo.deleteByIdsIfArchived(List.of(1L, 2L))).thenReturn(2);

        int count = service.deleteList(List.of(1L, 2L));

        assertEquals(2, count);
    }

    @Test
    void testDeleteList_zeroUpdated_returnsZero() {
        when(repo.deleteByIdsIfArchived(List.of(7L, 8L))).thenReturn(0);

        int count = service.deleteList(List.of(7L, 8L));

        assertEquals(0, count);
    }

    @Test
    void testClear_ok_returnsCount() {
        when(repo.deleteAllByIsArchivedTrue()).thenReturn(6);

        int count = service.clear();

        assertEquals(6, count);
    }

    @Test
    void testClear_zeroUpdated_returnsZero() {
        when(repo.deleteAllByIsArchivedTrue()).thenReturn(0);

        int count = service.clear();

        assertEquals(0, count);
    }

    @Test
    void testCreate_generatesNextDefaultName_withForAndWhileLoops() {
        List<String> existingNames = Arrays.asList(
                "New Board",
                "New Board 2",
                "New Board 3",
                "new board abc",
                null,
                "Random Name"
        );
        when(repo.findNamesByPrefix("New Board")).thenReturn(existingNames);

        BoardModel modelIn = mock(BoardModel.class);
        Board entityIn = mock(Board.class);
        when(entityIn.getName()).thenReturn("   ");
        when(mapper.toEntity(modelIn)).thenReturn(entityIn);

        Board saved = mock(Board.class);
        when(repo.save(any(Board.class))).thenReturn(saved);
        when(mapper.toModel(saved)).thenReturn(mock(BoardModel.class));

        service.create(modelIn);

        verify(entityIn).setName("New Board 4");
    }

    @Test
    void testCreate_defaultName_coversCounterGte2Branch() {
        List<String> existing = Arrays.asList("New Board", "New Board 1", "New Board 2", "Other");
        when(repo.findNamesByPrefix("New Board")).thenReturn(existing);

        BoardModel in = mock(BoardModel.class);
        Board entity = mock(Board.class);
        when(entity.getName()).thenReturn("   ");
        when(mapper.toEntity(in)).thenReturn(entity);

        Board saved = mock(Board.class);
        when(repo.save(any(Board.class))).thenReturn(saved);
        when(mapper.toModel(saved)).thenReturn(mock(BoardModel.class));

        service.create(in);

        verify(entity).setName("New Board 3");
    }

}
