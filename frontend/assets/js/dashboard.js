import { DashboardAPI, ProjectAPI, TaskAPI } from './api.js';
import { statusBadge, DateHelper, toast } from './utils.js';
import Chart from 'chart.js/auto';

let taskChartInstance = null;
let projectChartInstance = null;

export async function loadDashboardStats(containerId) {
  const container = document.getElementById(containerId);
  try {
    const res = await DashboardAPI.getStats();
    const data = res.data.data;
    container.innerHTML = '';
    const cards = [
      { label: 'Total Projects', value: data.totalProjects || 0, icon: 'fa-folder', color: 'blue' },
      { label: 'Completed Tasks', value: data.completedTasks || 0, icon: 'fa-check-circle', color: 'green' },
      { label: 'Pending Tasks', value: data.pendingTasks || 0, icon: 'fa-clock', color: 'amber' },
      { label: 'Overdue Tasks', value: data.overdueTasks || 0, icon: 'fa-exclamation-circle', color: 'red' },
      { label: 'Team Members', value: data.totalMembers || 0, icon: 'fa-users', color: 'cyan' },
    ];
    cards.forEach(c => {
      container.innerHTML += `
        <div class="stat-card">
          <div class="stat-icon ${c.color}"><i class="fa-solid ${c.icon}"></i></div>
          <div class="stat-info">
            <div class="label">${c.label}</div>
            <div class="value">${c.value}</div>
          </div>
        </div>`;
    });
  } catch (e) {
    container.innerHTML = '<div class="text-muted">Failed to load stats</div>';
  }
}

export async function loadDashboardCharts(taskCtxId, projectCtxId) {
  try {
    const res = await DashboardAPI.getAnalytics();
    const data = res.data.data;
    const taskCtx = document.getElementById(taskCtxId)?.getContext('2d');
    if (taskCtx) {
      if (taskChartInstance) taskChartInstance.destroy();
      taskChartInstance = new Chart(taskCtx, {
        type: 'doughnut',
        data: {
          labels: ['To Do', 'In Progress', 'Testing', 'Done'],
          datasets: [{
            data: [data.todoTasks || 0, data.inProgressTasks || 0, data.testingTasks || 0, data.doneTasks || 0],
            backgroundColor: ['#94A3B8', '#2563EB', '#F59E0B', '#10B981'],
            borderWidth: 0,
          }],
        },
        options: { responsive: true, plugins: { legend: { display: false } }, cutout: '70%' },
      });
    }
    const projCtx = document.getElementById(projectCtxId)?.getContext('2d');
    if (projCtx) {
      if (projectChartInstance) projectChartInstance.destroy();
      const labels = data.projectLabels || ['Project 1', 'Project 2', 'Project 3'];
      const progress = data.projectProgress || [30, 60, 90];
      projectChartInstance = new Chart(projCtx, {
        type: 'bar',
        data: {
          labels,
          datasets: [{ label: 'Progress %', data: progress, backgroundColor: '#2563EB', borderRadius: 6 }],
        },
        options: { responsive: true, scales: { y: { beginAtZero: true, max: 100 } }, plugins: { legend: { display: false } } },
      });
    }
  } catch (e) {
    console.log('Charts data unavailable');
  }
}

export async function loadRecentProjects(containerId, limit = 4) {
  const container = document.getElementById(containerId);
  try {
    const res = await ProjectAPI.getAll(0, limit);
    const projects = res.data.data?.content || res.data.data || [];
    if (!projects.length) {
      container.innerHTML = '<div class="empty-state"><div class="empty-icon"><i class="fa-solid fa-folder-open"></i></div><h4>No projects yet</h4><p>Create your first project.</p></div>';
      return;
    }
    container.innerHTML = projects.map(p => `
      <div class="project-card" onclick="window.location.href='project-details.html?id=${p.id}'">
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
          <span><i class="fa-regular fa-calendar"></i> ${DateHelper.format(p.startDate)}</span>
          <span><i class="fa-regular fa-clock"></i> ${DateHelper.format(p.endDate)}</span>
        </div>
      </div>
    `).join('');
  } catch (e) {
    container.innerHTML = '<div class="text-muted">Failed to load projects</div>';
  }
}

export async function loadActivityFeed(containerId, limit = 5) {
  const container = document.getElementById(containerId);
  try {
    const res = await TaskAPI.getAll(0, limit);
    const tasks = res.data.data?.content || res.data.data || [];
    if (!tasks.length) {
      container.innerHTML = '<div class="text-muted text-sm">No recent activity</div>';
      return;
    }
    container.innerHTML = tasks.map(t => `
      <div class="activity-item">
        <div class="activity-dot-wrap">
          <div class="activity-dot ${t.status === 'DONE' ? 'success' : t.status === 'IN_PROGRESS' ? '' : 'warning'}"></div>
          <div class="activity-line"></div>
        </div>
        <div class="activity-body">
          <div class="act-text"><strong>${t.assignedToName || 'Someone'}</strong> ${t.status === 'DONE' ? 'completed' : 'updated'} <strong>${t.title || 'task'}</strong></div>
          <div class="act-time">${DateHelper.timeAgo(t.updatedAt || t.createdAt)}</div>
        </div>
      </div>
    `).join('');
  } catch (e) {
    container.innerHTML = '<div class="text-muted text-sm">No recent activity</div>';
  }
}
