# TaskBoard – Backend

API **Spring Boot 3** per la gestione di **Board** e **Task** con supporto a **paginazione**, **archiviazione soft**, **operazioni bulk** e **OpenAPI/Swagger**.

## Tech Stack

**Linguaggio:** Java 17  
**Framework:** Spring Boot 3.5.0  
**Persistenza:** Spring Data JPA + PostgreSQL  
**Validazione:** Jakarta Validation  
**Doc API:** springdoc-openapi (Swagger UI)  
**Build:** Maven

## Requisiti

- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (o un’istanza compatibile)
- Porta libera: `8181` (configurabile)

## Avvio Locale

```bash
# 1) Configura il DB in application.properties (vedi sotto)
# 2) Avvia l’app
mvn spring-boot:run
```

Avvio jar:

```bash
mvn clean package
java -jar target/task-board-be-0.0.1-SNAPSHOT.jar
```

L’API espone di default: `http://localhost:8181`

## Environment / Configurazione

`src/main/resources/application.properties`:

```
spring.application.name=task-board-be

spring.datasource.url=jdbc:postgresql://localhost:5432/taskboard
spring.datasource.username=myuser
spring.datasource.password=mypassword

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.port=8181

springdoc.swagger-ui.path=/swagger-ui/
springdoc.cache.disabled=true

spring.mvc.throw-exception-if-no-handler-found=true
spring.web.resources.add-mappings=false

logging.level.org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping=TRACE
logging.level.org.springframework.web=DEBUG
management.endpoints.web.exposure.include=mappings
```

## Swagger / OpenAPI

- UI: `http://localhost:8181/swagger-ui/`
- Config: `OpenApiConfig` con titolo **TaskBoard API** e versione **1.0**

## Struttura del Progetto (principale)

```
src/main/java/com/example/task_board_be/
  configuration/
    OpenApiConfig.java
    SpringDataPageConfig.java
  controller/
    BoardsController.java
    TasksController.java
  mapping/
    assembler/ (BoardAssembler, TaskAssembler)
    mapper/    (BoardMapper, TaskMapper)
  pojo/
    model/     (BoardModel, TaskModel)
    request/   (CreateBoardRequest, UpdateBoardRequest, IdsRequest)
    resource/  (BoardResource, TaskResource, BulkResource, CascadeBulkResource, ErrorResource)
  service/
    entity/    (BoardService, TaskService)
  utils/
    LoggerUtils.java
resources/
  application.properties
pom.xml
```

## Paginazione e Ordinamento

- Basati su **Spring Data Pageable**
- Parametri comuni: `page` (0-based), `size`, `sort` (es. `createdAt,desc`)
- Default: `size=20`, `sort=createdAt,DESC` (via `@PageableDefault`)
- Config aggiuntiva: `SpringDataPageConfig` (`EnableSpringDataWebSupport` con `PageSerializationMode.VIA_DTO`)

## Convenzioni

- **Soft delete** = *archiviazione* (`/archive`)  
- **Restore** = ripristino elementi archiviati (`/restore`)  
- **Hard delete** = eliminazione definitiva (`/delete`)  
- Operazioni **bulk** accettano un body `IdsRequest` con `idList`

---

## Endpoints – Boards

Base path: `/boards`

### GET `/boards`
Lista paginata delle board con filtro:
- `isArchived` (default `false`)
- `nameFilter` (contains, case-insensitive)
- `page`, `size`, `sort`

**200 OK** → `Page<BoardResource>`

### GET `/boards/{id}`
Dettaglio board per `id` (opz. `isArchived`):
- `isArchived` (default `false`)

**200 OK** → `BoardResource`  
**404** → `ErrorResource`

### POST `/boards`
Crea board.

Body `CreateBoardRequest`:
```json
{ "name": "Roadmap Q4", "description": "..." }
```

**201 Created** → `BoardResource` (+ `Location` header)

### PUT `/boards/{id}`
Aggiorna board.

Body `UpdateBoardRequest`:
```json
{ "name": "Nuovo nome", "description": "..." }
```

**200 OK** → `BoardResource`

### DELETE `/boards/archive/{id}`
Archivia (soft delete) singola board.

**200 OK** → `BoardResource`

### POST `/boards/archive`
Archivia lista di board.

Body `IdsRequest`:
```json
{ "idList": [1,2,3] }
```

**200 OK** → `BulkResource { operation: "ARCHIVE_LIST", updatedRow: N }`

