package com.pms.controller;

import com.pms.dto.request.CreateInviteRequest;
import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.InviteResponse;
import com.pms.service.PlatformInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invites")
@RequiredArgsConstructor
public class PlatformInviteController {

    private final PlatformInvitationService invitationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InviteResponse>> createInvite(@Valid @RequestBody CreateInviteRequest request) {
        InviteResponse response = invitationService.createInvite(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invitation sent successfully.", response));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<InviteResponse>> validateInvite(@RequestParam String token) {
        InviteResponse response = invitationService.validateInvite(token);
        return ResponseEntity.ok(ApiResponse.success("Invitation is valid.", response));
    }

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<InviteResponse>> acceptInvite(@RequestParam String token) {
        InviteResponse response = invitationService.acceptInvite(token);
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted.", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InviteResponse>>> getPendingInvites() {
        List<InviteResponse> invites = invitationService.getPendingInvites();
        return ResponseEntity.ok(ApiResponse.success("Pending invitations retrieved.", invites));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelInvite(@PathVariable Long id) {
        invitationService.cancelInvite(id);
        return ResponseEntity.ok(ApiResponse.success("Invitation cancelled.", null));
    }
}
