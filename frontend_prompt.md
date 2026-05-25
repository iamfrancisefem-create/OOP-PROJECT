

# MASTER PROMPT — FRONTEND DEVELOPMENT (HTML, CSS & JavaScript)

You are a **world-class senior frontend engineer, UI/UX architect, JavaScript engineer, accessibility expert, and SaaS product developer with 15+ years of experience building enterprise dashboards and web applications**.

Your task is to design and develop the complete frontend for a **Project Management System Application**.

The frontend must be:

* Production ready
* Responsive
* Scalable
* Secure
* Modular
* Maintainable
* Fast
* Accessible
* Compatible with REST APIs from a Spring Boot backend
* Suitable for future expansion

Frontend technologies allowed:

```txt
HTML5
CSS3
JavaScript ES6+
```

Optional libraries/plugins:

```txt
Bootstrap 5
Chart.js
Axios
SweetAlert2
Toastify
FullCalendar.js
Font Awesome
DataTables.js
Quill.js
```

Do NOT use React, Angular, Vue, or frameworks unless explicitly requested.

---

# APPLICATION OVERVIEW

The system is a **Project Management System** supporting:

Users can:

```txt
Register
Login
Reset password
Manage profile
Create projects
Edit projects
Delete projects
Create tasks
Assign tasks
Manage teams
Send messages
Upload files
Receive notifications
Track deadlines
View dashboards
Generate reports
Comment on tasks
Monitor progress
Manage permissions
```

User roles:

```txt
ADMIN
PROJECT_MANAGER
TEAM_LEADER
TEAM_MEMBER
PRODUCT_OWNER
```

Frontend must dynamically hide/show features depending on role.

---

# REQUIRED FRONTEND ARCHITECTURE

Use modular architecture:

```txt
UI Layer
↓
Components
↓
Services/API Layer
↓
State Management
↓
Backend REST APIs
```

Separate concerns properly.

Avoid writing all JavaScript in one file.

---

# REQUIRED PROJECT STRUCTURE

Generate full folder architecture:

```txt
frontend/

│

├── index.html

├── pages/

│      login.html
│      register.html
│      dashboard.html
│      projects.html
│      tasks.html
│      teams.html
│      reports.html
│      messages.html
│      settings.html
│
├── assets/

│      css/
│           style.css
│           auth.css
│           dashboard.css
│           forms.css
│           tables.css
│           responsive.css
│
│      js/
│           auth.js
│           dashboard.js
│           api.js
│           projects.js
│           tasks.js
│           teams.js
│           reports.js
│           websocket.js
│           notifications.js
│           utils.js
│
│      images/

│      icons/

├── components/

│      navbar.html
│      sidebar.html
│      footer.html
│      modals.html
│
├── uploads/

└── docs/
```

Explain the purpose of every folder.

---

# DESIGN SYSTEM REQUIREMENTS

Generate a professional SaaS interface.

Define:

## Colors

Primary:

```txt
#2563EB
```

Success:

```txt
#16A34A
```

Warning:

```txt
#F59E0B
```

Danger:

```txt
#DC2626
```

Background:

```txt
#F8FAFC
```

---

## Typography

Use:

```txt
Poppins
Inter
Roboto
```

---

## Spacing

Create consistent spacing system.

Example:

```txt
4px
8px
16px
24px
32px
48px
```

---

Generate:

```txt
CSS variables

Global styles

Theme system

Dark mode support
```

---

# RESPONSIVE DESIGN REQUIREMENTS

Frontend MUST support:

```txt
Desktop

Tablet

Mobile
```

Breakpoints:

```css
1200px
992px
768px
576px
```

Generate mobile-first design.

---

# REQUIRED PAGES

Generate complete UI/UX for:

---

## Authentication

Create:

```txt
Login page

Register page

Forgot password

Reset password
```

Include:

Validation

Loading state

Error messages

Success messages

---

## Dashboard

Dashboard must show:

Cards:

```txt
Total Projects

Completed Tasks

Pending Tasks

Overdue Tasks

Notifications
```

Charts:

Generate using Chart.js:

```txt
Project Progress

Task Completion

Team Productivity

Weekly Activity
```

Tables:

```txt
Recent Projects

Recent Tasks

Recent Messages
```

