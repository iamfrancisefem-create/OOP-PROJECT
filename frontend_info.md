

# MASTER FRONTEND SPECIFICATION

The frontend must be developed as a **responsive web application** using:

```txt
HTML5
CSS3
JavaScript (ES6+)
```

Recommended additions:

```txt
Bootstrap 5 (UI components)

Chart.js (dashboard charts)

Axios (API requests)

SweetAlert2 (alerts)

FullCalendar.js (task deadlines)

Socket.IO client or WebSocket API

Font Awesome (icons)

DataTables.js (tables)

Quill.js (rich text editor)

Toastify (notifications)
```

---

# 1. FRONTEND ARCHITECTURE

Use modular structure:

```txt
Browser
 ↓

UI Components

 ↓

State Management

 ↓

API Layer

 ↓

Backend REST API

 ↓

Database
```

Frontend responsibilities:

* Authentication
* Form validation
* API communication
* Dashboard rendering
* State updates
* Notifications
* File uploads
* Chat
* Reports

---

# 2. PROJECT FOLDER STRUCTURE

Organize frontend:

```txt
frontend/

│

├── index.html

├── login.html

├── register.html

├── dashboard.html

├── projects.html

├── project-details.html

├── tasks.html

├── teams.html

├── reports.html

├── messages.html

├── settings.html

│

├── assets/

│      ├── css/

│      │      style.css

│      │      dashboard.css

│      │      auth.css

│      │      forms.css

│      │      tables.css

│      │      responsive.css

│      │

│      ├── js/

│      │      auth.js

│      │      dashboard.js

│      │      api.js

│      │      tasks.js

│      │      projects.js

│      │      reports.js

│      │      websocket.js

│      │      notifications.js

│      │

│      ├── images/

│      └── icons/

│

├── components/

│      navbar.html

│      sidebar.html

│      footer.html

│      modals.html

│

├── utils/

│      constants.js

│      validators.js

│      helpers.js

│

└── uploads/
```

---

# 3. PAGE REQUIREMENTS

Generate UI pages.

---

## Authentication Pages

Need:

### Login Page

Fields:

```txt
Email

Password

Remember me

Forgot password
```

Buttons:

```txt
Login

Register
```

---

### Register Page

Fields:

```txt
Full name

Email

Phone

Password

Confirm password

Role
```

---

### Forgot Password

---

### Reset Password

---

# 4. DASHBOARD PAGE

Dashboard must display:

Cards:

```txt
Total Projects

Completed Tasks

Pending Tasks

Overdue Tasks

Notifications
```

Charts:

Use Chart.js

Generate:

```txt
Task Completion Chart

Team Productivity Chart

Project Progress Chart

Weekly Activity
```

Tables:

```txt
Recent Tasks

Recent Projects

Recent Messages
```

---

# 5. PROJECT MANAGEMENT UI

Pages:

---

### Create Project

Form:

```txt
Title

Description

Priority

Start Date

End Date

Assign Team

Upload files
```

---

### Edit Project

---

### Delete Project

---

### Project Details

Show:

```txt
Progress

Milestones

Tasks

Files

Members
```

---

# 6. TASK MANAGEMENT UI

Need:

Kanban board style:

Columns:

```txt
TODO

IN_PROGRESS

TESTING

DONE
```

Allow:

```txt
Drag and Drop

Assign member

Deadline

Priority

Comments
```

---

# 7. TEAM MANAGEMENT UI

Pages:

Create Team

Add Member

Remove Member

Assign Role

Display:

```txt
Member Name

Role

Tasks Assigned

Performance
```

---

# 8. CHAT / MESSAGING UI

Need:

Real-time chat.

Features:

```txt
Send message

Receive message

Read receipts

Online status

Attachments
```

Layout:

```txt
Sidebar → Contacts

Main panel → Chat

Bottom → Input
```

---

# 9. NOTIFICATION UI

Need dropdown:

Examples:

```txt
Task assigned

Deadline approaching

Message received
```

---

# 10. REPORTS UI

Generate:

Tables:

```txt
Project Reports

Performance Reports

Task Reports
```

Buttons:

```txt
Export PDF

Export Excel
```

---

# 11. FILE MANAGEMENT UI

Support:

Upload:

```txt
PDF

DOCX

PNG

ZIP
```

Need:

```txt
Progress bar

Preview

Download

Delete
```

---

# 12. SETTINGS PAGE

Allow:

Update:

```txt
Profile picture

Name

Email

Password

Theme

Notifications
```

---

# 13. RESPONSIVE DESIGN REQUIREMENTS

Must support:

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

---

# 14. DESIGN SYSTEM

Color palette example:

Primary:

```txt
#2563EB
```

Success:

```txt
#16A34A
```

Danger:

```txt
#DC2626
```

Warning:

```txt
#F59E0B
```

Background:

```txt
#F8FAFC
```

---

Fonts:

Use:

```txt
Poppins

Inter

Roboto
```

---

# 15. COMPONENT LIBRARY

Reusable components:

Create:

```txt
Buttons

Cards

Tables

Modals

Inputs

Dropdowns

Sidebar

Navbar

Loader

Pagination

Alerts
```

---

# 16. FORM VALIDATION

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

Number

Special char

8+ length
```

---

# 17. API LAYER DESIGN

Create central API manager:

Example:

api.js

```javascript
const API =
"https://localhost:8080/api/v1";
```

Functions:

```javascript
login()

register()

createProject()

getProjects()

createTask()
```

Use Axios.

---

# 18. JWT STORAGE

After login:

Store:

```txt
Access Token

Refresh Token
```

Use:

Prefer:

```txt
HttpOnly Cookies
```

Avoid unsafe localStorage.

---

# 19. ERROR HANDLING

Display:

```txt
401 Unauthorized

403 Forbidden

500 Server Error
```

Use Toast notifications.

---

# 20. LOADING STATES

Need:

```txt
Skeleton loaders

Spinners

Progress bars
```

---

# 21. SEARCH & FILTERS

Implement:

Search:

```txt
Projects

Tasks

Users
```

Filters:

```txt
Priority

Status

Date
```

---

# 22. ACCESS CONTROL

Hide UI based on role.

Example:

ADMIN:

See everything

MEMBER:

Limited menus

---

# 23. FRONTEND SECURITY

Implement:

```txt
XSS prevention

Sanitize inputs

CSRF awareness

Secure cookies
```

---

# 24. PERFORMANCE OPTIMIZATION

Need:

```txt
Lazy loading

Debouncing

Caching

Pagination
```

---

# 25. TESTING

Generate:

```txt
UI testing

Form testing

API testing

Responsive testing
```

---

# 26. MASTER FRONTEND DEVELOPMENT PROMPT

Use this with AI:

---

**Prompt Start**

Act as a senior frontend architect with 15+ years experience building enterprise SaaS systems.

Develop a complete production-ready frontend for a Project Management System using:

```txt
HTML5
CSS3
JavaScript ES6
Bootstrap
Axios
Chart.js
WebSocket
```

The frontend must integrate with a Spring Boot backend using JWT authentication and REST APIs.

Generate:

1. Folder structure
2. UI architecture
3. Authentication pages
4. Dashboard
5. Projects module
6. Tasks/Kanban board
7. Team management
8. Messaging module
9. Notifications
10. Reports
11. File uploads
12. Settings
13. Reusable components
14. Responsive design
15. API integration
16. JWT handling
17. Validation
18. Error handling
19. Testing
20. Final complete project tree

Generate scalable, secure, production-quality code only.


