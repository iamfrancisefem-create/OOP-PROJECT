package com.pms.service;

import com.pms.dto.request.CreateInviteRequest;
import com.pms.dto.response.InviteResponse;

import java.util.List;

public interface PlatformInvitationService {
    InviteResponse createInvite(CreateInviteRequest request);
    InviteResponse acceptInvite(String token);
    InviteResponse validateInvite(String token);
    List<InviteResponse> getPendingInvites();
    void cancelInvite(Long id);
}
