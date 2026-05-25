package com.pms.controller;

import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.NotificationResponse;
import com.pms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getMyNotifications(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PagedResponse<NotificationResponse> response = notificationService.getMyNotifications(pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully.", response));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read successfully.", null));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read successfully.", null));
    }
}
