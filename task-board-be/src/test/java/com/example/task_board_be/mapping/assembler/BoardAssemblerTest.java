package com.example.task_board_be.mapping.assembler;

import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.request.CreateBoardRequest;
import com.example.task_board_be.pojo.request.UpdateBoardRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardAssemblerTest {

    private final BoardAssembler assembler = new BoardAssembler();

    @Test
    void testAssembleCreate_passThroughEvenIfBlank() {
        CreateBoardRequest req = new CreateBoardRequest();
        req.setName("   ");
        req.setDescription("desc");

        BoardModel out = assembler.assembleModel(req);

        assertEquals("   ", out.getName());
        assertEquals("desc", out.getDescription());
    }

    @Test
    void testAssembleCreate_usesProvidedName() {
        CreateBoardRequest req = new CreateBoardRequest();
        req.setName("Roadmap 2025");
        req.setDescription("All features");

        BoardModel out = assembler.assembleModel(req);

        assertEquals("Roadmap 2025", out.getName());
        assertEquals("All features", out.getDescription());
    }

    @Test
    void testAssembleUpdate_setsAllFields() {
        UpdateBoardRequest req = new UpdateBoardRequest();
        req.setName("Renamed");
        req.setDescription("Updated");

        BoardModel out = assembler.assembleModel(req, 42L);

        assertEquals(42L, out.getId());
        assertEquals("Renamed", out.getName());
        assertEquals("Updated", out.getDescription());
    }
}
