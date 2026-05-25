import { TaskAPI, ProjectAPI, UserAPI } from './api.js';
import { statusBadge, priorityBadge, DateHelper, toast, closeModal, openModal } from './utils.js';

let allTasks = [];

export async function loadTasks(containerSelector, projectFilter = '') {
  try {
    const res = projectFilter ? await TaskAPI.getByProject(projectFilter) : await TaskAPI.getAll(0, 100);
    allTasks = res.data.data?.content || res.data.data || [];
    renderKanban(allTasks, containerSelector);
  } catch (e) {
    document.querySelectorAll('.kanban-tasks').forEach(l => l.innerHTML = '<div class="text-muted text-sm">Failed to load tasks</div>');
  }
}

export function renderKanban(tasks, containerSelector) {
  const board = document.querySelector(containerSelector);
  if (!board) return;
  const columns = { TODO: [], IN_PROGRESS: [], TESTING: [], DONE: [] };
  tasks.forEach(t => { if (columns[t.status]) columns[t.status].push(t); });

  Object.keys(columns).forEach(status => {
    const list = board.querySelector(`.kanban-tasks[data-status="${status}"]`);
    const countEl = board.querySelector(`#${status.toLowerCase()}Count`);
    if (countEl) countEl.textContent = columns[status].length;
    if (!list) return;
    if (!columns[status].length) {
      list.innerHTML = '<div class="empty-state" style="padding:16px"><p class="text-xs text-muted">No tasks</p></div>';
      return;
    }
    list.innerHTML = columns[status].map(t => {
      const deadlineClass = DateHelper.isOverdue(t.deadline) ? 'overdue' : DateHelper.isSoon(t.deadline) ? 'soon' : '';
      return `<div class="task-card" draggable="true" data-id="${t.id}" data-status="${t.status}">
        <div class="task-card-top">
          <div class="task-card-title">${t.title || 'Untitled'}</div>
          ${priorityBadge(t.priority)}
        </div>
        <div class="task-card-desc">${t.description || ''}</div>
        <div class="task-card-footer">
          <div class="task-card-meta">
            <div class="task-deadline ${deadlineClass}"><i class="fa-regular fa-calendar"></i> ${DateHelper.format(t.deadline)}</div>
          </div>
          ${t.assignedTo ? `<div class="avatar-sm" style="background:var(--primary)">${(t.assignedToName || 'U')[0]}</div>` : ''}
        </div>
      </div>`;
    }).join('');
    setupDragAndDrop(list);
  });
}

export function setupDragAndDrop(container) {
  container.querySelectorAll('.task-card').forEach(card => {
    card.addEventListener('dragstart', e => {
      e.dataTransfer.setData('text/plain', card.dataset.id);
      card.classList.add('dragging');
    });
    card.addEventListener('dragend', () => card.classList.remove('dragging'));
  });
}

export function setupDropZones() {
  document.querySelectorAll('.kanban-tasks').forEach(zone => {
    zone.addEventListener('dragover', e => { e.preventDefault(); zone.classList.add('drag-over'); });
    zone.addEventListener('dragleave', () => zone.classList.remove('drag-over'));
    zone.addEventListener('drop', async e => {
      e.preventDefault();
      zone.classList.remove('drag-over');
      const taskId = e.dataTransfer.getData('text/plain');
      const newStatus = zone.dataset.status;
      if (!taskId || !newStatus) return;
      try {
        await TaskAPI.updateStatus(taskId, newStatus);
        const task = allTasks.find(t => t.id == taskId);
        if (task) task.status = newStatus;
        renderKanban(allTasks, '.kanban-board');
        toast('Task updated', 'success');
      } catch (err) {
        toast('Failed to update task', 'error');
      }
    });
  });
}

export async function loadAssigneeOptions(selectId) {
  try {
    const res = await UserAPI.getAll(0, 100);
    const users = res.data.data?.content || res.data.data || [];
    const sel = document.getElementById(selectId);
    if (sel) sel.innerHTML = '<option value="">Unassigned</option>' + users.map(u => `<option value="${u.id}">${u.fullName || u.email}</option>`).join('');
  } catch (e) {}
}

export async function loadProjectOptions(selectId) {
  try {
    const res = await ProjectAPI.getAll(0, 100);
    const projects = res.data.data?.content || res.data.data || [];
    const sel = document.getElementById(selectId);
    if (sel) sel.innerHTML = '<option value="">Select project</option>' + projects.map(p => `<option value="${p.id}">${p.title}</option>`).join('');
  } catch (e) {}
}

export async function saveTask(modalId, fields, loadCb) {
  const title = document.getElementById(fields.title).value.trim();
  if (!title) { document.getElementById(fields.titleErr).textContent = 'Title is required'; return; }
  const projectId = document.getElementById(fields.project).value;
  if (!projectId) { toast('Please select a project', 'warning'); return; }
  const btn = document.getElementById(fields.submitBtn);
  btn.disabled = true; btn.innerHTML = '<span class="spinner"></span>';
  try {
    await TaskAPI.create({
      title,
      description: document.getElementById(fields.desc).value.trim(),
      status: 'TODO',
      priority: document.getElementById(fields.priority).value,
      deadline: document.getElementById(fields.deadline).value || null,
      assignedToId: document.getElementById(fields.assignee).value || null,
      projectId,
    });
    toast('Task created', 'success');
    closeModal(modalId);
    if (loadCb) loadCb();
  } catch (e) {
    toast('Failed to create task', 'error');
  } finally {
    btn.disabled = false; btn.innerHTML = 'Create Task';
  }
}

export function filterTasks(query) {
  if (!query) { renderKanban(allTasks, '.kanban-board'); return; }
  const q = query.toLowerCase();
  const filtered = allTasks.filter(t => (t.title || '').toLowerCase().includes(q) || (t.description || '').toLowerCase().includes(q));
  renderKanban(filtered, '.kanban-board');
}
