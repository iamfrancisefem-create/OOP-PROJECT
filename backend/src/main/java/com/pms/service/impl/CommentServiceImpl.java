package com.pms.service.impl;

import com.pms.dto.request.CommentRequest;
import com.pms.dto.response.CommentResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.entity.Comment;
import com.pms.entity.Project;
import com.pms.entity.Task;
import com.pms.entity.User;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.CommentMapper;
import com.pms.repository.CommentRepository;
import com.pms.repository.TaskRepository;
import com.pms.repository.UserRepository;
import com.pms.service.CommentService;
import com.pms.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
// OOP — INHERITANCE: implements CommentService interface
// OOP — ENCAPSULATION: hides comment CRUD logic for tasks
// OOP — COMPOSITION: composed of CommentRepository, TaskRepository, UserRepository, CommentMapper, TeamMemberRepository
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final TeamMemberRepository teamMemberRepository;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private void validateProjectAccess(Project project) {
        User currentUser = getCurrentAuthenticatedUser();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ADMIN"));
        boolean isCreator = project.getCreatedBy() != null && project.getCreatedBy().getId().equals(currentUser.getId());
        boolean isTeamMember = project.getTeam() != null && teamMemberRepository.existsByTeamAndUser(project.getTeam(), currentUser);

        if (!isAdmin && !isCreator && !isTeamMember) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not have permission to access comments for this project.");
        }
    }

    @Override
    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        User user = getCurrentAuthenticatedUser();
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + request.getTaskId()));

        validateProjectAccess(task.getProject());

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

        validateProjectAccess(task.getProject());

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
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + id));

        validateProjectAccess(comment.getTask().getProject());

        commentRepository.delete(comment);
    }
}