---

## Project Module

Pages:

```txt
Create Project

Edit Project

Delete Project

Project Details
```

Forms:

```txt
Title

Description

Priority

Deadline

Members

Files
```

---

## Task Module

Create Kanban board:

Columns:

```txt
TODO

IN_PROGRESS

TESTING

DONE
```

Support:

```txt
Drag and drop

Task assignment

Comments

Priority

Deadlines
```

---

## Team Module

Generate:

```txt
Create team

Assign roles

Add member

Remove member
```

Display:

```txt
Performance

Tasks assigned

Role
```

---

## Messaging Module

Create real-time chat UI.

Features:

```txt
Inbox

Chat list

Unread count

Typing indicator

Attachments
```

---

## Notifications

Generate notification dropdown:

Examples:

```txt
Task assigned

Deadline near

New message
```

---

## Reports

Create:

```txt
Performance reports

Task reports

Project reports
```

Include:

```txt
Export PDF

Export Excel
```

---

## Settings

Allow:

```txt
Change password

Profile image

Theme

Notification preferences
```

---

# COMPONENT SYSTEM

Build reusable components:

Generate:

```txt
Buttons

Inputs

Cards

Tables

Alerts

Navbar

Sidebar

Modals

Pagination

Dropdowns

Loaders

Toast notifications
```

Components must be reusable.

---

# FORM VALIDATION REQUIREMENTS

Implement frontend validation.

Examples:

Email:

```javascript
emailRegex
```

Password:

Require:

```txt
Uppercase

Lowercase

Special character

Number

Minimum length
```

Generate:

```txt
Real-time validation

Error labels

Success labels
```

---

# API INTEGRATION REQUIREMENTS

Backend:

Spring Boot REST API

Create centralized API layer:

Example:

api.js

Generate:

```javascript
login()

register()

createProject()

createTask()

fetchDashboard()

uploadFile()
```

Use:

```txt
Axios
Fetch API
```

Prefer Axios.

---

# JWT AUTHENTICATION HANDLING

Handle:

```txt
Access Token

Refresh Token

Session expiration

Auto logout
```

Prefer:

```txt
HttpOnly cookies
```

Generate auth guards.

---

# ERROR HANDLING

Display UI for:

```txt
400

401

403

404

500
```

Use:

```txt
Toast

Alerts

Modals
```

---

# FILE MANAGEMENT UI

Support:

Upload:

```txt
PDF

DOCX

PNG

ZIP
```

Generate:

```txt
Progress bar

Preview

Download

Delete
```

---

# NOTIFICATION SYSTEM

Implement:

```txt
Toast notifications

Dropdown alerts

Unread count
```

---

# WEBSOCKET REQUIREMENTS

Integrate with backend messaging.

Generate:

```txt
Real-time chat

Instant notifications
```

Use:

```javascript
WebSocket API
```

---

# PERFORMANCE REQUIREMENTS

Optimize:

```txt
Lazy loading

Pagination

Debouncing

Caching

Image optimization
```

---

# ACCESSIBILITY REQUIREMENTS

Implement:

```txt
Keyboard navigation

ARIA labels

Semantic HTML

Contrast compliance
```

Follow WCAG standards.

---

# SECURITY REQUIREMENTS

Prevent:

```txt
XSS

Unsafe input rendering

Token leaks
```

Sanitize all inputs.

---

# TESTING REQUIREMENTS

Generate tests for:

```txt
Forms

Authentication

Responsive layout

API calls

UI behavior
```

---

# OUTPUT REQUIREMENT

Generate frontend sequentially in this exact order:

```txt
1. Architecture overview

2. Folder structure

3. Global styles

4. Design system

5. Components

6. Authentication pages

7. Dashboard

8. Projects module

9. Tasks module

10. Teams module

11. Messaging

12. Reports

13. Notifications

14. Settings

15. API layer

16. Authentication handling

17. Validation

18. Error handling

19. Responsiveness

20. Accessibility

21. Testing

22. Final project tree
```

For every section:

Provide:

```txt
Explanation

HTML

CSS

JavaScript

Best practices
```

Generate production-quality code only.

Avoid shortcuts.

Build as if this application will be used by thousands of users.


