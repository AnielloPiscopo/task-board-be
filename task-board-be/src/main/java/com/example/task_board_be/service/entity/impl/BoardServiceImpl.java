package com.example.task_board_be.service.entity.impl;

import com.example.task_board_be.exception.custom.NotFoundException;
import com.example.task_board_be.exception.custom.StateMismatchException;
import com.example.task_board_be.mapping.mapper.BoardMapper;
import com.example.task_board_be.pojo.entity.BaseEntity;
import com.example.task_board_be.pojo.entity.Board;
import com.example.task_board_be.pojo.model.BoardModel;
import com.example.task_board_be.pojo.model.TaskModel;
import com.example.task_board_be.repo.entity.BoardRepository;
import com.example.task_board_be.service.entity.BoardService;
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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.example.task_board_be.repo.spec.BoardSpecifications.filterBoards;


@Service
public class BoardServiceImpl implements BoardService {
    private final BoardRepository repo;
    private final BoardMapper mapper;

    private final TaskService taskService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String RESOURCE_NAME = "board";

    @Autowired
    public BoardServiceImpl(BoardRepository repo, BoardMapper mapper, TaskService taskService) {
        this.repo = repo;
        this.mapper = mapper;
        this.taskService = taskService;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<BoardModel> getPage(String filterStr, boolean isArchived, Pageable p) {
        logger.info("{} - [PARAMS: filterStr->{} , isArchived->{} ; p->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), filterStr, isArchived, p);

        Page<BoardModel> boardModelPage = repo.findAll(filterBoards(filterStr, isArchived), p).map(mapper::toModel);

        logger.info("{} - [RESULT: pageSize->{} ; pageTotalElements->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false),
                boardModelPage.getNumberOfElements(), boardModelPage.getTotalElements());
        return boardModelPage;
    }

    @Transactional(readOnly = true)
    @Override
    public List<BoardModel> getList(boolean isArchived) {
        logger.info("{} - [PARAMS: isArchived->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), isArchived);

        List<Board> boardList = (isArchived)
                ? repo.findAllByIsArchivedTrue().stream().toList()
                : repo.findAllByIsArchivedFalse().stream().toList();
        List<BoardModel> boardModelList = mapper.toModelList(boardList);

        logger.info("{} - [RESULT: boardModelList -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), boardModelList);
        return boardModelList;
    }

    @Transactional(readOnly = true)
    @Override
    public BoardModel getEl(Long id, boolean isArchived) {
        logger.info("{} - [PARAMS: id->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id);

        BoardModel boardModel = mapper.toModelWithCascade(getBoard(id, isArchived));

        logger.info("{} - [RESULT: boardModel -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), boardModel);
        return boardModel;
    }

    @Transactional
    @Override
    public BoardModel create(BoardModel boardModel) {
        logger.info("{} - [PARAMS: boardModel->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), boardModel);

        Board board = mapper.toEntity(boardModel);

        if(StringUtils.isBlank(board.getName())){
            board.setName(getNextDefaultName());
        }

        board = repo.save(board);

        boardModel = mapper.toModel(board);

        logger.info("{} - [RESULT: boardModel -> {}]",
                LoggerUtils.getStandardLoggerMsg("end", false), boardModel);
        return boardModel;
    }

    @Transactional
    @Override
    public BoardModel update(BoardModel boardModel) {
        logger.info("{} - [PARAMS: boardModel->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), boardModel);

        Long id = boardModel.getId();
        Board board = getBoard(id, false);

        if (!StringUtils.isBlank(boardModel.getName())) {
            board.setName(boardModel.getName());
        }

        board.setDescription(boardModel.getDescription());
        board = repo.save(board);

        boardModel = mapper.toModelWithCascade(board);

        logger.info("{} - [RESULT: boardModel->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), boardModel);
        return boardModel;
    }

    @Transactional
    @Override
    public BoardModel archiveEl(Long id) {
        logger.info("{} - [PARAMS: id->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id);

        int updatedRow = repo.archiveByIds(List.of(id));

        checkUpdatedRow(updatedRow, id, false);

        Board board = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Board" + id + "non trovata dopo l'update"));

        BoardModel boardModel = mapper.toModelWithCascade(board);

        logger.info("{} - [RESULT: boardModel->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), boardModel);
        return boardModel;
    }

    @Transactional
    @Override
    public int archiveList(List<Long> idList) {
        logger.info("{} - [PARAMS: idList->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), idList);

        if (idList.isEmpty()) {
            logger.info("{} - [RESULT: updatedRow->{}]",
                    LoggerUtils.getStandardLoggerMsg("end", false) , 0);
            return 0;
        }

        int updatedRow = repo.archiveByIds(idList);

        if (!isUpdatedRow(updatedRow, idList)){
            logger.info("{} - [RESULT: updatedRow->{}]",
                    LoggerUtils.getStandardLoggerMsg("end", false) , 0);
            return 0;
        }

        logger.info("{} - [RESULT: updatedRow->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), updatedRow);
        return updatedRow;
    }

    @Transactional
    @Override
    public int archiveList() {
        logger.info(LoggerUtils.getStandardLoggerMsg("start", false));

        int updatedRow = repo.archiveAllActive();

        if (!isUpdatedRow(updatedRow)){
            logger.info("{} - [RESULT: updatedRow->{}]",
                    LoggerUtils.getStandardLoggerMsg("end", false) , 0);
            return 0;
        }

        List<Board> boardList = repo.findAllByIsArchivedTrue();

        if (boardList.isEmpty()){
            logger.info("{} - [RESULT: updatedRow->{}]",
                    LoggerUtils.getStandardLoggerMsg("end", false) , 0);
            return 0;
        }

        boardList.forEach(b -> b.setIsArchived(true));

        logger.info("{} - [RESULT: updatedRow->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), updatedRow);
        return updatedRow;
    }

    @Transactional
    @Override
    public BoardModel restoreEl(Long id, boolean withTasks) {
        logger.info("{} - [PARAMS: id->{} ; withTasks->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id, withTasks);

        int updatedRow = repo.restoreByIds(List.of(id));

        checkUpdatedRow(updatedRow, id, true);

        if (withTasks) {
            List<TaskModel> taskList = taskService.getList(id, true);

            if (!taskList.isEmpty()) {
                List<Long> taskIdList = taskList.stream().map(TaskModel::getId).toList();
                taskService.restoreList(taskIdList);
            }
        }

        Board board = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Board" + id + "non trovata dopo l'update"));
        BoardModel boardModel = mapper.toModelWithCascade(board);

        logger.info("{} - [RESULT: boardModel->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), boardModel);
        return boardModel;
    }

    @Transactional
    @Override
    public int restoreList(List<Long> idList, boolean withTasks) {
        logger.info("{} - [PARAMS: idList->{} ; withTasks->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), idList, withTasks);

        if (idList == null || idList.isEmpty()) {
            logger.info("{} - [RESULT: updatedRow->{}]",
                    LoggerUtils.getStandardLoggerMsg("end", false) , 0);
            return 0;
        }

        int updatedRow = repo.restoreByIds(idList);

        if (!isUpdatedRow(updatedRow, idList)){
            logger.info("{} - [RESULT: updatedRow->{}]",
                    LoggerUtils.getStandardLoggerMsg("end", false) , 0);
            return 0;
        }

        if (withTasks) {
            List<Long> taskIdList = idList.stream()
                    .flatMap(boardId -> taskService.getList(boardId, true).stream())
                    .map(TaskModel::getId)
                    .toList();

            if (!taskIdList.isEmpty()) {
                taskService.restoreList(taskIdList);
            }
        }

        logger.info("{} - [RESULT: updatedRow->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), updatedRow);
        return updatedRow;
    }

    @Transactional
    @Override
    public int restoreList(boolean withTasks) {
        logger.info("{} - [PARAMS: withTasks->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), withTasks);

        int updatedRow = repo.restoreAllArchived();

        if (!isUpdatedRow(updatedRow)){
            logger.info("{} - [RESULT: updatedRow->{}]",
                    LoggerUtils.getStandardLoggerMsg("end", false) , 0);
            return 0;
        }

        List<Board> boardList = repo.findAllByIsArchivedFalse();

        if (boardList.isEmpty()){
            logger.info("{} - [RESULT: updatedRow->{}]",
                    LoggerUtils.getStandardLoggerMsg("end", false) , 0);
            return 0;
        }

        if (withTasks) {
            List<Long> taskIdList = boardList.stream()
                    .flatMap(b -> taskService.getList(b.getId(), true).stream())
                    .map(TaskModel::getId)
                    .toList();

            if (!taskIdList.isEmpty()) {
                taskService.restoreList(taskIdList);
            }
        }

        boardList.forEach(b -> b.setIsArchived(false));

        logger.info("{} - [RESULT: updatedRow->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), updatedRow);
        return updatedRow;
    }

    @Transactional
    @Override
    public int delete(Long id) {
        logger.info("{} - [PARAMS: id->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), id);


        int updatedRow = repo.deleteByIdsIfArchived(List.of(id));
        checkUpdatedRow(updatedRow, id, true);

        logger.info("{} - [RESULT: updatedRow->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), updatedRow);
        return updatedRow;
    }

    @Transactional
    @Override
    public int deleteList(List<Long> idList) {
        logger.info("{} - [PARAMS: idList->{}]",
                LoggerUtils.getStandardLoggerMsg("start", false), idList);

        int updatedRow = repo.deleteByIdsIfArchived(idList);
        if (!isUpdatedRow(updatedRow, idList)) return 0;

        logger.info("{} - [RESULT: updatedRow->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), updatedRow);
        return updatedRow;
    }

    @Transactional
    @Override
    public int clear() {
        logger.info(LoggerUtils.getStandardLoggerMsg("start", false));

        int updatedRow = repo.deleteAllByIsArchivedTrue();

        if (!isUpdatedRow(updatedRow)) return 0;

        logger.info("{} - [RESULT: updatedRow->{}]",
                LoggerUtils.getStandardLoggerMsg("end", false), updatedRow);
        return updatedRow;
    }

    private Board getBoard(Long id, boolean isArchived) {
        Board board = repo.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));

        if (board.isArchived() != isArchived) {
            throw new StateMismatchException(RESOURCE_NAME, id, isArchived);
        }

        return board;
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

    private boolean isUpdatedRow(int updated) {
        logger.info("{} - [INFO: updated->{}]",
                LoggerUtils.getStandardLoggerMsg("mid", false), updated);

        if (updated == 0) {
            logger.info(LoggerUtils.getStandardLoggerMsg("end", false));
            return false;
        }

        return true;
    }

    private String getNextDefaultName(){
        String prefix ="New Board";
        List<String> names = repo.findNamesByPrefix(prefix);
        Set<Integer> usedBoardsIdSet = new HashSet<>();
        String base = prefix.toLowerCase(Locale.ROOT);

        for(String n : names){
            if(n==null) continue;
            String defaultName = n.trim().toLowerCase(Locale.ROOT);

            if(defaultName.equals(base)){
                usedBoardsIdSet.add(1);
            }else if(defaultName.startsWith(base + " ")){
                String tail = defaultName.substring(base.length()).trim();

                try{
                    int counter = Integer.parseInt(tail);

                    if(counter>=2){
                        usedBoardsIdSet.add(counter);
                    }
                }catch (NumberFormatException ignore){}
            }
        }

        int elNum = 1;

        while(usedBoardsIdSet.contains(elNum)){
            elNum++;
        }

        return (elNum == 1) ? prefix : (prefix + " " + elNum);
    }
}
