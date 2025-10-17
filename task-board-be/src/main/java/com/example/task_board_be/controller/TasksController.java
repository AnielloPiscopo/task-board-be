package com.example.task_board_be.controller;

import com.example.task_board_be.enums.BulkOperation;
import com.example.task_board_be.mapping.assembler.TaskAssembler;
import com.example.task_board_be.mapping.mapper.TaskMapper;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.pojo.request.CreateTaskRequest;
import com.example.task_board_be.pojo.request.IdsRequest;
import com.example.task_board_be.pojo.request.UpdateTaskRequest;
import com.example.task_board_be.pojo.resource.BulkResource;
import com.example.task_board_be.pojo.resource.ErrorResource;
import com.example.task_board_be.pojo.resource.TaskResource;
import com.example.task_board_be.service.entity.TaskService;
import com.example.task_board_be.utils.LoggerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Tasks", description = "API per la gestione delle task")
@ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResource.class))),
})
@Validated
@RestController
@RequestMapping("tasks")
public class TasksController {
    private final TaskMapper mapper;
    private final TaskAssembler assembler;
    private final TaskService service;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public TasksController(TaskMapper mapper, TaskAssembler assembler, TaskService service) {
        this.mapper = mapper;
        this.assembler = assembler;
        this.service = service;
    }

