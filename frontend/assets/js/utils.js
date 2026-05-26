/* ============================================================
   PMS — Utility Helpers & Validators
   utils.js
   ============================================================ */
'use strict';

// ─── Constants ───────────────────────────────────────────
export const ROLES = {
  ADMIN: 'ADMIN',
  PROJECT_MANAGER: 'PROJECT_MANAGER',
  TEAM_LEADER: 'TEAM_LEADER',
  TEAM_MEMBER: 'TEAM_MEMBER',
  PRODUCT_OWNER: 'PRODUCT_OWNER',
};

export const TASK_STATUS = { TODO: 'TODO', IN_PROGRESS: 'IN_PROGRESS', TESTING: 'TESTING', DONE: 'DONE' };
export const TASK_PRIORITY = { LOW: 'LOW', MEDIUM: 'MEDIUM', HIGH: 'HIGH', URGENT: 'URGENT' };
export const PROJECT_STATUS = { NEW: 'NEW', ACTIVE: 'ACTIVE', ON_HOLD: 'ON_HOLD', COMPLETED: 'COMPLETED', CANCELLED: 'CANCELLED' };

// ─── Session / Auth State ────────────────────────────────
export const Session = {
  set(token, refreshToken, user) {
    sessionStorage.setItem('pms_token', token);
    sessionStorage.setItem('pms_refresh', refreshToken);
    sessionStorage.setItem('pms_user', JSON.stringify(user));
  },
  getToken()   { return sessionStorage.getItem('pms_token'); },
  getRefresh() { return sessionStorage.getItem('pms_refresh'); },
  getUser()    {
    try { return JSON.parse(sessionStorage.getItem('pms_user')); }
    catch { return null; }
  },
  clear() {
    sessionStorage.removeItem('pms_token');
    sessionStorage.removeItem('pms_refresh');
    sessionStorage.removeItem('pms_user');
  },
  isLoggedIn() { return !!this.getToken() && !!this.getUser(); },
  hasRole(role) {
    const user = this.getUser();
    return user?.roles?.includes(role) ?? false;
  },
  hasAnyRole(...roles) { return roles.some(r => this.hasRole(r)); },
};

// ─── Role-Based UI Gating ────────────────────────────────
export function applyRoleGating() {
  const user = Session.getUser();
  if (!user) return;
  document.querySelectorAll('[data-role-required]').forEach(el => {
    const required = el.getAttribute('data-role-required').split(',').map(r => r.trim());
    const allowed  = required.some(r => user.roles?.includes(r));
    el.style.display = allowed ? '' : 'none';
  });
}

// ─── Theme ───────────────────────────────────────────────
export const Theme = {
  STORAGE_KEY: 'pms_theme',
  get()  { return localStorage.getItem(this.STORAGE_KEY) || 'light'; },
  set(t) { localStorage.setItem(this.STORAGE_KEY, t); document.documentElement.setAttribute('data-theme', t); },
  toggle() { this.set(this.get() === 'dark' ? 'light' : 'dark'); },
  init()   { document.documentElement.setAttribute('data-theme', this.get()); },
};

// ─── Validators ──────────────────────────────────────────
export const Validators = {
  email(val) { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val.trim()); },
  required(val) { return val !== null && val !== undefined && String(val).trim().length > 0; },
  minLength(val, n) { return String(val).length >= n; },
  maxLength(val, n) { return String(val).length <= n; },
  password(val) {
    return {
      length:  val.length >= 6,
      upper:   /[A-Z]/.test(val),
      number:  /[0-9]/.test(val),
      special: /[^A-Za-z0-9]/.test(val),
      get score() { return [this.length, this.upper, this.number, this.special].filter(Boolean).length; },
      get label() {
        const s = this.score;
        if (s <= 1) return 'weak';
        if (s === 2) return 'fair';
        if (s === 3) return 'good';
        return 'strong';
      },
    };
  },
};

