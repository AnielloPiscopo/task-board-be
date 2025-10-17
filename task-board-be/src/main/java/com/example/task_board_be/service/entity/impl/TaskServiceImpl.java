package com.example.task_board_be.service.entity.impl;

import com.example.task_board_be.enums.task.TaskIcon;
import com.example.task_board_be.enums.task.TaskStatus;
import com.example.task_board_be.exception.custom.NotFoundException;
import com.example.task_board_be.exception.custom.StateMismatchException;
import com.example.task_board_be.mapping.mapper.BoardMapper;
import com.example.task_board_be.mapping.mapper.TaskMapper;
import com.example.task_board_be.pojo.entity.Board;
import com.example.task_board_be.pojo.entity.Task;
import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.repo.entity.BoardRepository;
import com.example.task_board_be.repo.entity.TaskRepository;
import com.example.task_board_be.service.entity.TaskService;
import com.example.task_board_be.utils.LoggerUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.example.task_board_be.repo.spec.TaskSpecifications.filterTasks;
import static com.example.task_board_be.repo.spec.TaskSpecifications.filterTasksOfBoard;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository repo;
    private final TaskMapper mapper;
    private final BoardMapper boardMapper;

    private final BoardRepository boardRepo;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String RESOURCE_NAME = "task";

    @Autowired
    public TaskServiceImpl(TaskRepository repo, TaskMapper mapper, BoardMapper boardMapper, BoardRepository boardRepo) {
        this.repo = repo;
        this.mapper = mapper;
        this.boardMapper = boardMapper;
        this.boardRepo = boardRepo;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TaskModel> getPage(String filterStr, boolean isArchived, Pageable p) {
        logger.info("{} - [PARAMS: filterStr->{} , isArchived->{} ; p->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), filterStr, isArchived, p);

        Page<TaskModel> taskModelPage = repo.findAll(filterTasks(filterStr, isArchived), p).map(mapper::toModel);

        logger.info("{} - [RESULT: pageSize->{} ; pageTotalElements->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false),
                taskModelPage.getNumberOfElements(), taskModelPage.getTotalElements());
        return taskModelPage;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TaskModel> getPage(String filterStr, Long boardId, boolean isArchived, Pageable p) {
        logger.info("{} - [PARAMS: filterStr->{} ; boardId ->{} ; isArchived->{} ; p->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), filterStr, boardId, isArchived, p);

        Page<TaskModel> taskModelPage = repo.findAll(filterTasksOfBoard(boardId, filterStr, isArchived), p).map(mapper::toModel);

        logger.info("{} - [RESULT: pageSize->{} ; pageTotalElements->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false),
                taskModelPage.getNumberOfElements(), taskModelPage.getTotalElements());
        return taskModelPage;
    }

    @Transactional(readOnly = true)
    @Override
    public List<TaskModel> getList(boolean isArchived) {
        logger.info("{} - [PARAMS: isArchived->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), isArchived);

        List<TaskModel> taskModelList = mapper.toModelList(repo.findAll(filterTasks(null, isArchived)));

        logger.info("{} - [RESULT: taskModelList -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), taskModelList);
        return taskModelList;
    }

    @Transactional(readOnly = true)
    @Override
    public List<TaskModel> getList(Long boardId, boolean isArchived) {
        logger.info("{} - [PARAMS: boardId->{} ; isArchived ->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), boardId, isArchived);

        List<TaskModel> taskModelList = mapper.toModelList(repo.findAll(filterTasksOfBoard(boardId, null, isArchived)));

        logger.info("{} - [RESULT: taskModelList -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), taskModelList);
        return taskModelList;
    }

    @Transactional(readOnly = true)
    @Override
    public TaskModel getEl(Long id, boolean isArchived) {
        logger.info("{} - [PARAMS: id->{} , isArchived -> {}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id, isArchived);

        TaskModel taskModel = mapper.toModel(getTask(id, isArchived));

        logger.info("{} - [RESULT: taskModel -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), taskModel);
        return taskModel;
    }

    @Transactional
    @Override
    public TaskModel create(TaskModel taskModel) {
        logger.info("{} - [PARAMS: taskModel->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), taskModel);

        Long boardId = Optional.ofNullable(taskModel.getBoardModel())
                .map(BoardModel::getId)
                .orElseThrow(() -> new IllegalArgumentException("Board id è obbligatorio"));

        Board board = boardRepo.findById(boardId)
                .orElseThrow(() -> new NotFoundException("board", boardId));

        Task task = mapper.toEntity(taskModel);

        task.setBoard(board);

        if (StringUtils.isBlank(task.getName())) {
            task.setName(getNextDefaultTaskName(boardId));
        }

        if (task.getStatus() == null) task.setStatus(TaskStatus.NONE);
        if (task.getIcon() == null) task.setIcon(TaskIcon.NONE);

        task = repo.save(task);

        taskModel = mapper.toModel(task);
        BoardModel boardModel = boardMapper.toModel(board);
        taskModel.setBoardModel(boardModel);

        logger.info("{} - [RESULT: taskModel -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), taskModel);
        return taskModel;
    }

    @Transactional
    @Override
    public TaskModel update(TaskModel taskModel) {
        logger.info("{} - [PARAMS: taskModel->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), taskModel);

        Long id = taskModel.getId();
        Task task = getTask(id, false);

        if (!StringUtils.isBlank(taskModel.getName())) {
            task.setName(taskModel.getName());
        }

        task.setDescription(taskModel.getDescription());
        task.setStatus(taskModel.getStatus());
        task.setIcon(taskModel.getIcon());

        repo.save(task);

        taskModel = mapper.toModel(task);

        logger.info("{} - [RESULT: taskModel -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), taskModel);
        return taskModel;
    }

    @Transactional
    @Override
    public TaskModel archiveEl(Long id) {
        return toggleStateEl(id, false);
    }

    @Transactional
    @Override
    public int archiveList(List<Long> idList) {
        return toggleStateList(idList, false);
    }

    @Transactional
    @Override
    public TaskModel restoreEl(Long id) {
        return toggleStateEl(id, true);
    }

    @Transactional
    @Override
    public int restoreList(List<Long> idList) {
        return toggleStateList(idList, true);
    }

    @Transactional
    @Override
    public int delete(Long id) {
        logger.info("{} - [PARAMS: id->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id);

        int updated = repo.deleteByIdsIfArchived(List.of(id));
        checkUpdatedRow(updated, id, true);

        logger.info("{} - [RESULT: updatedRow->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), updated);
        return updated;
    }

    @Transactional
    @Override
    public int deleteList(List<Long> idList) {
        logger.info("{} - [PARAMS: idList->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), idList);

        if (idList == null || idList.isEmpty()) return 0;

        int updated = repo.deleteByIdsIfArchived(idList);
        if (!isUpdatedRow(updated, idList)) return 0;

        logger.info("{} - [RESULT: updatedRow->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), updated);
        return updated;
    }

    @Override
    public TaskModel toggleStateEl(Long id, boolean isArchived) {
        logger.info("{} - [PARAMS: id->{} ; isArchived->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id, isArchived);

        int updatedRow = isArchived ? repo.restoreByIds(List.of(id)) : repo.archiveByIds(List.of(id));

        checkUpdatedRow(updatedRow, id, isArchived);

        Task task = repo.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "Task " + id + " non trovata dopo l'update"));

        TaskModel taskModel = mapper.toModel(task);

        logger.info("{} - [RESULT: taskModel->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), taskModel);
        return taskModel;
    }

    @Override
    public int toggleStateList(List<Long> idList, boolean isArchived) {
        logger.info("{} - [PARAMS: idList->{} ; isArchived->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), idList, isArchived);

        if (idList.isEmpty()) {
            logger.info("{} - [RESULT: updatedRow->{}]",
                    LoggerUtils.getStandardLoggerMsg("end", false), 0);
            return 0;
        }

        int updatedRow = isArchived ? repo.restoreByIds(idList) : repo.archiveByIds(idList);

        if (!isUpdatedRow(updatedRow, idList)) {
            logger.info("{} - [RESULT: updatedRow->{}]",
                    LoggerUtils.getStandardLoggerMsg("end", false), 0);
            return 0;
        }

        logger.info("{} - [RESULT: updatedRow->{}])",
                LoggerUtils.getStandardLoggerMsg("end", false), updatedRow);

        return updatedRow;
    }

    private Task getTask(Long id, boolean isArchived) {
        Task task = repo.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));

        if (isArchived != task.isArchived()) {
            throw new StateMismatchException(RESOURCE_NAME, id, isArchived);
        }

        return task;
    }

    private void checkUpdatedRow(int updatedRow, Long id, boolean isArchived) {
        if (updatedRow == 0) {
            if (!repo.existsById(id)) throw new NotFoundException(RESOURCE_NAME, id);

            throw new StateMismatchException(RESOURCE_NAME, id, isArchived);
        }
    }

    private boolean isUpdatedRow(int updated, List<Long> idList) {
        logger.info("{} - [INFO: updated->{} ; idList size->{}]",
                LoggerUtils.getStandardLoggerMsg("mid", false), updated, idList.size());

        if (updated == 0) {
            logger.info(LoggerUtils.getStandardLoggerMsg("end", false));
            return false;
        }

        return true;
    }

    private String getNextDefaultTaskName(Long boardId) {
        String prefix = "New Task";
        List<String> names = repo.findNamesByPrefixForBoard(boardId, prefix);

        Set<Integer> used = new HashSet<>();
        String base = prefix.toLowerCase(Locale.ROOT);

        for (String n : names) {
            if (n == null) continue;
            String def = n.trim().toLowerCase(Locale.ROOT);

            if (def.equals(base)) {
                used.add(1);
            } else if (def.startsWith(base + " ")) {
                String tail = def.substring(base.length()).trim();
                try {
                    int counter = Integer.parseInt(tail);
                    if (counter >= 2) used.add(counter);
                } catch (NumberFormatException ignore) {
                }
            }
        }

        int num = 1;
        while (used.contains(num)) num++;
        return (num == 1) ? prefix : (prefix + " " + num);
    }

}
