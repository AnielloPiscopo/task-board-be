package com.example.task_board_be.controller;

import com.example.task_board_be.enums.BulkOperation;
import com.example.task_board_be.mapping.assembler.BoardAssembler;
import com.example.task_board_be.mapping.mapper.BoardMapper;
import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.request.CreateBoardRequest;
import com.example.task_board_be.pojo.request.IdsRequest;
import com.example.task_board_be.pojo.request.UpdateBoardRequest;
import com.example.task_board_be.pojo.resource.BoardResource;
import com.example.task_board_be.pojo.resource.BulkResource;
import com.example.task_board_be.pojo.resource.CascadeBulkResource;
import com.example.task_board_be.pojo.resource.ErrorResource;
import com.example.task_board_be.service.entity.BoardService;
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

@Tag(name = "Boards", description = "API per la gestione delle board")
@ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResource.class))),
})
@Validated
@RestController
@RequestMapping("boards")
public class BoardsController {
    private final BoardMapper mapper;
    private final BoardAssembler assembler;
    private final BoardService service;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public BoardsController(BoardMapper mapper, BoardAssembler assembler, BoardService service) {
        this.mapper = mapper;
        this.assembler = assembler;
        this.service = service;
    }

    @Operation(summary = "Lista board", description = "Filtra per isArchived e per nome (nameFilter). Paginazione e ordinamento.")
    @Parameter(name = "isArchived", description = "false = attive (default), true = archiviate",
            schema = @Schema(type = "boolean", defaultValue = "false"))
    @Parameter(name = "nameFilter", description = "Filtro su name (contains, case-insensitive)", required = false)
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = BoardResource.class))))
    @GetMapping(produces = "application/json")
    public ResponseEntity<Page<BoardResource>> getBoardPage(
            @RequestParam(name = "isArchived", defaultValue = "false") boolean isArchived,
            @RequestParam(name = "nameFilter", required = false) String nameFilter,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        logger.info("{} - [PARAMS: isArchived->{} ; nameFilter->{} ; pageable->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), isArchived, nameFilter, pageable);

        Page<BoardModel> modelPage = service.getPage(nameFilter, isArchived, pageable);
        Page<BoardResource> resourcePage = modelPage.map(mapper::toResource);

        logger.info("{} - [RESULT: pageSize->{} ; total->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false),
                resourcePage.getNumberOfElements(), resourcePage.getTotalElements());
        return ResponseEntity.ok(resourcePage);
    }

    @Operation(summary = "Ottieni board", description = "Ottiene una board per id, filtrando per stato di archiviazione.")
    @Parameter(name = "isArchived", description = "false = attiva (default), true = archiviata",
            schema = @Schema(type = "boolean", defaultValue = "false"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board ottenuta con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BoardResource.class))),
            @ApiResponse(responseCode = "404", description = "Board non trovata",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @GetMapping(value = "/{id:\\d+}", produces = "application/json")
    public ResponseEntity<BoardResource> getBoard(@PathVariable Long id,
                                                  @RequestParam(required = false, defaultValue = "false", name = "isArchived") boolean isArchived) {
        logger.info("{} - [PARAMS: id->{} , isArchived->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id, isArchived);

        BoardModel model = service.getEl(id, isArchived);
        BoardResource resource = mapper.toResource(model);

        logger.info("{} - [RESULT: result -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), resource);
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Crea una nuova board", description = "Crea una board e restituisce la board creata con ID generato.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Board creata con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BoardResource.class))),
            @ApiResponse(responseCode = "400", description = "Errore nella richiesta o nella validazione",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<BoardResource> createBoard(@RequestBody @Valid CreateBoardRequest request) {
        logger.info("{} - [PARAMS: request->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), request);

        BoardModel model = assembler.assembleModel(request);
        model = service.create(model);
        BoardResource response = mapper.toResource(model);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();

        logger.info("{} - [RESULT: response->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), response);
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Aggiorna board", description = "Aggiorna nome e descrizione della board")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board aggiornata con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BoardResource.class))),
            @ApiResponse(responseCode = "404", description = "Board non trovata",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "400", description = "Errore nella richiesta o nella validazione",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BoardResource> updateBoard(@PathVariable Long id,
                                                     @RequestBody @Valid UpdateBoardRequest request) {
        logger.info("{} - [PARAMS: id->{} ; request->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id, request);

        BoardModel model = assembler.assembleModel(request, id);
        model = service.update(model);
        BoardResource resource = mapper.toResource(model);

        logger.info("{} - [RESULT: result -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), resource);
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Archivia (soft delete) una board", description = "Archivia la board indicata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivazione avvenuta con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BoardResource.class))),
            @ApiResponse(responseCode = "404", description = "Board non trovata",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @DeleteMapping(value = "/archive/{id:\\d+}", produces = "application/json")
    public ResponseEntity<BoardResource> archiveBoard(@PathVariable Long id) {
        logger.info("{} - [PARAMS: id->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id);

        BoardModel model = service.archiveEl(id);

        BoardResource resource = mapper.toResource(model);
        logger.info("{} - [RESULT: result -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), resource);
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Archivia (soft delete) più board", description = "Archivia le board passate per id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivazioni avvenute con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BulkResource.class))),
            @ApiResponse(responseCode = "400", description = "Lista ID vuota o non valida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PostMapping(value = "/archive", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BulkResource> archiveBoardList(@RequestBody @Valid IdsRequest req) {
        logger.info("{} - [PARAMS: request->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), req);

        List<Long> idList = req.getIdList().stream().distinct().toList();

        int updatedRow = service.archiveList(idList);

        BulkResource result = new BulkResource(BulkOperation.ARCHIVE_LIST, updatedRow);

        logger.info("{} - [RESULT: result -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), result);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Archivia (soft delete) più board", description = "Archivia le board passate per id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivazioni avvenute con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BulkResource.class))),
            @ApiResponse(responseCode = "400", description = "Lista ID vuota o non valida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PostMapping(value = "/archive-all", produces = "application/json")
    public ResponseEntity<BulkResource> archiveBoardList() {
        logger.info(LoggerUtils.getStandardLoggerMsg("start", false));

        int updatedRow = service.archiveList();

        BulkResource result = new BulkResource(BulkOperation.ARCHIVE_ALL, updatedRow);

        logger.info("{} - [RESULT: result -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), result);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Ripristina una board", description = "Ripristina una board archiviata; opzionalmente ripristina anche le task figlie.")
    @Parameter(name = "isWithTasks", description = "false = senza task, true = con task (default)",
            schema = @Schema(type = "boolean", defaultValue = "true"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ripristinazione avvenuta con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BoardResource.class))),
            @ApiResponse(responseCode = "404", description = "Board non trovata",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PutMapping(value = "/restore/{id:\\d+}", produces = "application/json")
    public ResponseEntity<BoardResource> restoreBoard(@PathVariable Long id,
                                                      @RequestParam(defaultValue = "true", required = false, name = "isWithTasks") boolean isWithTasks
    ) {
        logger.info("{} - [PARAMS: id->{} ; isWithTasks -> {}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id, isWithTasks);

        BoardModel model = service.restoreEl(id, isWithTasks);

        BoardResource resource = mapper.toResource(model);
        logger.info("{} - [RESULT: result -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), resource);
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Ripristina più board", description = "Ripristina una lista di board; opzionalmente anche le task figlie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ripristinazioni avvenute con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CascadeBulkResource.class))),
            @ApiResponse(responseCode = "400", description = "Lista ID vuota o non valida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PostMapping(value = "/restore", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CascadeBulkResource> restoreBoardList(@RequestBody @Valid IdsRequest req,
                                                                @RequestParam(defaultValue = "true") boolean isWithTasks) {
        logger.info("{} - [PARAMS: request->{} ; isWithTasks -> {}]",
                LoggerUtils.getStandardLoggerMsg("start", false), req, isWithTasks);

        List<Long> idList = req.getIdList().stream().distinct().toList();

        int updatedRow = service.restoreList(idList, isWithTasks);

        CascadeBulkResource result = new CascadeBulkResource(BulkOperation.RESTORE_LIST, updatedRow, isWithTasks);

        logger.info("{} - [RESULT: result -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), result);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Ripristina tutte le board", description = "Ripristina tutte le board; opzionalmente anche le task figlie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ripristinazioni avvenute con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CascadeBulkResource.class))),
    })
    @PostMapping(value = "/restore-all", produces = "application/json")
    public ResponseEntity<CascadeBulkResource> restoreBoardList(@RequestParam(defaultValue = "true") boolean isWithTasks) {
        logger.info("{} - [PARAMS: isWithTasks->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), isWithTasks);

        int updatedRow = service.restoreList(isWithTasks);
        CascadeBulkResource result = new CascadeBulkResource(BulkOperation.RESTORE_ALL, updatedRow, isWithTasks);

        logger.info("{} - [RESULT: result -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), result);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Elimina (hard delete) una board", description = "Cancella definitivamente una board.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Eliminazione avvenuta con successo"),
            @ApiResponse(responseCode = "404", description = "Board non trovata",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @DeleteMapping("/delete/{id:\\d+}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        logger.info("{} - [PARAMS: id->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id);

        service.delete(id);

        logger.info(LoggerUtils.getStandardLoggerMsg("end", false));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Elimina (hard delete) più board", description = "Cancella definitivamente un elenco di board.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eliminazioni avvenute con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BulkResource.class))),
            @ApiResponse(responseCode = "400", description = "Lista ID vuota o non valida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class))),
    })
    @PostMapping(value = "/delete",produces="application/json", consumes = "application/json")
    public ResponseEntity<BulkResource> deleteBoardList(@RequestBody @Valid IdsRequest req) {
        logger.info("{} - [PARAMS: request->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), req);

        List<Long> idList = req.getIdList().stream().distinct().toList();

        int updatedRow = service.deleteList(idList);
        BulkResource result = new BulkResource(BulkOperation.DELETE_LIST, updatedRow);

        logger.info("{} - [RESULT: result -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), result);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Elimina (hard delete) tutte le board", description = "Cancella definitivamente tutte le board.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eliminazioni avvenute con successo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BulkResource.class))),
    })
    @PostMapping(value = "/delete-all" , produces="application/json")
    public ResponseEntity<BulkResource> deleteAll() {
        logger.info(LoggerUtils.getStandardLoggerMsg("start", false));

        int updatedRow = service.clear();
        BulkResource result = new BulkResource(BulkOperation.DELETE_ALL, updatedRow);

        logger.info("{} - [RESULT: result -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), result);
        return ResponseEntity.ok(result);
    }
}
