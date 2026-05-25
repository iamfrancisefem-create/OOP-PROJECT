import { AuthAPI } from './api.js';
import { Session, toast, showAlert, hideAlert, setLoading, validateForm } from './utils.js';

export async function handleLogin(formEl, emailEl, passwordEl, btnEl, alertEl, rememberEl) {
  hideAlert(alertEl);
  if (!validateForm({
    [`#${emailEl.id}`]: { required: true, email: true },
    [`#${passwordEl.id}`]: { required: true, min: 6 },
  })) return;
  setLoading(btnEl, true);
  try {
    const res = await AuthAPI.login({ email: emailEl.value.trim(), password: passwordEl.value });
    const { token, refreshToken, id, email, fullName, phone, roles } = res.data.data;
    Session.set(token, refreshToken, { id, email, fullName, phone, roles });
    if (rememberEl?.checked) {
      localStorage.setItem('pms_remember_email', emailEl.value);
    }
    toast(`Welcome back, ${fullName || 'User'}!`, 'success');
    setTimeout(() => window.location.href = 'dashboard.html', 500);
  } catch (err) {
    showAlert(alertEl, err.response?.data?.message || 'Invalid email or password.', 'error');
  } finally {
    setLoading(btnEl, false);
  }
}

export async function handleRegister(formEl, btnEl, alertEl, fields) {
  hideAlert(alertEl);
  if (!validateForm({
    '#regName': { required: true },
    '#regEmail': { required: true, email: true },
    '#regPassword': { required: true, min: 6 },
    '#regConfirm': { required: true, match: '#regPassword' },
  })) return;
  setLoading(btnEl, true);
  try {
    const res = await AuthAPI.register({
      fullName: fields.name,
      email: fields.email,
      phone: fields.phone,
      password: fields.password,
      role: fields.role,
    });
    const { token, refreshToken, id, email, fullName, phone, roles } = res.data.data;
    Session.set(token, refreshToken, { id, email, fullName, phone, roles });
    toast('Account created successfully!', 'success');
    setTimeout(() => window.location.href = 'dashboard.html', 500);
  } catch (err) {
    showAlert(alertEl, err.response?.data?.message || 'Registration failed.', 'error');
  } finally {
    setLoading(btnEl, false);
  }
}

export async function handleForgotPassword(emailEl, btnEl, alertEl) {
  hideAlert(alertEl);
  if (!validateForm({ '#fpEmail': { required: true, email: true } })) return;
  setLoading(btnEl, true);
  try {
    await AuthAPI.forgotPassword(emailEl.value.trim());
    showAlert(alertEl, 'If that email exists, a reset link has been sent.', 'success');
  } catch (err) {
    showAlert(alertEl, 'Failed to send reset email. Try again.', 'error');
  } finally {
    setLoading(btnEl, false);
  }
}

export function togglePasswordVisibility(inputEl, iconEl) {
  const isPass = inputEl.type === 'password';
  inputEl.type = isPass ? 'text' : 'password';
  if (iconEl) {
    iconEl.className = isPass ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye';
  }
}
