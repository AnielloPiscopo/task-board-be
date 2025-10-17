package com.example.task_board_be.mapping.mapper;

import com.example.task_board_be.pojo.entity.Board;
import com.example.task_board_be.pojo.entity.Task;
import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.pojo.resource.BoardResource;
import com.example.task_board_be.pojo.resource.TaskResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardMapperTest {

    private TaskMapper taskMapper;
    private BoardMapper boardMapper;

    @BeforeEach
    void setup() {
        taskMapper = new TaskMapper();
        boardMapper = new BoardMapper(taskMapper);
    }

    @Test
    void testToEntity_mapsFieldsAndTaskList() {
        TaskModel tm1 = new TaskModel();
        tm1.setId(1L);
        tm1.setName("T1");

        BoardModel bm = new BoardModel();
        bm.setId(10L);
        bm.setName("B");
        bm.setDescription("D");
        bm.setTaskModelList(new ArrayList<>(Arrays.asList(tm1, null)));

        Board e = boardMapper.toEntity(bm);

        assertEquals(10L, e.getId());
        assertEquals("B", e.getName());
        assertEquals("D", e.getDescription());
        assertEquals(1, e.getTaskList().size());
        assertEquals("T1", e.getTaskList().get(0).getName());
    }

    @Test
    void testToModel_mapsSimpleFields() {
        Board b = new Board();
        b.setId(5L);
        b.setName("X");
        b.setDescription("Y");

        BoardModel m = boardMapper.toModel(b);

        assertEquals(5L, m.getId());
        assertEquals("X", m.getName());
        assertEquals("Y", m.getDescription());
    }

    @Test
    void testToResource_mapsTasksToResources() {
        TaskModel tm = new TaskModel();
        tm.setId(2L);
        tm.setName("T");

        BoardModel bm = new BoardModel();
        bm.setId(20L);
        bm.setName("RB");
        bm.setDescription("RD");
        bm.setTaskModelList(new ArrayList<>(Arrays.asList(tm, null)));

        BoardResource r = boardMapper.toResource(bm);

        assertEquals(20L, r.getId());
        assertEquals("RB", r.getName());
        assertEquals("RD", r.getDescription());
        assertEquals(1, r.getTaskResourceList().size());
        assertEquals("T", r.getTaskResourceList().get(0).getName());
    }

    @Test
    void testToEntityList_nullEmptyAndValues() {
        assertTrue(boardMapper.toEntityList(null).isEmpty());
        assertTrue(boardMapper.toEntityList(List.of()).isEmpty());

        BoardModel bm1 = new BoardModel();
        bm1.setName("A");
        BoardModel bm2 = new BoardModel();
        bm2.setName("B");

        List<Board> out = boardMapper.toEntityList(new ArrayList<>(Arrays.asList(bm1, null, bm2)));
        assertEquals(2, out.size());
        assertEquals("A", out.get(0).getName());
        assertEquals("B", out.get(1).getName());
    }

    @Test
    void testToModelList_nullEmptyAndValues() {
        assertTrue(boardMapper.toModelList(null).isEmpty());
        assertTrue(boardMapper.toModelList(List.of()).isEmpty());

        Board b1 = new Board();
        b1.setName("A");
        Board b2 = new Board();
        b2.setName("B");

        List<BoardModel> out = boardMapper.toModelList(new ArrayList<>(Arrays.asList(b1, null, b2)));
        assertEquals(2, out.size());
        assertEquals("A", out.get(0).getName());
        assertEquals("B", out.get(1).getName());
    }

    @Test
    void testToResourceList_nullEmptyAndValues() {
        assertTrue(boardMapper.toResourceList(null).isEmpty());
        assertTrue(boardMapper.toResourceList(List.of()).isEmpty());

        BoardModel bm1 = new BoardModel();
        bm1.setName("A");
        BoardModel bm2 = new BoardModel();
        bm2.setName("B");

        List<BoardResource> out = boardMapper.toResourceList(new ArrayList<>(Arrays.asList(bm1, null, bm2)));
        assertEquals(2, out.size());
        assertEquals("A", out.get(0).getName());
        assertEquals("B", out.get(1).getName());
    }

    @Test
void testToModelWithCascade_mapsTasksFromEntity() {
    Task t1 = new Task();
    t1.setId(100L);
    t1.setName("TA");
    Task t2 = new Task();
    t2.setId(101L);
    t2.setName("TB");

    Board b = new Board();
    b.setId(9L);
    b.setName("BC");
    b.setDescription("DC");
    b.addTaskList(new ArrayList<>(Arrays.asList(t1, t2)));

    BoardModel m = boardMapper.toModelWithCascade(b);

    assertEquals(9L, m.getId());
    assertEquals("BC", m.getName());
    assertEquals("DC", m.getDescription());
    assertEquals(2, m.getTaskModelList().size());
    List<String> names = m.getTaskModelList().stream().map(TaskModel::getName).toList();
    assertTrue(names.contains("TA"));
    assertTrue(names.contains("TB"));
}


    @Test
    void testToResource_taskResourceMappingFields() {
        TaskModel tm = new TaskModel();
        tm.setId(3L);
        tm.setName("T");
        tm.setDescription("D");

        BoardModel bm = new BoardModel();
        bm.setId(30L);
        bm.setName("B");
        bm.setDescription("BD");
        bm.setTaskModelList(List.of(tm));

        BoardResource r = boardMapper.toResource(bm);
        TaskResource tr = r.getTaskResourceList().get(0);

        assertEquals(3L, tr.getId());
        assertEquals("T", tr.getName());
        assertEquals("D", tr.getDescription());
    }
}
