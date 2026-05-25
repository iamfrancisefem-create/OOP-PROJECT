package com.pms.mapper;

import com.pms.dto.request.CommentRequest;
import com.pms.dto.response.CommentResponse;
import com.pms.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "taskId", source = "task.id")
    CommentResponse toResponse(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "user", ignore = true)
    Comment toEntity(CommentRequest request);
}