    @Operation(
            summary = "Lista task di una board",
            description = "Ritorna le task filtrate per board, stato di archiviazione e (opz.) nome. Supporta paginazione/ordinamento."
    )
    @Parameter(name = "boardId", description = "Id della board", required = true)
    @Parameter(name = "isArchived", description = "false = attive (default), true = archiviate",
            schema = @Schema(type = "boolean", defaultValue = "false"))
    @Parameter(name = "nameFilter", description = "Filtro su name (contains, case-insensitive)", required = false)
    @ApiResponse(responseCode = "200", description = "Task ottenute con successo",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TaskResource.class))))
    @GetMapping(produces = "application/json")
    public ResponseEntity<Page<TaskResource>> getTaskPage(
            @RequestParam(name = "boardId") Long boardId,
            @RequestParam(name = "isArchived", defaultValue = "false", required = false) boolean isArchived,
            @RequestParam(name = "nameFilter", required = false) String nameFilter,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        logger.info("{} - [PARAMS: boardId->{} ; isArchived->{} ; nameFilter->{} ; pageable->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), boardId, isArchived, nameFilter, pageable);

        Page<TaskModel> page = service.getPage(nameFilter, boardId, isArchived, pageable);
        Page<TaskResource> resourcePage = page.map(mapper::toResource);

        logger.info("{} - [RESULT: pageSize->{} ; total->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false),
                resourcePage.getNumberOfElements(), resourcePage.getTotalElements());

        return ResponseEntity.ok(resourcePage);
    }

    @Operation(summary = "Ottieni task", description = "Ottieni una task per id, filtrando per stato di archiviazione.")
    @Parameter(name = "isArchived", description = "false = attiva (default), true = archiviata",
            schema = @Schema(type = "boolean", defaultValue = "false"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task ottenuta con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskResource.class))),
            @ApiResponse(responseCode = "404", description = "Task non trovata",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @GetMapping(value = "/{id:\\d+}", produces = "application/json")
    public ResponseEntity<TaskResource> getTask(@PathVariable Long id,
                                                @RequestParam(name = "isArchived", defaultValue = "false", required = false) boolean isArchived) {
        logger.info("{} - [PARAMS: id->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id);

        TaskModel model = service.getEl(id, isArchived);
        TaskResource resource = mapper.toResource(model);

        logger.info("{} - [RESULT: response->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), resource);
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Crea una nuova task", description = "Crea una task e restituisce la task creata con ID generato.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task creata con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskResource.class))),
            @ApiResponse(responseCode = "400", description = "Errore nella richiesta o nella validazione",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<TaskResource> createTask(@RequestBody @Valid CreateTaskRequest request) {
        logger.info("{} - [PARAMS: request->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), request);

        TaskModel model = assembler.assembleModel(request);
        model = service.create(model);
        TaskResource response = mapper.toResource(model);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();

        logger.info("{} - [RESULT: response->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), response);
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Aggiorna task", description = "Aggiorna nome, descrizione, stato e icona della task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task aggiornata con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskResource.class))),
            @ApiResponse(responseCode = "404", description = "Task non trovata",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "400", description = "Errore nella richiesta o nella validazione",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TaskResource> updateTask(@PathVariable Long id,
                                                   @RequestBody @Valid UpdateTaskRequest request) {
        logger.info("{} - [PARAMS: id->{} ; request->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id, request);

        TaskModel model = assembler.assembleModel(request, id);
        model = service.update(model);
        TaskResource resource = mapper.toResource(model);

        logger.info("{} - [RESULT: result -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), resource);
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Archivia (soft delete) una task", description = "Archivia la task indicata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivazione avvenuta con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskResource.class))),
            @ApiResponse(responseCode = "404", description = "Task non trovata",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @DeleteMapping(value = "/archive/{id:\\d+}", produces = "application/json")
    public ResponseEntity<TaskResource> archiveTask(@PathVariable Long id) {
        logger.info("{} - [PARAMS: id->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id);

        TaskModel model = service.archiveEl(id);
        TaskResource resource = mapper.toResource(model);

        logger.info("{} - [RESULT: response->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), resource);
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Archivia (soft delete) più task", description = "Archivia le task passate per id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivazioni avvenute con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BulkResource.class))),
            @ApiResponse(responseCode = "400", description = "Lista ID vuota o non valida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PostMapping(value = "/archive", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BulkResource> archiveTaskList(@RequestBody @Valid IdsRequest req) {
        logger.info("{} - [PARAMS: request->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), req);

        List<Long> idList = req.getIdList().stream().distinct().toList();

        int updatedRow = service.archiveList(idList);
        BulkResource result = new BulkResource(BulkOperation.ARCHIVE_LIST , updatedRow);

        logger.info("{} - [RESULT: response->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), result);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Ripristina una task", description = "Ripristina una task archiviata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ripristinazione avvenuta con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskResource.class))),
            @ApiResponse(responseCode = "404", description = "Task non trovata",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PutMapping(value = "/restore/{id:\\d+}", produces = "application/json")
    public ResponseEntity<TaskResource> restoreTask(@PathVariable Long id) {
        logger.info("{} - [PARAMS: id->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id);

        TaskModel model = service.restoreEl(id);

        TaskResource resource = mapper.toResource(model);

        logger.info("{} - [RESULT: response->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), resource);
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Ripristina più task", description = "Ripristina una lista di task archiviate.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ripristinazioni avvenute con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BulkResource.class))),
            @ApiResponse(responseCode = "400", description = "Lista ID vuota o non valida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PostMapping(value = "/restore", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BulkResource> restoreTaskList(@RequestBody @Valid IdsRequest req) {
        logger.info("{} - [PARAMS: request->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), req);

        List<Long> idList = req.getIdList().stream().distinct().toList();

        int updatedRow = service.restoreList(idList);
        BulkResource result = new BulkResource(BulkOperation.RESTORE_LIST , updatedRow);

        logger.info("{} - [RESULT: response->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), result);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Elimina (hard delete) una task", description = "Cancella definitivamente una task.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Eliminazione avvenuta con successo"),
            @ApiResponse(responseCode = "404", description = "Task non trovata",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @DeleteMapping("/delete/{id:\\d+}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        logger.info("{} - [PARAMS: id->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id);

        service.delete(id);

        logger.info(LoggerUtils.getStandardLoggerMsg("end", false));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Elimina (hard delete) più task", description = "Cancella definitivamente le task specificate.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eliminazioni avvenute con successo",
            content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BulkResource.class))),
            @ApiResponse(responseCode = "400", description = "Lista ID vuota o non valida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PostMapping(value = "/delete",produces="application/json", consumes = "application/json")
    public ResponseEntity<BulkResource> deleteTaskList(@RequestBody @Valid IdsRequest req) {
        logger.info("{} - [PARAMS: request->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), req);

        List<Long> idList = req.getIdList().stream().distinct().toList();

        int updatedRow = service.deleteList(idList);
        BulkResource result = new BulkResource(BulkOperation.DELETE_LIST , updatedRow);

        logger.info("{} - [RESULT: response->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), result);
        return ResponseEntity.ok(result);
    }
}
