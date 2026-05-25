import { ProjectAPI, TeamAPI } from './api.js';
import { statusBadge, DateHelper, toast, confirmDialog, closeModal, openModal, renderPagination, renderAvatar } from './utils.js';

let currentPage = 0;
let totalPages = 0;

export async function loadProjects(containerId, paginationId, page = 0, search = '') {
  currentPage = page;
  const container = document.getElementById(containerId);
  try {
    const res = await ProjectAPI.getAll(page, 12);
    const data = res.data.data;
    const projects = data.content || data || [];
    totalPages = data.totalPages || 1;
    if (!projects.length) {
      container.innerHTML = `<div class="empty-state" style="grid-column:1/-1"><div class="empty-icon"><i class="fa-solid fa-folder-open"></i></div><h4>No projects found</h4><p>Create your first project.</p></div>`;
      return;
    }
    let filtered = projects;
    if (search) {
      const q = search.toLowerCase();
      filtered = projects.filter(p => (p.title || '').toLowerCase().includes(q) || (p.description || '').toLowerCase().includes(q));
    }
    container.innerHTML = filtered.map(p => `
      <div class="project-card" data-id="${p.id}">
        <div class="project-card-header">
          <div class="project-card-title">${p.title || 'Untitled'}</div>
          ${statusBadge(p.status)}
        </div>
        <div class="project-card-desc">${p.description || 'No description'}</div>
        <div class="project-progress-wrap">
          <div class="project-progress-label"><span>Progress</span><span>${Math.round(p.progress || 0)}%</span></div>
          <div class="progress"><div class="progress-bar ${(p.progress || 0) >= 100 ? 'success' : ''}" style="width:${p.progress || 0}%"></div></div>
        </div>
        <div class="project-card-meta">
          <span><i class="fa-regular fa-calendar"></i> ${DateHelper.format(p.startDate)} - ${DateHelper.format(p.endDate)}</span>
        </div>
        <div class="project-card-footer">
          <div class="avatar-group">${renderAvatar(p.createdByName || 'U', null, 28)}</div>
          <div class="project-actions">
            <button class="btn btn-sm btn-ghost view-proj" data-id="${p.id}"><i class="fa-solid fa-eye"></i></button>
            <button class="btn btn-sm btn-ghost delete-proj" style="color:var(--danger)" data-id="${p.id}"><i class="fa-solid fa-trash"></i></button>
          </div>
        </div>
      </div>
    `).join('');
    container.querySelectorAll('.view-proj').forEach(b => b.addEventListener('click', () => window.location.href = `project-details.html?id=${b.dataset.id}`));
    container.querySelectorAll('.delete-proj').forEach(b => b.addEventListener('click', () => deleteProjectHandler(b.dataset.id, containerId, paginationId)));
    renderPagination(paginationId, currentPage, totalPages, (p) => loadProjects(containerId, paginationId, p, search));
  } catch (e) {
    container.innerHTML = '<div class="text-muted">Failed to load projects</div>';
  }
}

export async function loadTeamOptions(selectId) {
  try {
    const res = await TeamAPI.getAll(0, 100);
    const teams = res.data.data?.content || res.data.data || [];
    const sel = document.getElementById(selectId);
    if (sel) sel.innerHTML = '<option value="">No team</option>' + teams.map(t => `<option value="${t.id}">${t.teamName || t.name}</option>`).join('');
  } catch (e) {}
}

export async function saveProject(modalId, formFields, loadCb) {
  const title = document.getElementById(formFields.title).value.trim();
  if (!title) { document.getElementById(formFields.titleErr).textContent = 'Title is required'; return; }
  const btn = document.getElementById(formFields.submitBtn);
  btn.disabled = true; btn.innerHTML = '<span class="spinner"></span>';
  try {
    await ProjectAPI.create({
      title,
      description: document.getElementById(formFields.desc).value.trim(),
      priority: document.getElementById(formFields.priority).value,
      status: document.getElementById(formFields.status).value,
      startDate: document.getElementById(formFields.startDate).value || null,
      endDate: document.getElementById(formFields.endDate).value || null,
      teamId: document.getElementById(formFields.team).value || null,
    });
    toast('Project created successfully', 'success');
    closeModal(modalId);
    if (loadCb) loadCb();
  } catch (e) {
    toast(e.response?.data?.message || 'Failed to create project', 'error');
  } finally {
    btn.disabled = false; btn.innerHTML = 'Create Project';
  }
}

async function deleteProjectHandler(id, containerId, paginationId) {
  const ok = await confirmDialog('Delete Project', 'This action cannot be undone.', 'Delete');
  if (!ok) return;
  try {
    await ProjectAPI.delete(id);
    toast('Project deleted', 'success');
    loadProjects(containerId, paginationId, currentPage);
  } catch (e) {
    toast('Failed to delete project', 'error');
  }
}
