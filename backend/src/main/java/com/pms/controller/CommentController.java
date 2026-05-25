package com.pms.controller;

import com.pms.dto.request.CommentRequest;
import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.CommentResponse;
import com.pms.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(@Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully.", response));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<PagedResponse<CommentResponse>>> getCommentsByTask(
            @PathVariable Long taskId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PagedResponse<CommentResponse> response = commentService.getCommentsByTaskId(taskId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully.", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully.", null));
    }
}
