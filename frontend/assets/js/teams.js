import { TeamAPI, UserAPI } from './api.js';
import { toast, confirmDialog, closeModal, openModal, renderAvatar } from './utils.js';

export async function loadTeams(containerId) {
  const container = document.getElementById(containerId);
  try {
    const res = await TeamAPI.getAll(0, 50);
    const teams = res.data.data?.content || res.data.data || [];
    if (!teams.length) {
      container.innerHTML = `<div class="empty-state" style="grid-column:1/-1"><div class="empty-icon"><i class="fa-solid fa-users"></i></div><h4>No teams yet</h4><p>Create your first team.</p></div>`;
      return;
    }
    container.innerHTML = teams.map(t => `
      <div class="team-card">
        <div class="team-card-header">
          <div class="team-card-title">${t.teamName || t.name || 'Unnamed'}</div>
          <button class="btn btn-sm btn-ghost" style="color:var(--danger)" onclick="window.deleteTeam(${t.id})"><i class="fa-solid fa-trash"></i></button>
        </div>
        <div class="team-card-desc">${t.description || 'No description'}</div>
        <div class="team-card-stats">
          <div class="team-stat"><div class="val">${t.memberCount || t.members?.length || 0}</div><div class="lbl">Members</div></div>
          <div class="team-stat"><div class="val">${t.projectCount || 0}</div><div class="lbl">Projects</div></div>
        </div>
        <div id="members-${t.id}"></div>
        <button class="btn btn-sm btn-outline mt-4" onclick="window.addMemberToTeam(${t.id})"><i class="fa-solid fa-user-plus"></i> Add Member</button>
      </div>
    `).join('');
    teams.forEach(t => loadTeamMembers(t.id));
  } catch (e) {
    container.innerHTML = '<div class="text-muted">Failed to load teams</div>';
  }
}

export async function loadTeamMembers(teamId) {
  const container = document.getElementById(`members-${teamId}`);
  try {
    const res = await TeamAPI.getById(teamId);
    const members = res.data.data?.members || [];
    if (!members.length) {
      container.innerHTML = '<div class="text-muted text-sm" style="padding:8px 0">No members</div>';
      return;
    }
    container.innerHTML = members.map(m => `
      <div class="member-item">
        <div class="member-avatar">${(m.fullName || m.name || 'U')[0]}</div>
        <div class="member-info">
          <div class="member-name">${m.fullName || m.name || m.email || 'Unknown'}</div>
          <div class="member-role">${m.role || 'Member'}</div>
        </div>
        <button class="btn btn-sm btn-ghost" style="color:var(--danger);margin-left:auto" onclick="window.removeMemberFromTeam(${teamId}, ${m.id || m.userId})"><i class="fa-solid fa-xmark"></i></button>
      </div>
    `).join('');
  } catch (e) {}
}

export async function saveTeam(modalId, loadCb) {
  const name = document.getElementById('teamName').value.trim();
  if (!name) { document.getElementById('teamNameErr').textContent = 'Team name is required'; return; }
  const btn = document.getElementById('teamSaveBtn');
  btn.disabled = true; btn.innerHTML = '<span class="spinner"></span>';
  try {
    await TeamAPI.create({ teamName: name, description: document.getElementById('teamDesc').value.trim() });
    toast('Team created', 'success');
    closeModal(modalId);
    if (loadCb) loadCb();
  } catch (e) {
    toast('Failed to create team', 'error');
  } finally {
    btn.disabled = false; btn.innerHTML = 'Create Team';
  }
}

window.deleteTeam = async function(id) {
  const ok = await confirmDialog('Delete Team', 'Remove this team?', 'Delete');
  if (!ok) return;
  try { await TeamAPI.delete(id); toast('Team deleted', 'success'); window.location.reload(); } catch (e) { toast('Failed', 'error'); }
};

window.addMemberToTeam = async function(teamId) {
  const { value: userId } = await Swal.fire({
    title: 'Add Member',
    input: 'select',
    inputOptions: async () => {
      try {
        const res = await UserAPI.getAll(0, 200);
        const users = res.data.data?.content || res.data.data || [];
        return Object.fromEntries(users.map(u => [u.id, u.fullName || u.email]));
      } catch { return {}; }
    },
    inputPlaceholder: 'Select user...',
    confirmButtonText: 'Add',
    showCancelButton: true,
  });
  if (!userId) return;
  try { await TeamAPI.addMember(teamId, userId, 'TEAM_MEMBER'); toast('Member added', 'success'); loadTeamMembers(teamId); } catch (e) { toast('Failed', 'error'); }
};

window.removeMemberFromTeam = async function(teamId, userId) {
  try { await TeamAPI.removeMember(teamId, userId); toast('Member removed', 'success'); loadTeamMembers(teamId); } catch (e) { toast('Failed', 'error'); }
};
