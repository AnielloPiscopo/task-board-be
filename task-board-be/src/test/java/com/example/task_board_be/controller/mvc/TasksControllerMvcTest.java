package com.example.task_board_be.controller.mvc;

import com.example.task_board_be.controller.TasksController;
import com.example.task_board_be.exception.GlobalExceptionHandler;
import com.example.task_board_be.exception.custom.NotFoundException;
import com.example.task_board_be.exception.custom.StateMismatchException;
import com.example.task_board_be.mapping.assembler.TaskAssembler;
import com.example.task_board_be.mapping.mapper.TaskMapper;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.pojo.resource.TaskResource;
import com.example.task_board_be.service.entity.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TasksController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "spring.mvc.throw-exception-if-no-handler-found=true",
        "spring.web.resources.add-mappings=false"
})
class TasksControllerMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TaskMapper mapper;

    @MockitoBean
    private TaskAssembler assembler;

    @MockitoBean
    private TaskService service;

    @Test
    void testGetTask_ok_http() throws Exception {
        when(service.getEl(1L, false)).thenReturn(new TaskModel());
        when(mapper.toResource(any())).thenReturn(new TaskResource());

        mvc.perform(get("/tasks/1?isArchived=false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testGetTask_notFound_maps404() throws Exception {
        when(service.getEl(123L, false)).thenThrow(new NotFoundException("Task", 123L));

        mvc.perform(get("/tasks/123?isArchived=false"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testArchiveTask_stateMismatch_maps500() throws Exception {
        when(service.archiveEl(5L)).thenThrow(new StateMismatchException("Task", 5L, true));

        mvc.perform(delete("/tasks/archive/5"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testArchiveTaskList_badJson_400() throws Exception {
        mvc.perform(post("/tasks/archive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{")) // JSON malformato
                .andExpect(status().isBadRequest());
    }

    @Test
    void testArchiveTaskList_ok_http() throws Exception {
        when(service.archiveList(any())).thenReturn(2);

        mvc.perform(post("/tasks/archive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idList\":[1,2,2]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testWrongMethod_405() throws Exception {
        // GET su endpoint che accetta POST
        mvc.perform(get("/tasks/archive"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"));
    }

    @Test
    void testNoHandlerFound_404() throws Exception {
        mvc.perform(get("/tasks/not/existing/path"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTaskPage_ok_http() throws Exception {
        var page = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(new com.example.task_board_be.pojo.model.TaskModel()),
                org.springframework.data.domain.PageRequest.of(0, 20),
                1
        );
        org.mockito.Mockito.when(service.getPage(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(77L),
                org.mockito.ArgumentMatchers.eq(false),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(page);
        org.mockito.Mockito.when(mapper.toResource(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.example.task_board_be.pojo.resource.TaskResource());

        mvc.perform(get("/tasks?boardId=77&isArchived=false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetTaskPage_missingBoardId_400() throws Exception {
        mvc.perform(get("/tasks"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTask_created201_withLocation() throws Exception {
        var resource = new com.example.task_board_be.pojo.resource.TaskResource();
        resource.setId(99L);
        org.mockito.Mockito.when(service.create(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.example.task_board_be.pojo.model.TaskModel());
        org.mockito.Mockito.when(mapper.toResource(org.mockito.ArgumentMatchers.any()))
                .thenReturn(resource);

        mvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boardId\":1,\"name\":\"X\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesRegex(".*/tasks/99$")))
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testUpdateTask_ok_http() throws Exception {
        org.mockito.Mockito.when(service.update(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.example.task_board_be.pojo.model.TaskModel());
        org.mockito.Mockito.when(mapper.toResource(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.example.task_board_be.pojo.resource.TaskResource());

        mvc.perform(put("/tasks/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"N\",\"description\":\"D\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testRestoreTask_ok_http() throws Exception {
        org.mockito.Mockito.when(service.restoreEl(5L))
                .thenReturn(new com.example.task_board_be.pojo.model.TaskModel());
        org.mockito.Mockito.when(mapper.toResource(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.example.task_board_be.pojo.resource.TaskResource());

        mvc.perform(put("/tasks/restore/5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testRestoreTaskList_ok_http() throws Exception {
        org.mockito.Mockito.when(service.restoreList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(3);

        mvc.perform(post("/tasks/restore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idList\":[1,2,2]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testDeleteTask_noContent_http() throws Exception {
        mvc.perform(delete("/tasks/delete/15"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteTaskList_ok_http() throws Exception {
        org.mockito.Mockito.when(service.deleteList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(2);

        mvc.perform(post("/tasks/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idList\":[9,9,10]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

}
