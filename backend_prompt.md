



You are a **senior Java backend architect, software engineer, database engineer, DevOps engineer, and cybersecurity specialist with 15+ years of experience building enterprise SaaS systems**. Your task is to design and implement the complete backend of a **Project Management System Application** using modern industry standards.

The application is a **web-based project management system** whose frontend will later be developed using **HTML, CSS, and JavaScript**. Your responsibility is ONLY the backend.

The backend must be **production-ready**, **scalable**, **maintainable**, **secure**, **modular**, and follow **clean architecture principles**.


# PROJECT OVERVIEW

The system allows users to:

* Register and login
* Manage profiles
* Create teams
* Create projects
* Assign projects
* Create milestones
* Create tasks
* Assign tasks
* Track task progress
* Manage team members
* Send messages
* Upload files
* Receive notifications
* View dashboards
* Generate reports
* Track workload
* Track project completion
* Comment on tasks
* Monitor deadlines
* Manage permissions and roles

User roles include:

```txt
ADMIN
PROJECT_MANAGER
TEAM_LEADER
PRODUCT_OWNER
TEAM_MEMBER
```

The system should support future scaling into a mobile application and multi-tenant SaaS platform.

---

# REQUIRED TECH STACK

Backend stack:

```txt
Java 21 (or Java 17 LTS)

Spring Boot

Spring Security

JWT Authentication

Spring Data JPA

Hibernate

PostgreSQL

Maven

Swagger/OpenAPI

JUnit + Mockito

Lombok

MapStruct

Validation API

WebSocket

Docker

Redis (optional caching)

Flyway/Liquibase migrations

SLF4J Logging
```

---

# DEVELOPMENT RULES

Generate code following:

### SOLID principles

Apply:

```txt
Single Responsibility

Open Closed

Liskov

Interface Segregation

Dependency Injection
```

---

### Clean Architecture

Separate:

```txt
Controller Layer

Service Layer

Repository Layer

DTO Layer

Entity Layer

Security Layer

Exception Layer

Config Layer
```

Avoid putting business logic in controllers.

---

### Naming Convention

Use:

```txt
camelCase → variables

PascalCase → classes

UPPERCASE → constants
```

---

### Code Quality

Generate:

* Readable code
* Reusable code
* Scalable code
* Well commented code
* Production structure
* Error handling
* Logging

Never generate quick prototypes.

---

# REQUIRED PROJECT STRUCTURE

Generate full Spring Boot structure:

```txt
src/main/java/com/pms/

config/
controller/
dto/
entity/
repository/
security/
service/
service/impl/
exception/
mapper/
scheduler/
util/
websocket/
audit/
storage/

resources/

application.yml

sql/

tests/
```

Generate every folder with explanation.

---

# DATABASE DESIGN REQUIREMENTS

Generate PostgreSQL schema for all entities.

Required entities:

### User

Fields:

```txt
id
fullName
email
password
phone
profileImage
enabled
createdAt
updatedAt
```

Relationships:

```txt
User → Roles
User → Tasks
User → Notifications
User → Messages
User → Teams
```

---

### Role

Roles:

```txt
ADMIN

PROJECT_MANAGER

TEAM_MEMBER

TEAM_LEADER

PRODUCT_OWNER
```

---

### Team

---

### TeamMember

---

### Project

Include:

```txt
status

priority

progress

startDate

endDate
```

---

### Milestone

---

### Task

Include:

Status:

```txt
TODO

IN_PROGRESS

TESTING

DONE
```

Priority:

```txt
LOW

MEDIUM

HIGH

URGENT
```

---

### Comment

---

### Message

---

### Notification

---

### FileUpload

---

### Report

---

### AuditLog

Track:

```txt
user action

IP address

timestamp

operation
```

---

Generate:

1. ER diagram (text form)
2. SQL schema
3. Constraints
4. Foreign keys
5. Indexes
6. Cascading rules

---

# SECURITY REQUIREMENTS

Implement enterprise security.

Generate:

## Authentication

Use:

```txt
JWT Access Token

Refresh Token

BCrypt Password Hashing
```

---

## Authorization

Role-based access control.

Examples:

```txt
ADMIN → full access

MEMBER → limited access
```

---

## Security Features

Implement:

```txt
CSRF protection

CORS config

Rate limiting

Input sanitization

Password encryption

Email verification

Account lock after failed attempts

Refresh token rotation
```

---

Generate:

```txt
SecurityConfig.java

JwtFilter.java

JwtService.java

CustomUserDetailsService.java
```

---

# API DESIGN REQUIREMENTS

Generate complete REST APIs.

Include:

Authentication:

```http
POST /auth/register

POST /auth/login

POST /auth/logout

POST /auth/forgot-password

POST /auth/reset-password
```

Users:

```http
GET /users

PUT /users/{id}
```

Projects:

```http
POST /projects

GET /projects

PUT /projects/{id}

DELETE /projects/{id}
```

Tasks:

```http
POST /tasks

PATCH /tasks/status
```

Generate all endpoints.

For every endpoint provide:

```txt
Request body

Response body

Validation

HTTP codes

Example JSON
```

---

# FILE MANAGEMENT

Implement:

```txt
Upload files

Download files

Delete files

Versioning

Storage paths
```

Support:

```txt
pdf

docx

jpg

png

zip
```

---

# DASHBOARD MODULE

Generate backend analytics:

Examples:

```txt
Total projects

Completed tasks

Pending tasks

Overdue tasks

Productivity %

Team performance

Recent activities
```

Generate optimized SQL queries.

---

# NOTIFICATION SYSTEM

Implement:

```txt
In-app notifications

Email notifications

Deadline reminders

Task assignment alerts
```

Use scheduler:

```java
@Scheduled
```

---

# MESSAGING SYSTEM

Generate:

Real-time chat using:

```txt
WebSocket
```

Support:

```txt
send

receive

read status

timestamps
```

---

# REPORTING MODULE

Generate reports for:

```txt
Project progress

Task completion

User productivity

Deadlines

Team performance
```

Support export:

```txt
PDF

Excel
```

---

# TESTING REQUIREMENTS

Generate:

```txt
Unit tests

Integration tests

Repository tests

Controller tests

Security tests
```

Use:

```txt
JUnit

Mockito
```

Target:

```txt
80%+ coverage
```

---

# DOCUMENTATION REQUIREMENTS

Generate:

Swagger documentation for all APIs.

Include:

```txt
Endpoint description

Request examples

Response examples

Authorization requirements
```

---

# DEPLOYMENT REQUIREMENTS

Prepare backend for deployment.

Generate:

Docker:

```txt
Dockerfile

docker-compose.yml
```

Environment configs:

```txt
dev

test

prod
```

Deployment ready for:

```txt
Render

Railway

AWS

DigitalOcean
```

---

# OUTPUT FORMAT REQUIREMENT

Build the backend sequentially and do NOT skip steps.

Generate in this order:

```txt
1. Architecture overview

2. Folder structure

3. Database design

4. Entities

5. SQL schema

6. DTOs

7. Repositories

8. Services

9. Security configuration

10. Controllers

11. APIs

12. Validation

13. Exception handling

14. Notifications

15. WebSocket

16. Reports

17. Testing

18. Docker

19. Deployment

20. Final project tree
```

For every section explain WHY the implementation choice was made.

Generate production-quality code only.


