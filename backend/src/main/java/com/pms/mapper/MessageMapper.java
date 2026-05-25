package com.pms.mapper;

import com.pms.dto.request.MessageRequest;
import com.pms.dto.response.MessageResponse;
import com.pms.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface MessageMapper {

    MessageResponse toResponse(Message message);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "receiver", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "readStatus", ignore = true)
    Message toEntity(MessageRequest request);
}
