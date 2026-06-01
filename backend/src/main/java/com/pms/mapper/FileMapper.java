package com.pms.mapper;

import com.pms.dto.response.FileUploadResponse;
import com.pms.entity.FileUpload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// OOP — ABSTRACTION: MapStruct generates FileUpload ↔ FileUploadResponse converter
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface FileMapper {

    @Mapping(target = "projectId", source = "project.id")
    FileUploadResponse toResponse(FileUpload fileUpload);
}