### POST `/boards/archive-all`
Archivia **tutte** le board.

**200 OK** → `BulkResource { operation: "ARCHIVE_ALL", updatedRow: N }`

### PUT `/boards/restore/{id}`
Ripristina una board.

Query:
- `isWithTasks` (default `true`) → ripristina anche le task figlie

**200 OK** → `BoardResource`

### POST `/boards/restore`
Ripristina più board.

Body `IdsRequest` + query `isWithTasks=true|false` (default `true`)

**200 OK** → `CascadeBulkResource { operation: "RESTORE_LIST", updatedRow: N, isWithTasks: true|false }`

### POST `/boards/restore-all`
Ripristina **tutte** le board.

Query `isWithTasks` (default `true`)

**200 OK** → `CascadeBulkResource { operation: "RESTORE_ALL", ... }`

### DELETE `/boards/delete/{id}`
Elimina definitivamente una board.

**204 No Content**

### POST `/boards/delete`
Elimina definitivamente più board.

Body `IdsRequest`:
```json
{ "idList": [1,2,3] }
```

**200 OK** → `BulkResource { operation: "DELETE_LIST", updatedRow: N }`

---

## Endpoints – Tasks

Base path: `/tasks`

### GET `/tasks`
Lista paginata delle task per **board**:

Query:
- `boardId` (**obbligatorio**)
- `isArchived` (default `false`)
- `nameFilter` (opzionale)
- `page`, `size`, `sort`

**200 OK** → `Page<TaskResource>`

### GET `/tasks/{id}`
Dettaglio task per `id` (opz. `isArchived`).

**200 OK** → `TaskResource`

### POST `/tasks`
Crea task.

Body `CreateTaskRequest` (esempio):
```json
{
  "boardId": 1,
  "name": "Implementare filtro",
  "description": "Ricerca per titolo",
  "status": "TODO",
  "icon": "FEATURE"
}
```

**201 Created** → `TaskResource` (+ `Location` header)

### PUT `/tasks/{id}`
Aggiorna task.

Body `UpdateTaskRequest` (esempio):
```json
{
  "name": "Implementare filtro veloce",
  "description": "Debounce 350ms",
  "status": "IN_PROGRESS",
  "icon": "REFACTOR"
}
```

**200 OK** → `TaskResource`

### DELETE `/tasks/archive/{id}`
Archivia (soft delete) singola task.

**200 OK** → `TaskResource`

### POST `/tasks/archive`
Archivia lista di task.

Body `IdsRequest`:
```json
{ "idList": [10,11,12] }
```

**200 OK** → `BulkResource { operation: "ARCHIVE_LIST", updatedRow: N }`

### PUT `/tasks/restore/{id}`
Ripristina task singola.

**200 OK** → `TaskResource`

### POST `/tasks/restore`
Ripristina lista di task.

Body `IdsRequest`:
```json
{ "idList": [10,11,12] }
```

**200 OK** → `BulkResource { operation: "RESTORE_LIST", updatedRow: N }`

### DELETE `/tasks/delete/{id}`
Elimina definitivamente una task.

**204 No Content**

### POST `/tasks/delete`
Elimina definitivamente più task.

Body `IdsRequest`:
```json
{ "idList": [10,11,12] }
```

**200 OK** → `BulkResource { operation: "DELETE_LIST", updatedRow: N }`

---

## Esempi cURL

```bash
# Lista board attive (page=0, size=20)
curl "http://localhost:8181/boards?page=0&size=20"

# Crea board
curl -X POST "http://localhost:8181/boards" \
  -H "Content-Type: application/json" \
  -d '{"name":"Roadmap Q4","description":"Iniziative quarto trimestre"}'

# Archivia board 5
curl -X DELETE "http://localhost:8181/boards/archive/5"

# Ripristina board 5 con task
curl -X PUT "http://localhost:8181/boards/restore/5?isWithTasks=true"

# Lista task della board 1
curl "http://localhost:8181/tasks?boardId=1&page=0&size=20"
```

## Note di Implementazione

- Logging strutturato via `LoggerUtils` inizio/fine operazioni
- Validazione `@Valid` per request DTO
- Assemblers/Mapper separano **DTO ↔ Model**
- `IdsRequest` de-duplica gli ID via `distinct()` lato controller
- **Devtools** abilitati per hot reload in dev
- `spring.jpa.hibernate.ddl-auto=update` (valutare `validate`/migrations in prod)

## Licenza

Distribuito sotto licenza **MIT**.
