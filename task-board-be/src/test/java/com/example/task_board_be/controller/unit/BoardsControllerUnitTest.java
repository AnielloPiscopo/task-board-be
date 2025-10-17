package com.example.task_board_be.controller.unit;

import com.example.task_board_be.controller.BoardsController;
import com.example.task_board_be.enums.BulkOperation;
import com.example.task_board_be.exception.custom.NotFoundException;
import com.example.task_board_be.mapping.assembler.BoardAssembler;
import com.example.task_board_be.mapping.mapper.BoardMapper;
import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.request.CreateBoardRequest;
import com.example.task_board_be.pojo.request.IdsRequest;
import com.example.task_board_be.pojo.request.UpdateBoardRequest;
import com.example.task_board_be.pojo.resource.BoardResource;
import com.example.task_board_be.pojo.resource.BulkResource;
import com.example.task_board_be.pojo.resource.CascadeBulkResource;
import com.example.task_board_be.service.entity.BoardService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardsControllerUnitTest {

    @InjectMocks
    private BoardsController controller;
    @Mock
    private BoardMapper mapper;
    @Mock
    private BoardAssembler assembler;
    @Mock
    private BoardService service;

    @Test
    void getBoardPage_mapsModelsToResources() {
        var m = org.mockito.Mockito.mock(com.example.task_board_be.pojo.model.BoardModel.class);
        var r = org.mockito.Mockito.mock(com.example.task_board_be.pojo.resource.BoardResource.class);
        var p = org.springframework.data.domain.PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("createdAt").descending());
        var page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(m), p, 1);

        org.mockito.Mockito.when(service.getPage(null, false, p)).thenReturn(page);
        org.mockito.Mockito.when(mapper.toResource(m)).thenReturn(r);

        var resp = controller.getBoardPage(false, null, p);

        org.junit.jupiter.api.Assertions.assertEquals(200, resp.getStatusCodeValue());
        org.junit.jupiter.api.Assertions.assertEquals(1, resp.getBody().getTotalElements());
    }

    @Test
    void updateBoard_mapsAssemblerServiceAndMapper() {
        var req = org.mockito.Mockito.mock(com.example.task_board_be.pojo.request.UpdateBoardRequest.class);
        var model = org.mockito.Mockito.mock(com.example.task_board_be.pojo.model.BoardModel.class);
        var resource = org.mockito.Mockito.mock(com.example.task_board_be.pojo.resource.BoardResource.class);

        org.mockito.Mockito.when(assembler.assembleModel(req, 9L)).thenReturn(model);
        org.mockito.Mockito.when(service.update(model)).thenReturn(model);
        org.mockito.Mockito.when(mapper.toResource(model)).thenReturn(resource);

        var resp = controller.updateBoard(9L, req);

        org.junit.jupiter.api.Assertions.assertEquals(200, resp.getStatusCodeValue());
        org.junit.jupiter.api.Assertions.assertSame(resource, resp.getBody());
    }


    @Test
    void testGetBoardPage_ok() {
        BoardModel m = mock(BoardModel.class);
        BoardResource r = mock(BoardResource.class);
        Pageable p = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BoardModel> page = new PageImpl<>(List.of(m), p, 1);

        when(service.getPage(null, false, p)).thenReturn(page);
        when(mapper.toResource(m)).thenReturn(r);

        var resp = controller.getBoardPage(false, null, p);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(1, resp.getBody().getTotalElements());
        verify(mapper).toResource(m);
    }

    @Test
    void testGetBoard_ok() {
        BoardModel m = mock(BoardModel.class);
        BoardResource r = mock(BoardResource.class);
        when(service.getEl(10L, false)).thenReturn(m);
        when(mapper.toResource(m)).thenReturn(r);

        var resp = controller.getBoard(10L, false);

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(r, resp.getBody());
    }

    @Test
    void testGetBoard_notFound_bubbles() {
        when(service.getEl(999L, false)).thenThrow(new NotFoundException("Board", 999L));
        assertThrows(NotFoundException.class, () -> controller.getBoard(999L, false));
    }

    @Test
    void testCreateBoard_created201_andLocationHeader() {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/boards");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        CreateBoardRequest request = mock(CreateBoardRequest.class);
        BoardModel m = mock(BoardModel.class);
        BoardResource r = mock(BoardResource.class);

        when(assembler.assembleModel(request)).thenReturn(m);
        when(service.create(m)).thenReturn(m);
        when(mapper.toResource(m)).thenReturn(r);
        when(r.getId()).thenReturn(123L);

        var resp = controller.createBoard(request);

        assertEquals(201, resp.getStatusCodeValue());
        assertSame(r, resp.getBody());
        URI loc = resp.getHeaders().getLocation();
        assertNotNull(loc);
        assertTrue(loc.getPath().endsWith("/boards/123"));
    }

    @Test
    void testUpdateBoard_ok() {
        UpdateBoardRequest reqBody = mock(UpdateBoardRequest.class);
        BoardModel m = mock(BoardModel.class);
        BoardResource r = mock(BoardResource.class);

        when(assembler.assembleModel(eq(reqBody), eq(7L))).thenReturn(m);
        when(service.update(m)).thenReturn(m);
        when(mapper.toResource(m)).thenReturn(r);

        var resp = controller.updateBoard(7L, reqBody);

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(r, resp.getBody());
    }

    @Test
    void testArchiveBoard_ok() {
        BoardModel m = mock(BoardModel.class);
        BoardResource r = mock(BoardResource.class);
        when(service.archiveEl(5L)).thenReturn(m);
        when(mapper.toResource(m)).thenReturn(r);

        var resp = controller.archiveBoard(5L);

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(r, resp.getBody());
    }

    @Test
    void testArchiveBoardList_ok() {
        IdsRequest req = mock(IdsRequest.class);
        when(req.getIdList()).thenReturn(List.of(1L, 2L, 2L));
        when(service.archiveList(List.of(1L, 2L))).thenReturn(2);

        var resp = controller.archiveBoardList(req);

        assertEquals(200, resp.getStatusCodeValue());
        BulkResource b = resp.getBody();
        assertNotNull(b);
        assertEquals(BulkOperation.ARCHIVE_LIST, b.getOperation());
        assertEquals(2, b.getUpdatedRow());
    }

    @Test
    void testArchiveAll_ok() {
        when(service.archiveList()).thenReturn(7);
        var resp = controller.archiveBoardList();
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(7, resp.getBody().getUpdatedRow());
        assertEquals(BulkOperation.ARCHIVE_ALL, resp.getBody().getOperation());
    }

    @Test
    void testRestoreBoard_ok() {
        BoardModel m = mock(BoardModel.class);
        BoardResource r = mock(BoardResource.class);
        when(service.restoreEl(3L, true)).thenReturn(m);
        when(mapper.toResource(m)).thenReturn(r);

        var resp = controller.restoreBoard(3L, true);

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(r, resp.getBody());
    }

    @Test
    void testRestoreBoardList_ok() {
        IdsRequest req = mock(IdsRequest.class);
        when(req.getIdList()).thenReturn(List.of(1L, 1L, 2L));
        when(service.restoreList(List.of(1L, 2L), false)).thenReturn(2);

        var resp = controller.restoreBoardList(req, false);

        assertEquals(200, resp.getStatusCodeValue());
        CascadeBulkResource c = resp.getBody();
        assertNotNull(c);
        assertEquals(BulkOperation.RESTORE_LIST, c.getOperation());
        assertEquals(2, c.getUpdatedRow());
        assertFalse(c.getCascade());
    }

    @Test
    void testRestoreAll_ok() {
        when(service.restoreList(true)).thenReturn(4);

        var resp = controller.restoreBoardList(true);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(4, resp.getBody().getUpdatedRow());
        assertEquals(BulkOperation.RESTORE_ALL, resp.getBody().getOperation());
        assertTrue(resp.getBody().getCascade());
    }

    @Test
    void testDeleteBoard_noContent() {
        var resp = controller.deleteBoard(11L);
        assertEquals(204, resp.getStatusCodeValue());
        verify(service).delete(11L);
    }

    @Test
    void testDeleteBoardList_ok() {
        IdsRequest req = mock(IdsRequest.class);
        when(req.getIdList()).thenReturn(List.of(9L, 9L, 10L));
        when(service.deleteList(List.of(9L, 10L))).thenReturn(2);

        var resp = controller.deleteBoardList(req);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(2, resp.getBody().getUpdatedRow());
        assertEquals(BulkOperation.DELETE_LIST, resp.getBody().getOperation());
    }

    @Test
    void testDeleteAll_ok() {
        when(service.clear()).thenReturn(12);
        var resp = controller.deleteAll();
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(12, resp.getBody().getUpdatedRow());
        assertEquals(BulkOperation.DELETE_ALL, resp.getBody().getOperation());
    }
}
