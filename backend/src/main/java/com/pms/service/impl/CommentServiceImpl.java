package com.pms.service.impl;

import com.pms.dto.request.CommentRequest;
import com.pms.dto.response.CommentResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.entity.Comment;
import com.pms.entity.Task;
import com.pms.entity.User;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.CommentMapper;
import com.pms.repository.CommentRepository;
import com.pms.repository.TaskRepository;
import com.pms.repository.UserRepository;
import com.pms.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        User user = getCurrentAuthenticatedUser();
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + request.getTaskId()));

        Comment comment = commentMapper.toEntity(request);
        comment.setTask(task);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponse(savedComment);
    }

    @Override
    public PagedResponse<CommentResponse> getCommentsByTaskId(Long taskId, Pageable pageable) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        Page<Comment> commentsPage = commentRepository.findByTask(task, pageable);
        List<CommentResponse> content = commentsPage.getContent().stream()
                .map(commentMapper::toResponse)
                .toList();

        return PagedResponse.<CommentResponse>builder()
                .content(content)
                .page(commentsPage.getNumber())
                .size(commentsPage.getSize())
                .totalElements(commentsPage.getTotalElements())
                .totalPages(commentsPage.getTotalPages())
                .last(commentsPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comment not found with ID: " + id);
        }
        commentRepository.deleteById(id);
    }
}
