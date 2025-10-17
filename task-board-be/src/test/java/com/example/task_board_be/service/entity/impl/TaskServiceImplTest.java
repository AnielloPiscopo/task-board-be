package com.example.task_board_be.service.entity.impl;

import com.example.task_board_be.enums.task.TaskIcon;
import com.example.task_board_be.enums.task.TaskStatus;
import com.example.task_board_be.exception.custom.NotFoundException;
import com.example.task_board_be.exception.custom.StateMismatchException;
import com.example.task_board_be.mapping.mapper.BoardMapper;
import com.example.task_board_be.mapping.mapper.TaskMapper;
import com.example.task_board_be.pojo.entity.Board;
import com.example.task_board_be.pojo.entity.Task;
import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.repo.entity.BoardRepository;
import com.example.task_board_be.repo.entity.TaskRepository;
import com.example.task_board_be.service.entity.impl.TaskServiceImpl;
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
class TaskServiceImplTest {

    @Mock
    private TaskRepository repo;
    @Mock
    private TaskMapper mapper;
    @Mock
    private BoardMapper boardMapper;
    @Mock
    private BoardRepository boardRepo;

    @InjectMocks
    private TaskServiceImpl service;

    @Test
    void testGetPage_ok() {
        Pageable p = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Task t = mock(Task.class);
        TaskModel m = mock(TaskModel.class);

        when(repo.findAll(ArgumentMatchers.<Specification<Task>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(t), p, 1));
        when(mapper.toModel(t)).thenReturn(m);

        Page<TaskModel> page = service.getPage("foo", false, p);

        assertEquals(1, page.getTotalElements());
        verify(repo).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(p));
        verify(mapper).toModel(t);
    }

    @Test
    void testGetPage_withBoard_ok() {
        Pageable p = PageRequest.of(1, 10);
        Task t = mock(Task.class);
        TaskModel m = mock(TaskModel.class);

        when(repo.findAll(ArgumentMatchers.<Specification<Task>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(t), p, 1));
        when(mapper.toModel(t)).thenReturn(m);

        Page<TaskModel> page = service.getPage("bar", 99L, true, p);

        assertEquals(1, page.getNumberOfElements());
        verify(repo).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(p));
        verify(mapper).toModel(t);
    }

    @Test
    void testGetList_ok() {
        Task t = mock(Task.class);
        TaskModel m = mock(TaskModel.class);
        when(repo.findAll(ArgumentMatchers.<Specification<Task>>any())).thenReturn(List.of(t));
        when(mapper.toModelList(List.of(t))).thenReturn(List.of(m));

        List<TaskModel> list = service.getList(false);

        assertEquals(1, list.size());
        verify(mapper).toModelList(List.of(t));
    }

    @Test
    void testGetList_withBoard_ok() {
        Task t = mock(Task.class);
        TaskModel m = mock(TaskModel.class);

        when(repo.findAll(ArgumentMatchers.<Specification<Task>>any()))
                .thenReturn(List.of(t));
        when(mapper.toModelList(List.of(t))).thenReturn(List.of(m));

        List<TaskModel> list = service.getList(77L, true);

        assertEquals(1, list.size());
        verify(repo).findAll(ArgumentMatchers.<Specification<Task>>any());
    }

    @Test
    void testGetEl_ok_active() {
        Task t = mock(Task.class);
        when(t.isArchived()).thenReturn(false);
        when(repo.findById(10L)).thenReturn(Optional.of(t));

        TaskModel m = mock(TaskModel.class);
        when(mapper.toModel(t)).thenReturn(m);

        TaskModel out = service.getEl(10L, false);

        assertSame(m, out);
    }

    @Test
    void testGetEl_notFound_throws() {
        when(repo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getEl(999L, false));
    }

    @Test
    void testGetEl_stateMismatch_throws() {
        Task t = mock(Task.class);
        when(t.isArchived()).thenReturn(true);
        when(repo.findById(5L)).thenReturn(Optional.of(t));

        assertThrows(StateMismatchException.class, () -> service.getEl(5L, false));
    }

    @Test
    void testCreate_defaults_blankName_nullEnums_generatesNameAndSetsDefaults() {
        TaskModel input = mock(TaskModel.class);
        BoardModel bm = new BoardModel();
        bm.setId(42L);
        when(input.getBoardModel()).thenReturn(bm);

        Board board = mock(Board.class);
        when(boardRepo.findById(42L)).thenReturn(Optional.of(board));

        Task entity = mock(Task.class);
        when(entity.getName()).thenReturn("  ");
        when(entity.getStatus()).thenReturn(null);
        when(entity.getIcon()).thenReturn(null);
        when(mapper.toEntity(input)).thenReturn(entity);

        when(repo.findNamesByPrefixForBoard(42L, "New Task"))
                .thenReturn(Arrays.asList("New Task", "New Task 2", "New Task 3", "New Task 5", null, "  new task  ", "New Task x"));

        Task saved = mock(Task.class);
        when(repo.save(entity)).thenReturn(saved);

        TaskModel out = mock(TaskModel.class);
        when(mapper.toModel(saved)).thenReturn(out);

        BoardModel mappedBoard = mock(BoardModel.class);
        when(boardMapper.toModel(board)).thenReturn(mappedBoard);

        TaskModel res = service.create(input);

        assertSame(out, res);
        verify(entity).setName("New Task 4");
        verify(entity).setStatus(TaskStatus.NONE);
        verify(entity).setIcon(TaskIcon.NONE);
        verify(repo).save(entity);
        verify(boardMapper).toModel(board);
    }


    @Test
    void testCreate_withExplicitValues_noDefaults() {
        TaskModel input = mock(TaskModel.class);
        BoardModel bm = new BoardModel();
        bm.setId(7L);
        when(input.getBoardModel()).thenReturn(bm);

        Board board = mock(Board.class);
        when(boardRepo.findById(7L)).thenReturn(Optional.of(board));

        Task entity = mock(Task.class);
        when(entity.getName()).thenReturn("Given");
        when(entity.getStatus()).thenReturn(TaskStatus.DONE);
        when(entity.getIcon()).thenReturn(TaskIcon.BUG);
        when(mapper.toEntity(input)).thenReturn(entity);

        Task saved = mock(Task.class);
        when(repo.save(entity)).thenReturn(saved);

        TaskModel out = mock(TaskModel.class);
        when(mapper.toModel(saved)).thenReturn(out);

        BoardModel mappedBoard = mock(BoardModel.class);
        when(boardMapper.toModel(board)).thenReturn(mappedBoard);

        TaskModel res = service.create(input);

        assertSame(out, res);
        verify(entity, never()).setName(anyString());
        verify(entity, never()).setStatus(TaskStatus.NONE);
        verify(entity, never()).setIcon(TaskIcon.NONE);
        verify(repo).save(entity);
    }

    @Test
    void testCreate_blankName_noExistingNames_returnsBaseAndSetsDefaults() {
        TaskModel in = mock(TaskModel.class);
        BoardModel bm = new BoardModel();
        bm.setId(100L);
        when(in.getBoardModel()).thenReturn(bm);

        Board board = mock(Board.class);
        when(boardRepo.findById(100L)).thenReturn(Optional.of(board));

        Task entity = mock(Task.class);
        when(entity.getName()).thenReturn(" ");
        when(entity.getStatus()).thenReturn(null);
        when(entity.getIcon()).thenReturn(null);
        when(mapper.toEntity(in)).thenReturn(entity);

        when(repo.findNamesByPrefixForBoard(100L, "New Task")).thenReturn(List.of());

        Task saved = mock(Task.class);
        when(repo.save(entity)).thenReturn(saved);

        TaskModel out = mock(TaskModel.class);
        when(mapper.toModel(saved)).thenReturn(out);
        BoardModel mappedBoard = mock(BoardModel.class);
        when(boardMapper.toModel(board)).thenReturn(mappedBoard);

        TaskModel res = service.create(in);

        assertSame(out, res);
        verify(entity).setName("New Task");
        verify(entity).setStatus(TaskStatus.NONE);
        verify(entity).setIcon(TaskIcon.NONE);
        verify(out).setBoardModel(mappedBoard);
    }

    @Test
    void testCreate_blankName_namesCoverAllBranches_generatesMissingIndex() {
        TaskModel in = mock(TaskModel.class);
        BoardModel bm = new BoardModel();
        bm.setId(42L);
        when(in.getBoardModel()).thenReturn(bm);

        Board board = mock(Board.class);
        when(boardRepo.findById(42L)).thenReturn(Optional.of(board));

        Task entity = mock(Task.class);
        when(entity.getName()).thenReturn("   ");
        when(entity.getStatus()).thenReturn(null);
        when(entity.getIcon()).thenReturn(null);
        when(mapper.toEntity(in)).thenReturn(entity);

        when(repo.findNamesByPrefixForBoard(42L, "New Task"))
                .thenReturn(Arrays.asList(
                        null,
                        "New Task",
                        "  new task  ",
                        "New Task 2",
                        "New Task 3",
                        "New Task x",
                        "New Task 5"
                ));

        Task saved = mock(Task.class);
        when(repo.save(entity)).thenReturn(saved);

        TaskModel out = mock(TaskModel.class);
        when(mapper.toModel(saved)).thenReturn(out);
        BoardModel mappedBoard = mock(BoardModel.class);
        when(boardMapper.toModel(board)).thenReturn(mappedBoard);

        TaskModel res = service.create(in);

        assertSame(out, res);
        verify(entity).setName("New Task 4");
        verify(entity).setStatus(TaskStatus.NONE);
        verify(entity).setIcon(TaskIcon.NONE);
        verify(out).setBoardModel(mappedBoard);
    }

    @Test
    void testCreate_blankName_namesOnlyBaseTriggersWhileOnce() {
        TaskModel in = mock(TaskModel.class);
        BoardModel bm = new BoardModel();
        bm.setId(77L);
        when(in.getBoardModel()).thenReturn(bm);

        Board board = mock(Board.class);
        when(boardRepo.findById(77L)).thenReturn(Optional.of(board));

        Task entity = mock(Task.class);
        when(entity.getName()).thenReturn("");
        when(entity.getStatus()).thenReturn(null);
        when(entity.getIcon()).thenReturn(null);
        when(mapper.toEntity(in)).thenReturn(entity);

        when(repo.findNamesByPrefixForBoard(77L, "New Task")).thenReturn(List.of("New Task"));

        Task saved = mock(Task.class);
        when(repo.save(entity)).thenReturn(saved);

        TaskModel out = mock(TaskModel.class);
        when(mapper.toModel(saved)).thenReturn(out);

        TaskModel res = service.create(in);

        assertSame(out, res);
        verify(entity).setName("New Task 2");
    }

    @Test
    void testUpdate_setsAllFields() {
        TaskModel in = mock(TaskModel.class);
        when(in.getId()).thenReturn(15L);
        when(in.getName()).thenReturn("N");
        when(in.getDescription()).thenReturn("D");
        when(in.getStatus()).thenReturn(TaskStatus.IN_PROGRESS);
        when(in.getIcon()).thenReturn(TaskIcon.FEATURE);

        Task existing = mock(Task.class);
        when(existing.isArchived()).thenReturn(false);
        when(repo.findById(15L)).thenReturn(Optional.of(existing));

        TaskModel out = mock(TaskModel.class);
        when(mapper.toModel(existing)).thenReturn(out);

        TaskModel res = service.update(in);

        assertSame(out, res);
        verify(existing).setName("N");
        verify(existing).setDescription("D");
        verify(existing).setStatus(TaskStatus.IN_PROGRESS);
        verify(existing).setIcon(TaskIcon.FEATURE);
        verify(repo).save(existing);
    }

    @Test
    void testUpdate_skipBlankName() {
        TaskModel in = mock(TaskModel.class);
        when(in.getId()).thenReturn(16L);
        when(in.getName()).thenReturn("   ");
        when(in.getDescription()).thenReturn("D2");
        when(in.getStatus()).thenReturn(TaskStatus.TODO);
        when(in.getIcon()).thenReturn(TaskIcon.TEST);

        Task existing = mock(Task.class);
        when(existing.isArchived()).thenReturn(false);
        when(repo.findById(16L)).thenReturn(Optional.of(existing));

        TaskModel out = mock(TaskModel.class);
        when(mapper.toModel(existing)).thenReturn(out);

        TaskModel res = service.update(in);

        assertSame(out, res);
        verify(existing, never()).setName(anyString());
        verify(existing).setDescription("D2");
        verify(existing).setStatus(TaskStatus.TODO);
        verify(existing).setIcon(TaskIcon.TEST);
        verify(repo).save(existing);
    }

    @Test
    void testArchiveEl_wrapper_ok() {
        when(repo.archiveByIds(List.of(5L))).thenReturn(1);
        Task after = mock(Task.class);
        when(repo.findById(5L)).thenReturn(Optional.of(after));
        TaskModel out = mock(TaskModel.class);
        when(mapper.toModel(after)).thenReturn(out);

        TaskModel res = service.archiveEl(5L);

        assertSame(out, res);
        verify(repo).archiveByIds(List.of(5L));
    }

    @Test
    void testRestoreEl_wrapper_ok() {
        when(repo.restoreByIds(List.of(6L))).thenReturn(1);
        Task after = mock(Task.class);
        when(repo.findById(6L)).thenReturn(Optional.of(after));
        TaskModel out = mock(TaskModel.class);
        when(mapper.toModel(after)).thenReturn(out);

        TaskModel res = service.restoreEl(6L);

        assertSame(out, res);
        verify(repo).restoreByIds(List.of(6L));
    }

    @Test
    void testArchiveList_wrapper_ok() {
        when(repo.archiveByIds(List.of(1L, 2L))).thenReturn(2);
        int res = service.archiveList(List.of(1L, 2L));
        assertEquals(2, res);
    }

    @Test
    void testArchiveList_wrapper_empty_returnsZero() {
        int res = service.archiveList(List.of());
        assertEquals(0, res);
    }

    @Test
    void testRestoreList_wrapper_ok() {
        when(repo.restoreByIds(List.of(3L, 4L))).thenReturn(2);
        int res = service.restoreList(List.of(3L, 4L));
        assertEquals(2, res);
    }

    @Test
    void testRestoreList_wrapper_empty_returnsZero() {
        int res = service.restoreList(List.of());
        assertEquals(0, res);
    }

    @Test
    void testToggleStateEl_archive_ok() {
        when(repo.archiveByIds(List.of(5L))).thenReturn(1);
        Task after = mock(Task.class);
        when(repo.findById(5L)).thenReturn(Optional.of(after));
        TaskModel out = mock(TaskModel.class);
        when(mapper.toModel(after)).thenReturn(out);

        TaskModel res = service.toggleStateEl(5L, false);

        assertSame(out, res);
        verify(repo).archiveByIds(List.of(5L));
    }

    @Test
    void testToggleStateEl_restore_ok() {
        when(repo.restoreByIds(List.of(6L))).thenReturn(1);
        Task after = mock(Task.class);
        when(repo.findById(6L)).thenReturn(Optional.of(after));
        TaskModel out = mock(TaskModel.class);
        when(mapper.toModel(after)).thenReturn(out);

        TaskModel res = service.toggleStateEl(6L, true);

        assertSame(out, res);
        verify(repo).restoreByIds(List.of(6L));
    }

    @Test
    void testToggleStateEl_archive_zeroUpdated_notFound_throws() {
        when(repo.archiveByIds(List.of(50L))).thenReturn(0);
        when(repo.existsById(50L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.toggleStateEl(50L, false));
    }

    @Test
    void testToggleStateEl_archive_zeroUpdated_stateMismatch_throws() {
        when(repo.archiveByIds(List.of(51L))).thenReturn(0);
        when(repo.existsById(51L)).thenReturn(true);

        assertThrows(StateMismatchException.class, () -> service.toggleStateEl(51L, false));
    }

    @Test
    void testToggleStateEl_afterUpdate_entityMissing_illegalState() {
        when(repo.restoreByIds(List.of(70L))).thenReturn(1);
        when(repo.findById(70L)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.toggleStateEl(70L, true));
        assertTrue(ex.getMessage().contains("non trovata dopo l'update"));
    }

    @Test
    void testToggleStateList_empty_returnsZero() {
        int res = service.toggleStateList(List.of(), true);
        assertEquals(0, res);
    }

    @Test
    void testToggleStateList_archive_zeroUpdated_returnsZero() {
        when(repo.archiveByIds(List.of(1L, 2L))).thenReturn(0);

        int res = service.toggleStateList(List.of(1L, 2L), false);

        assertEquals(0, res);
    }

    @Test
    void testToggleStateList_restore_ok_returnsUpdated() {
        when(repo.restoreByIds(List.of(3L, 4L))).thenReturn(2);

        int res = service.toggleStateList(List.of(3L, 4L), true);

        assertEquals(2, res);
    }

    @Test
    void testToggleStateList_archive_ok_returnsUpdated() {
        when(repo.archiveByIds(List.of(9L, 10L))).thenReturn(2);

        int res = service.toggleStateList(List.of(9L, 10L), false);

        assertEquals(2, res);
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
    void testDeleteList_null_returnsZero() {
        int count = service.deleteList(null);
        assertEquals(0, count);
    }
}
