package com.example.task_board_be.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OpenApiConfig.class)
class OpenApiConfigTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    void testCustomOpenAPIBean_presentAndPopulated() {
        assertNotNull(openAPI, "OpenAPI bean should be present");
        Info info = openAPI.getInfo();
        assertNotNull(info, "OpenAPI info should be present");
        assertEquals("TaskBoard API", info.getTitle());
        assertEquals("1.0", info.getVersion());
        assertEquals("API per la gestione di board e task", info.getDescription());
    }
}