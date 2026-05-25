package com.pms.service;

import com.pms.dto.request.CommentRequest;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.CommentResponse;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponse createComment(CommentRequest request);
    PagedResponse<CommentResponse> getCommentsByTaskId(Long taskId, Pageable pageable);
    void deleteComment(Long id);
}
