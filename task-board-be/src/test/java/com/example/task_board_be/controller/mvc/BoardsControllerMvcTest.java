package com.example.task_board_be.controller.mvc;

import com.example.task_board_be.controller.BoardsController;
import com.example.task_board_be.exception.GlobalExceptionHandler;
import com.example.task_board_be.mapping.assembler.BoardAssembler;
import com.example.task_board_be.mapping.mapper.BoardMapper;
import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.resource.BoardResource;
import com.example.task_board_be.service.entity.BoardService;
import org.hamcrest.Matchers;
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

@WebMvcTest(controllers = BoardsController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "spring.mvc.throw-exception-if-no-handler-found=true",
        "spring.web.resources.add-mappings=false"
})
class BoardsControllerMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BoardMapper mapper;
    @MockitoBean
    private BoardAssembler assembler;
    @MockitoBean
    private BoardService service;

    @Test
    void testGetBoardPage_ok_minimal() throws Exception {
        var page = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(new com.example.task_board_be.pojo.model.BoardModel()),
                org.springframework.data.domain.PageRequest.of(0, 20),
                1
        );
        org.mockito.Mockito.when(service.getPage(org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.eq(false), org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);
        org.mockito.Mockito.when(mapper.toResource(org.mockito.ArgumentMatchers.any())).thenReturn(new com.example.task_board_be.pojo.resource.BoardResource());

        mvc.perform(get("/boards?isArchived=false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateBoard_ok_minimal() throws Exception {
        org.mockito.Mockito.when(assembler.assembleModel(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(42L)))
                .thenReturn(new com.example.task_board_be.pojo.model.BoardModel());
        org.mockito.Mockito.when(service.update(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.example.task_board_be.pojo.model.BoardModel());
        org.mockito.Mockito.when(mapper.toResource(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.example.task_board_be.pojo.resource.BoardResource());

        mvc.perform(put("/boards/42")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"description\":\"Y\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }


    @Test
    void testCreateBoard_created201_http_withLocation() throws Exception {
        when(service.create(any())).thenReturn(new BoardModel());
        BoardResource resource = org.mockito.Mockito.mock(BoardResource.class);
        when(resource.getId()).thenReturn(55L);
        when(mapper.toResource(any())).thenReturn(resource);

        mvc.perform(post("/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alpha\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.matchesRegex(".*/boards/55$")))
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testCreateBoard_400_onValidation() throws Exception {
        String longDesc = "x".repeat(300);
        mvc.perform(post("/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"description\":\"" + longDesc + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testGetBoard_ok_http() throws Exception {
        when(service.getEl(1L, false)).thenReturn(new BoardModel());
        when(mapper.toResource(any())).thenReturn(new BoardResource());

        mvc.perform(get("/boards/1?isArchived=false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testTypeMismatch_400_onQueryParam() throws Exception {
        mvc.perform(get("/boards/1?isArchived=abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testArchiveBoard_ok_http() throws Exception {
        when(service.archiveEl(5L)).thenReturn(new BoardModel());
        when(mapper.toResource(any())).thenReturn(new BoardResource());

        mvc.perform(delete("/boards/archive/5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testArchiveBoardList_ok_http() throws Exception {
        when(service.archiveList(any())).thenReturn(3);

        mvc.perform(post("/boards/archive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idList\":[1,2,2]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testArchiveAll_ok_http() throws Exception {
        when(service.archiveList()).thenReturn(10);

        mvc.perform(post("/boards/archive-all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testWrongMethod_405_onArchiveCollection() throws Exception {
        mvc.perform(get("/boards/archive"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"));
    }

    @Test
    void testRestoreBoard_ok_http_defaultTrue() throws Exception {
        when(service.restoreEl(7L, true)).thenReturn(new BoardModel());
        when(mapper.toResource(any())).thenReturn(new BoardResource());

        mvc.perform(put("/boards/restore/7"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testRestoreBoardList_ok_http_falseFlag() throws Exception {
        when(service.restoreList(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(2);

        mvc.perform(post("/boards/restore?isWithTasks=false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idList\":[1,2,2]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testRestoreAll_ok_http_trueFlag() throws Exception {
        when(service.restoreList(true)).thenReturn(4);

        mvc.perform(post("/boards/restore-all?isWithTasks=true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testUpdateBoard_400_onValidation() throws Exception {
        String longDesc = "x".repeat(300);
        mvc.perform(put("/boards/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"" + longDesc + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteBoard_ok_204_http() throws Exception {
        mvc.perform(delete("/boards/delete/11"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteBoardList_ok_http() throws Exception {
        when(service.deleteList(any())).thenReturn(5);

        mvc.perform(post("/boards/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idList\":[9,9,10]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testDeleteAll_ok_http() throws Exception {
        when(service.clear()).thenReturn(8);

        mvc.perform(post("/boards/delete-all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testNoHandlerFound_404() throws Exception {
        mvc.perform(get("/boards/not/existing/path"))
                .andExpect(status().isNotFound());
    }
}
