import { NotificationAPI } from './api.js';
import { DateHelper, toast } from './utils.js';

export async function loadNotifications(containerId, badgeId) {
  const container = document.getElementById(containerId);
  const badge = document.getElementById(badgeId);
  try {
    const res = await NotificationAPI.getAll(0, 20);
    const notifications = res.data.data?.content || res.data.data || [];
    if (!container) return;

    const unread = notifications.filter(n => !n.seen).length;
    if (badge) {
      badge.style.display = unread > 0 ? 'flex' : 'none';
      badge.textContent = unread > 9 ? '9+' : unread;
    }

    if (!notifications.length) {
      container.innerHTML = '<div class="empty-state" style="padding:24px"><p class="text-muted">No notifications yet</p></div>';
      return;
    }

    container.innerHTML = notifications.map(n => `
      <div class="notif-item ${n.seen ? '' : 'unread'}" data-id="${n.id}">
        <div class="notif-icon-wrap" style="background:${n.seen ? 'var(--surface-2)' : 'var(--primary-light)'}">
          <i class="fa-solid ${n.type === 'TASK_ASSIGNED' ? 'fa-list-check' : n.type === 'MESSAGE' ? 'fa-comment' : n.type === 'DEADLINE' ? 'fa-clock' : 'fa-bell'}"></i>
        </div>
        <div class="notif-body">
          <div class="notif-title">${n.title || 'Notification'}</div>
          <div class="notif-sub">${n.message || ''}</div>
          <div class="notif-sub" style="margin-top:4px;font-size:11px">${DateHelper.timeAgo(n.createdAt)}</div>
        </div>
      </div>
    `).join('');

    container.querySelectorAll('.notif-item').forEach(el => {
      el.addEventListener('click', async () => {
        const id = el.dataset.id;
        try {
          await NotificationAPI.markRead(id);
          el.remove();
          const unreadCount = container.querySelectorAll('.notif-item.unread').length;
          if (badge) {
            badge.style.display = unreadCount > 0 ? 'flex' : 'none';
            badge.textContent = unreadCount > 9 ? '9+' : unreadCount;
          }
          if (!unreadCount) {
            container.innerHTML = '<div class="empty-state" style="padding:24px"><p class="text-muted">No notifications yet</p></div>';
          }
        } catch (e) {}
      });
    });
  } catch (e) {
    if (container) container.innerHTML = '<div class="text-muted text-sm">Failed to load notifications</div>';
  }
}

export async function markAllNotificationsRead() {
  try {
    await NotificationAPI.markAllRead();
    document.querySelectorAll('.notif-item').forEach(el => el.classList.remove('unread'));
    const badge = document.getElementById('notifBadge');
    if (badge) badge.style.display = 'none';
    toast('All notifications marked as read', 'success');
  } catch (e) {
    toast('Failed to mark notifications as read', 'error');
  }
}
