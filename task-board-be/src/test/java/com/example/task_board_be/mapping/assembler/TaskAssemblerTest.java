package com.example.task_board_be.mapping.assembler;

import com.example.task_board_be.enums.task.TaskIcon;
import com.example.task_board_be.enums.task.TaskStatus;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.pojo.request.CreateTaskRequest;
import com.example.task_board_be.pojo.request.UpdateTaskRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskAssemblerTest {

    private final TaskAssembler assembler = new TaskAssembler();

    @Test
    void testAssembleCreate_passThroughBlankName_statusIconDefaultedByParser() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setName("   ");
        req.setDescription("desc");
        req.setStatus(null);
        req.setIcon("");

        TaskModel out = assembler.assembleModel(req);

        assertEquals("   ", out.getName());
        assertEquals("desc", out.getDescription());
        assertEquals(TaskStatus.NONE, out.getStatus());
        assertEquals(TaskIcon.NONE, out.getIcon());
    }

    @Test
    void testAssembleCreate_trimsAndParsesCaseInsensitive() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setName("Task A");
        req.setDescription("x");
        req.setStatus("  in_progress ");
        req.setIcon("  bug  ");

        TaskModel out = assembler.assembleModel(req);

        assertEquals("Task A", out.getName());
        assertEquals(TaskStatus.IN_PROGRESS, out.getStatus());
        assertEquals(TaskIcon.BUG, out.getIcon());
    }

    @Test
    void testAssembleCreate_invalidEnumsFallbackToNone() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setName("Task B");
        req.setDescription("x");
        req.setStatus("NOT_A_STATUS");
        req.setIcon("NOT_AN_ICON");

        TaskModel out = assembler.assembleModel(req);

        assertEquals(TaskStatus.NONE, out.getStatus());
        assertEquals(TaskIcon.NONE, out.getIcon());
    }

    @Test
    void testAssembleUpdate_setsIdAndFields_validEnums() {
        UpdateTaskRequest req = new UpdateTaskRequest();
        req.setName("Rename");
        req.setDescription("New desc");
        req.setStatus("NONE");
        req.setIcon("NONE");

        TaskModel out = assembler.assembleModel(req, 99L);

        assertEquals(99L, out.getId());
        assertEquals("Rename", out.getName());
        assertEquals("New desc", out.getDescription());
        assertEquals(TaskStatus.NONE, out.getStatus());
        assertEquals(TaskIcon.NONE, out.getIcon());
    }

    @Test
    void testAssembleUpdate_invalidEnumsFallbackToNone() {
        UpdateTaskRequest req = new UpdateTaskRequest();
        req.setName(null);
        req.setDescription(null);
        req.setStatus("???");
        req.setIcon("???");

        TaskModel out = assembler.assembleModel(req, 7L);

        assertEquals(7L, out.getId());
        assertEquals(TaskStatus.NONE, out.getStatus());
        assertEquals(TaskIcon.NONE, out.getIcon());
    }
}
