package com.example.task_board_be.mapping.mapper;

import com.example.task_board_be.enums.task.TaskIcon;
import com.example.task_board_be.enums.task.TaskStatus;
import com.example.task_board_be.pojo.entity.Task;
import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.pojo.resource.TaskResource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperTest {

    private final TaskMapper mapper = new TaskMapper();

    @Test
    void testToEntity_mapsAllFields() {
        TaskModel m = new TaskModel();
        m.setId(10L);
        m.setName("T1");
        m.setDescription("D1");
        m.setStatus(TaskStatus.NONE);
        m.setIcon(TaskIcon.NONE);

        Task e = mapper.toEntity(m);

        assertEquals(10L, e.getId());
        assertEquals("T1", e.getName());
        assertEquals("D1", e.getDescription());
        assertEquals(TaskStatus.NONE, e.getStatus());
        assertEquals(TaskIcon.NONE, e.getIcon());
    }

    @Test
    void testToModel_mapsAllFields() {
        Task e = new Task();
        e.setId(11L);
        e.setName("T2");
        e.setDescription("D2");
        e.setStatus(TaskStatus.NONE);
        e.setIcon(TaskIcon.NONE);

        TaskModel m = mapper.toModel(e);

        assertEquals(11L, m.getId());
        assertEquals("T2", m.getName());
        assertEquals("D2", m.getDescription());
        assertEquals(TaskStatus.NONE, m.getStatus());
        assertEquals(TaskIcon.NONE, m.getIcon());
    }

    @Test
    void testToResource_mapsFields_withoutBoard() {
        TaskModel m = new TaskModel();
        m.setId(12L);
        m.setName("T3");
        m.setDescription("D3");
        m.setStatus(TaskStatus.NONE);
        m.setIcon(TaskIcon.NONE);

        TaskResource r = mapper.toResource(m);

        assertEquals(12L, r.getId());
        assertEquals("T3", r.getName());
        assertEquals("D3", r.getDescription());
        assertEquals(TaskStatus.NONE, r.getStatus());
        assertEquals(TaskIcon.NONE, r.getIcon());
        assertNull(r.getBoardId());
    }

    @Test
    void testToResource_setsBoardId_whenBoardModelPresent() {
        TaskModel m = new TaskModel();
        m.setId(13L);
        BoardModel bm = new BoardModel();
        bm.setId(99L);
        m.setBoardModel(bm);

        TaskResource r = mapper.toResource(m);

        assertEquals(13L, r.getId());
        assertEquals(99L, r.getBoardId());
    }

    @Test
    void testToEntityList_nullAndEmpty() {
        List<Task> outNull = mapper.toEntityList(null);
        List<Task> outEmpty = mapper.toEntityList(List.of());
        assertNotNull(outNull);
        assertTrue(outNull.isEmpty());
        assertNotNull(outEmpty);
        assertTrue(outEmpty.isEmpty());
    }

    @Test
    void testToEntityList_filtersNulls_andMapsValues() {
        TaskModel m1 = new TaskModel();
        m1.setId(1L);
        TaskModel m2 = new TaskModel();
        m2.setId(2L);

        List<Task> out = mapper.toEntityList(new ArrayList<>(Arrays.asList(m1, null, m2)));

        assertEquals(2, out.size());
        List<Long> ids = out.stream().map(Task::getId).toList();
        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(2L));
    }

    @Test
    void testToModelList_nullAndEmpty() {
        List<TaskModel> outNull = mapper.toModelList(null);
        List<TaskModel> outEmpty = mapper.toModelList(List.of());
        assertNotNull(outNull);
        assertTrue(outNull.isEmpty());
        assertNotNull(outEmpty);
        assertTrue(outEmpty.isEmpty());
    }

    @Test
    void testToModelList_filtersNulls_andMapsValues() {
        Task e1 = new Task();
        e1.setId(3L);
        Task e2 = new Task();
        e2.setId(4L);

        List<TaskModel> out = mapper.toModelList(new ArrayList<>(Arrays.asList(e1, null, e2)));

        assertEquals(2, out.size());
        List<Long> ids = out.stream().map(TaskModel::getId).toList();
        assertTrue(ids.contains(3L));
        assertTrue(ids.contains(4L));
    }

    @Test
    void testToResourceList_nullAndEmpty() {
        List<TaskResource> outNull = mapper.toResourceList(null);
        List<TaskResource> outEmpty = mapper.toResourceList(List.of());
        assertNotNull(outNull);
        assertTrue(outNull.isEmpty());
        assertNotNull(outEmpty);
        assertTrue(outEmpty.isEmpty());
    }

    @Test
    void testToResourceList_filtersNulls_andMapsValues() {
        TaskModel m1 = new TaskModel();
        m1.setId(5L);
        TaskModel m2 = new TaskModel();
        m2.setId(6L);

        List<TaskResource> out = mapper.toResourceList(new ArrayList<>(Arrays.asList(m1, null, m2)));

        assertEquals(2, out.size());
        List<Long> ids = out.stream().map(TaskResource::getId).toList();
        assertTrue(ids.contains(5L));
        assertTrue(ids.contains(6L));
    }
}
