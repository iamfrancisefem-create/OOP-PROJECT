package com.pms.mapper;

import com.pms.dto.response.NotificationResponse;
import com.pms.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