// ─── Form Validation Helper ──────────────────────────────
export function validateForm(rules) {
  let valid = true;
  for (const [selector, checks] of Object.entries(rules)) {
    const el = document.querySelector(selector);
    if (!el) continue;
    const errEl = el.closest('.form-group')?.querySelector('.form-error');
    let msg = '';
    if (checks.required && !Validators.required(el.value)) msg = 'This field is required.';
    else if (checks.email  && !Validators.email(el.value))   msg = 'Enter a valid email address.';
    else if (checks.min    && !Validators.minLength(el.value, checks.min)) msg = `Minimum ${checks.min} characters required.`;
    else if (checks.match) {
      const matchEl = document.querySelector(checks.match);
      if (matchEl && el.value !== matchEl.value) msg = 'Passwords do not match.';
    }
    if (msg) {
      el.classList.add('error'); el.classList.remove('success');
      if (errEl) { errEl.textContent = msg; errEl.style.display = 'block'; }
      valid = false;
    } else {
      el.classList.remove('error'); el.classList.add('success');
      if (errEl) { errEl.textContent = ''; errEl.style.display = 'none'; }
    }
  }
  return valid;
}

// ─── Toast Notifications (Toastify) ─────────────────────
export function toast(message, type = 'info') {
  const colors = {
    success: '#10B981', error: '#EF4444', warning: '#F59E0B', info: '#2563EB',
  };
  const icons = { success: '✔', error: '✖', warning: '⚠', info: 'ℹ' };
  if (window.Toastify) {
    Toastify({
      text: `${icons[type] || ''}  ${message}`,
      duration: 4000,
      gravity: 'top',
      position: 'right',
      stopOnFocus: true,
      style: { background: colors[type] || colors.info, borderRadius: '10px', fontFamily: 'Inter, sans-serif', fontSize: '14px', fontWeight: '600', padding: '12px 18px', boxShadow: '0 8px 24px rgba(0,0,0,0.18)' },
    }).showToast();
  } else {
    console.log(`[${type.toUpperCase()}] ${message}`);
  }
}

// ─── SweetAlert2 Confirm ─────────────────────────────────
export async function confirmDialog(title, text, confirmText = 'Confirm', type = 'warning') {
  if (!window.Swal) return true;
  const result = await Swal.fire({
    title, text,
    icon: type,
    confirmButtonText: confirmText,
    cancelButtonText: 'Cancel',
    showCancelButton: true,
    confirmButtonColor: type === 'warning' ? '#EF4444' : '#2563EB',
    cancelButtonColor: '#94A3B8',
    borderRadius: '16px',
    customClass: { popup: 'swal-popup' },
  });
  return result.isConfirmed;
}

// ─── Date & Time Helpers ─────────────────────────────────
export const DateHelper = {
  format(iso) {
    if (!iso) return '—';
    const d = new Date(iso);
    return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
  },
  formatDateTime(iso) {
    if (!iso) return '—';
    const d = new Date(iso);
    return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  },
  timeAgo(iso) {
    if (!iso) return '';
    const diff = Date.now() - new Date(iso).getTime();
    const secs  = Math.floor(diff / 1000);
    const mins  = Math.floor(secs / 60);
    const hours = Math.floor(mins / 60);
    const days  = Math.floor(hours / 24);
    if (days > 0)  return `${days}d ago`;
    if (hours > 0) return `${hours}h ago`;
    if (mins > 0)  return `${mins}m ago`;
    return 'Just now';
  },
  isOverdue(deadlineIso) {
    if (!deadlineIso) return false;
    return new Date(deadlineIso) < new Date();
  },
  isSoon(deadlineIso, days = 3) {
    if (!deadlineIso) return false;
    const deadline = new Date(deadlineIso);
    const threshold = new Date(Date.now() + days * 864e5);
    return deadline <= threshold && deadline >= new Date();
  },
};

// ─── DOM Helpers ─────────────────────────────────────────
export const $ = (sel, ctx = document) => ctx.querySelector(sel);
export const $$ = (sel, ctx = document) => [...ctx.querySelectorAll(sel)];

export function setLoading(btn, loading) {
  if (!btn) return;
  if (loading) {
    btn.dataset.originalText = btn.innerHTML;
    btn.innerHTML = `<span class="spinner"></span>`;
    btn.disabled = true;
  } else {
    btn.innerHTML = btn.dataset.originalText || btn.innerHTML;
    btn.disabled = false;
  }
}

