package com.example.task_board_be.controller.unit;

import com.example.task_board_be.controller.TasksController;
import com.example.task_board_be.enums.BulkOperation;
import com.example.task_board_be.exception.custom.NotFoundException;
import com.example.task_board_be.exception.custom.StateMismatchException;
import com.example.task_board_be.mapping.assembler.TaskAssembler;
import com.example.task_board_be.mapping.mapper.TaskMapper;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.pojo.request.CreateTaskRequest;
import com.example.task_board_be.pojo.request.IdsRequest;
import com.example.task_board_be.pojo.request.UpdateTaskRequest;
import com.example.task_board_be.pojo.resource.BulkResource;
import com.example.task_board_be.pojo.resource.TaskResource;
import com.example.task_board_be.service.entity.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasksControllerUnitTest {

    @InjectMocks
    private TasksController controller;

    @Mock
    private TaskMapper mapper;

    @Mock
    private TaskAssembler assembler;

    @Mock
    private TaskService service;

    @Test
    void testGetTaskPage_ok() {
        TaskModel model = mock(TaskModel.class);
        TaskResource res = mock(TaskResource.class);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TaskModel> page = new PageImpl<>(List.of(model), pageable, 1);

        when(service.getPage(null, 1L, false, pageable)).thenReturn(page);
        when(mapper.toResource(model)).thenReturn(res);

        var resp = controller.getTaskPage(1L, false, null, pageable);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(1, resp.getBody().getTotalElements());
        verify(mapper).toResource(model);
    }

    @Test
    void testGetTask_ok() {
        TaskModel model = mock(TaskModel.class);
        TaskResource res = mock(TaskResource.class);
        when(service.getEl(10L, false)).thenReturn(model);
        when(mapper.toResource(model)).thenReturn(res);

        var resp = controller.getTask(10L, false);

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(res, resp.getBody());
    }

    @Test
    void testGetTask_notFound_bubbles() {
        when(service.getEl(999L, false)).thenThrow(new NotFoundException("Task", 999L));
        assertThrows(NotFoundException.class, () -> controller.getTask(999L, false));
    }

    @Test
    void testCreateTask_ok() {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/tasks");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        CreateTaskRequest request = mock(CreateTaskRequest.class);
        TaskModel m = mock(TaskModel.class);
        TaskResource r = mock(TaskResource.class);

        when(assembler.assembleModel(request)).thenReturn(m);
        when(service.create(m)).thenReturn(m);
        when(mapper.toResource(m)).thenReturn(r);
        when(r.getId()).thenReturn(123L); // usato per costruire Location

        var resp = controller.createTask(request);

        assertEquals(201, resp.getStatusCodeValue());
        assertSame(r, resp.getBody());
        URI loc = resp.getHeaders().getLocation();
        assertNotNull(loc);
        assertTrue(loc.getPath().endsWith("/tasks/123"));
    }

    @Test
    void testUpdateTask_ok() {
        UpdateTaskRequest req = mock(UpdateTaskRequest.class);
        TaskModel model = mock(TaskModel.class);
        TaskResource res = mock(TaskResource.class);

        when(assembler.assembleModel(req, 7L)).thenReturn(model);
        when(service.update(model)).thenReturn(model);
        when(mapper.toResource(model)).thenReturn(res);

        var resp = controller.updateTask(7L, req);

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(res, resp.getBody());
    }

    @Test
    void testArchiveTask_ok() {
        TaskModel model = mock(TaskModel.class);
        TaskResource res = mock(TaskResource.class);

        when(service.archiveEl(5L)).thenReturn(model);
        when(mapper.toResource(model)).thenReturn(res);

        var resp = controller.archiveTask(5L);

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(res, resp.getBody());
    }

    @Test
    void testArchiveTask_stateMismatch_bubbles() {
        when(service.archiveEl(5L)).thenThrow(new StateMismatchException("Task", 5L, true));
        assertThrows(StateMismatchException.class, () -> controller.archiveTask(5L));
    }

    @Test
    void testArchiveTaskList_ok() {
        IdsRequest req = mock(IdsRequest.class);
        when(req.getIdList()).thenReturn(List.of(1L, 2L, 2L)); // distinct()

        when(service.archiveList(List.of(1L, 2L))).thenReturn(2);

        var resp = controller.archiveTaskList(req);

        assertEquals(200, resp.getStatusCodeValue());
        BulkResource body = resp.getBody();
        assertNotNull(body);
        assertEquals(2, body.getUpdatedRow());
        assertEquals(BulkOperation.ARCHIVE_LIST, body.getOperation());
    }

    @Test
    void testRestoreTask_ok() {
        TaskModel model = mock(TaskModel.class);
        TaskResource res = mock(TaskResource.class);
        when(service.restoreEl(3L)).thenReturn(model);
        when(mapper.toResource(model)).thenReturn(res);

        var resp = controller.restoreTask(3L);

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(res, resp.getBody());
    }

    @Test
    void testRestoreTask_stateMismatch_bubbles() {
        when(service.restoreEl(3L)).thenThrow(new StateMismatchException("Task", 3L, false));
        assertThrows(StateMismatchException.class, () -> controller.restoreTask(3L));
    }

    @Test
    void testRestoreTaskList_ok() {
        IdsRequest req = mock(IdsRequest.class);
        when(req.getIdList()).thenReturn(List.of(1L, 1L, 2L));
        when(service.restoreList(List.of(1L, 2L))).thenReturn(2);

        var resp = controller.restoreTaskList(req);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(2, resp.getBody().getUpdatedRow());
        assertEquals(BulkOperation.RESTORE_LIST, resp.getBody().getOperation());
    }

    @Test
    void testDeleteTask_noContent() {
        var resp = controller.deleteTask(11L);
        assertEquals(204, resp.getStatusCodeValue());
        verify(service).delete(11L);
    }

    @Test
    void testDeleteTaskList_ok() {
        IdsRequest req = mock(IdsRequest.class);
        when(req.getIdList()).thenReturn(List.of(9L, 9L, 10L));
        when(service.deleteList(List.of(9L, 10L))).thenReturn(2);

        var resp = controller.deleteTaskList(req);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(2, resp.getBody().getUpdatedRow());
        assertEquals(BulkOperation.DELETE_LIST, resp.getBody().getOperation());
    }

    @Test
    void testGetTaskPage_withFilter_ok() {
        TaskModel model = mock(TaskModel.class);
        TaskResource res = mock(TaskResource.class);
        Pageable pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TaskModel> page = new PageImpl<>(List.of(model), pageable, 11); // totale 11

        when(service.getPage("alpha", 2L, true, pageable)).thenReturn(page);
        when(mapper.toResource(model)).thenReturn(res);

        var resp = controller.getTaskPage(2L, true, "alpha", pageable);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(11, resp.getBody().getTotalElements());
        verify(mapper).toResource(model);
    }
}
