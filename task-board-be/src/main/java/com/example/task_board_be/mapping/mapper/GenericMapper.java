package com.example.task_board_be.mapping.mapper;

import java.util.List;

public interface GenericMapper<MODEL , ENTITY , RESOURCE> {
    ENTITY toEntity(MODEL model);
    MODEL toModel(ENTITY entity);
    RESOURCE toResource(MODEL model);

    List<ENTITY> toEntityList(List<MODEL> modelList);
    List<MODEL> toModelList(List<ENTITY> entityList);
    List<RESOURCE> toResourceList(List<MODEL> modelList);
}