export function showAlert(el, msg, type = 'error') {
  if (!el) return;
  el.className = `alert-banner show ${type}`;
  el.textContent = '';
  const icon = document.createElement('i');
  icon.className = `fa-solid fa-${type === 'error' ? 'circle-exclamation' : type === 'success' ? 'circle-check' : 'circle-info'}`;
  el.appendChild(icon);
  const span = document.createElement('span');
  span.textContent = msg;
  el.appendChild(span);
}

export function hideAlert(el) {
  if (!el) return;
  el.className = 'alert-banner';
  el.innerHTML = '';
}

// ─── Initials Avatar ─────────────────────────────────────
export function getInitials(name = '') {
  return name.trim().split(/\s+/).map(w => w[0]?.toUpperCase()).slice(0, 2).join('');
}

// ─── Avatar color pool ───────────────────────────────────
const AVATAR_COLORS = ['#2563EB','#7C3AED','#DB2777','#D97706','#059669','#0891B2','#DC2626','#65A30D'];
export function getAvatarColor(name = '') {
  let hash = 0;
  for (const ch of name) hash = ch.charCodeAt(0) + ((hash << 5) - hash);
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];
}

// ─── Render Avatar ───────────────────────────────────────
export function renderAvatar(name, imgUrl, size = 36) {
  const sanitized = sanitize(name);
  if (imgUrl) {
    const safeUrl = imgUrl.replace(/["\\]/g, '');
    return `<img src="${safeUrl}" alt="${sanitized}" style="width:${size}px;height:${size}px;border-radius:50%;object-fit:cover">`;
  }
  const initials = getInitials(name);
  const color    = getAvatarColor(name);
  return `<div style="width:${size}px;height:${size}px;border-radius:50%;background:${color};display:flex;align-items:center;justify-content:center;font-size:${Math.floor(size*0.38)}px;font-weight:700;color:#fff;flex-shrink:0">${initials}</div>`;
}

// ─── Badge Helpers ───────────────────────────────────────
export function statusBadge(status) {
  const map = {
    TODO: ['badge-neutral', 'To Do'], IN_PROGRESS: ['badge-primary', 'In Progress'],
    TESTING: ['badge-warning', 'Testing'], DONE: ['badge-success', 'Done'],
    NEW: ['badge-info', 'New'], ACTIVE: ['badge-primary', 'Active'],
    ON_HOLD: ['badge-warning', 'On Hold'], COMPLETED: ['badge-success', 'Completed'],
    CANCELLED: ['badge-danger', 'Cancelled'],
    PENDING: ['badge-warning', 'Pending'],
  };
  const [cls, label] = map[status] || ['badge-neutral', status];
  return `<span class="badge ${cls}">${label}</span>`;
}

export function priorityBadge(priority) {
  const map = {
    LOW: ['badge-success', 'Low'], MEDIUM: ['badge-info', 'Medium'],
    HIGH: ['badge-warning', 'High'], URGENT: ['badge-danger', 'Urgent'],
  };
  const [cls, label] = map[priority] || ['badge-neutral', priority];
  return `<span class="badge ${cls}">${label}</span>`;
}

// ─── Pagination Helper ───────────────────────────────────
export function renderPagination(containerId, currentPage, totalPages, onPage) {
  const container = document.getElementById(containerId);
  if (!container || totalPages <= 1) { if (container) container.innerHTML = ''; return; }
  let html = `<div class="pagination">`;
  html += `<button class="btn btn-outline btn-sm" ${currentPage === 0 ? 'disabled' : ''} onclick="(${onPage})(${currentPage - 1})"><i class="fa-solid fa-chevron-left"></i></button>`;
  for (let i = 0; i < totalPages; i++) {
    if (i === 0 || i === totalPages - 1 || Math.abs(i - currentPage) <= 1) {
      html += `<button class="btn btn-sm ${i === currentPage ? 'btn-primary' : 'btn-outline'}" onclick="(${onPage})(${i})">${i + 1}</button>`;
    } else if (Math.abs(i - currentPage) === 2) {
      html += `<span class="text-muted" style="padding:0 4px">…</span>`;
    }
  }
  html += `<button class="btn btn-outline btn-sm" ${currentPage === totalPages - 1 ? 'disabled' : ''} onclick="(${onPage})(${currentPage + 1})"><i class="fa-solid fa-chevron-right"></i></button>`;
  html += `</div>`;
  container.innerHTML = html;
}

// ─── Debounce ────────────────────────────────────────────
export function debounce(fn, delay = 300) {
  let timer;
  return (...args) => { clearTimeout(timer); timer = setTimeout(() => fn(...args), delay); };
}

// ─── Modal Helpers ───────────────────────────────────────
export function openModal(id) {
  const el = document.getElementById(id);
  if (el) { el.classList.add('open'); document.body.style.overflow = 'hidden'; }
}
export function closeModal(id) {
  const el = document.getElementById(id);
  if (el) { el.classList.remove('open'); document.body.style.overflow = ''; }
}
export function closeAllModals() {
  document.querySelectorAll('.modal-overlay.open').forEach(m => { m.classList.remove('open'); });
  document.body.style.overflow = '';
}
window.openModal = openModal;
window.closeModal = closeModal;
window.closeAllModals = closeAllModals;

// ─── Sidebar Rendering ───────────────────────────────────
export function initSidebar(activeLink) {
  const user = Session.getUser();
  if (!user) return;

  // Set sidebar user info
  const nameEl  = document.querySelector('.sidebar-user-info .name');
  const roleEl  = document.querySelector('.sidebar-user-info .role-badge');
  const avatarEl = document.querySelector('.sidebar-user .avatar');
  if (nameEl)  nameEl.textContent  = user.fullName || user.email;
  if (roleEl)  roleEl.textContent  = (user.roles?.[0] || '').replace(/_/g, ' ');
  if (avatarEl) avatarEl.innerHTML = renderAvatar(user.fullName || user.email, user.profileImage);

  // Set navbar avatar
  const navAvatar = document.querySelector('.navbar-avatar');
  if (navAvatar) navAvatar.innerHTML = renderAvatar(user.fullName, user.profileImage, 36);

  // Highlight active nav item
  document.querySelectorAll('.nav-item').forEach(item => {
    item.classList.toggle('active', item.dataset.page === activeLink);
  });

  // Apply role-based gating
  applyRoleGating();

  // Show admin section if user has ADMIN role
  if (user.roles?.includes('ADMIN')) {
    const adminLabel = document.getElementById('adminSectionLabel');
    const adminLink  = document.getElementById('adminNavLink');
    if (adminLabel) adminLabel.style.display = '';
    if (adminLink)  adminLink.style.display  = '';
  }

  // Mobile menu toggle
  const menuBtn = document.querySelector('.btn-menu-toggle');
  const sidebar  = document.querySelector('.sidebar');
  const overlay  = document.querySelector('.sidebar-overlay');
  if (menuBtn && sidebar) {
    menuBtn.addEventListener('click', () => {
      sidebar.classList.toggle('open');
      overlay?.classList.toggle('open');
    });
    overlay?.addEventListener('click', () => {
      sidebar.classList.remove('open');
      overlay.classList.remove('open');
    });
  }

  // Theme toggle
  const themeBtn = document.querySelector('.theme-toggle');
  if (themeBtn) {
    themeBtn.addEventListener('click', () => {
      Theme.toggle();
      themeBtn.innerHTML = Theme.get() === 'dark'
        ? '<i class="fa-solid fa-sun"></i>'
        : '<i class="fa-solid fa-moon"></i>';
    });
    themeBtn.innerHTML = Theme.get() === 'dark'
      ? '<i class="fa-solid fa-sun"></i>'
      : '<i class="fa-solid fa-moon"></i>';
  }

  // Notification bell toggle
  const notifBell = document.getElementById('notifBell');
  const notifDropdown = document.getElementById('notifDropdown');
  if (notifBell && notifDropdown) {
    notifBell.addEventListener('click', e => {
      e.stopPropagation();
      notifDropdown.classList.toggle('open');
    });
  }

  // Profile dropdown
  const profileAvatar = document.querySelector('.navbar-avatar');
  if (profileAvatar && !document.getElementById('profileDropdown')) {
    const wrapper = document.createElement('div');
    wrapper.className = 'dropdown-wrapper';
    wrapper.style.marginLeft = 'var(--space-2)';
    profileAvatar.parentNode.insertBefore(wrapper, profileAvatar);
    wrapper.appendChild(profileAvatar);

    const menu = document.createElement('div');
    menu.className = 'dropdown-panel';
    menu.id = 'profileDropdown';
    menu.style.width = '200px';
    menu.style.right = '0';
    menu.innerHTML = `
      <div style="padding:12px 16px;border-bottom:1px solid var(--border)">
        <div style="font-weight:600;font-size:14px">${user.fullName || user.email}</div>
        <div style="font-size:12px;color:var(--text-muted)">${user.email}</div>
      </div>
      <div style="padding:8px">
        <a href="/pages/settings.html" class="dropdown-item"><i class="fa-solid fa-user"></i> Profile</a>
        <a href="/pages/settings.html" class="dropdown-item"><i class="fa-solid fa-gear"></i> Settings</a>
        <hr style="border:none;border-top:1px solid var(--border);margin:4px 0">
        <a href="#" class="dropdown-item" id="logoutBtn"><i class="fa-solid fa-right-from-bracket"></i> Logout</a>
      </div>`;
    wrapper.appendChild(menu);

    profileAvatar.addEventListener('click', e => {
      e.stopPropagation();
      menu.classList.toggle('open');
    });

    document.getElementById('logoutBtn').addEventListener('click', e => {
      e.preventDefault();
      Session.clear();
      window.location.href = '/pages/login.html';
    });
  }

  // Load notifications
  if (notifBell && notifDropdown) {
    import('./notifications.js').then(mod => {
      mod.loadNotifications('notifList', 'notifBadge');
      const markAllBtn = document.getElementById('markAllRead');
      if (markAllBtn) markAllBtn.addEventListener('click', mod.markAllNotificationsRead);
    });
  }

  // Close dropdowns on outside click
  document.addEventListener('click', e => {
    document.querySelectorAll('.dropdown-panel.open').forEach(panel => {
      if (!panel.closest('.dropdown-wrapper')?.contains(e.target)) {
        panel.classList.remove('open');
      }
    });
  });
}

// ─── Guard — redirect to login if not authenticated ──────
export function authGuard() {
  if (!Session.isLoggedIn()) {
    window.location.href = '/pages/login.html';
    return false;
  }
  return true;
}

// ─── Sanitize HTML (XSS prevention) ─────────────────────
export function sanitize(str) {
  const div = document.createElement('div');
  div.appendChild(document.createTextNode(String(str)));
  return div.innerHTML;
}

// ─── Format file size ────────────────────────────────────
export function formatSize(bytes) {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B','KB','MB','GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
}

// ─── File icon by type ───────────────────────────────────
export function fileIconClass(fileName = '') {
  const ext = fileName.split('.').pop().toLowerCase();
  const map = { pdf: 'pdf', doc: 'docx', docx: 'docx', png: 'img', jpg: 'img', jpeg: 'img', gif: 'img', zip: 'zip', rar: 'zip' };
  return map[ext] || 'other';
}
export function fileIconFA(fileName = '') {
  const ext = fileName.split('.').pop().toLowerCase();
  const map = { pdf: 'fa-file-pdf', doc: 'fa-file-word', docx: 'fa-file-word', png: 'fa-file-image', jpg: 'fa-file-image', jpeg: 'fa-file-image', zip: 'fa-file-zipper', rar: 'fa-file-zipper', xlsx: 'fa-file-excel', xls: 'fa-file-excel' };
  return `fa-solid ${map[ext] || 'fa-file'}`;
}

// ─── Skeleton HTML ───────────────────────────────────────
export function skeletonCards(count = 4) {
  return Array.from({ length: count }, () =>
    `<div class="card"><div class="card-body"><div class="skeleton skeleton-text w-60"></div><div class="skeleton skeleton-text"></div><div class="skeleton skeleton-text w-40"></div></div></div>`
  ).join('');
}
