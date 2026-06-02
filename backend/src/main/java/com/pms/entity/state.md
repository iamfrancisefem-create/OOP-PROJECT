# State — Tue Jun 02 2026

## Goal
- Admin invite-by-email/link system (done)
- User management enhancements: delete endpoint, createdBy tracking, new admin empty state (done)
- Mobile-friendly messages sidebar fix (done)
- Fix user deletion to handle FK constraints (done)
- Admin isolation: each admin only sees users they created (done)
- Email delivery via SendGrid HTTPS API (done)
- Create User generates link + welcome email (done)
- Invite link always available in pending invites (done)
- Register page pre-fills email from invite token (done)

## Done
### Invite system
- `PlatformInvitation` entity, service, controller (`/api/v1/invites`), invite registration flow, invite UI in user-management.html (modal, pending invites, copy link, cancel).
- `@Transactional(readOnly=true)` on `getPendingInvites` to fix `LazyInitializationException`.
- V3 Flyway migration: creates `platform_invitations` table.

### User management
- `DELETE /api/v1/users/{id}` endpoint with role-clear before delete.
- `createdBy` self-referencing field on `User` entity.
- New admins see empty user list until they create their first user (checks `GET /api/v1/users/created-by-me/count`).
- V4+V5 Flyway migrations for `created_by_id` column and backfill.
- **User deletion FK fix**: `UserServiceImpl.deleteUser()` now cleans up related data via native SQL (SET NULL on projects, teams, comments, file_uploads, reports, tasks, platform_invitations; DELETE messages) before removing the user.
- V6 Flyway migration: makes FK columns nullable (`ALTER COLUMN ... DROP NOT NULL`).

### Mobile
- Mobile messages sidebar fix: `classList.add('open')` on page load.

### Admin isolation & email
- `UserServiceImpl.getAllUsers()` now filters by `createdBy = currentUser` — each admin only sees their own created users.
- `UserRepository.findByCreatedBy(User, Pageable)` added.
- `EmailService` interface + `EmailServiceImpl` using SendGrid HTTPS API (no SMTP, works on all Railway plans).
- `EmailServiceImpl.sendEmail()` runs async in background thread, always logs to console as fallback.
- `PlatformInvitationServiceImpl.sendInviteEmail()` uses `EmailService`.
- `AuthServiceImpl` uses `EmailService` with proper frontend URLs.

### Create User link
- `UserServiceImpl.createUser()` now sends a welcome email with login link.
- `UserResponse.setupLink` field added — login link returned in API response.
- Frontend `handleCreateUser()` displays the login link in the success toast.

### Invite link fixes
- `getPendingInvites()`, `acceptInvite()`, `validateInvite()` now all include the invite link in responses (was `null` before).
- Register page fetches invite details via `InviteAPI.validate()` and pre-fills the email field (read-only).

### Email provider switch
- Gmail SMTP blocked on Railway free plan → switched to SendGrid HTTPS API.
- Env vars: `SENDGRID_API_KEY` (required), `EMAIL_FROM` (verified sender, required).
- Configurable `from` address via `EMAIL_FROM` env var.

## In Progress
- (none)

## Blocked
- (none)

## Key Details
- **Backend**: `https://oop-project-production.up.railway.app`
- **Frontend**: `https://oop-project-tau.vercel.app`
- **Remaining users in DB**: id=17 Aboy (`efemfrancis52@gmail.com`, ADMIN), id=18 tempadmin123@test.com
- **Git**: on branch `main`, all pushed to origin
- `spring.jpa.hibernate.ddl-auto=validate` — Flyway creates all tables/migrations.
- New registrations get `ADMIN` role.
- Vercel proxy rewrites `/api/v1/*` to Railway.
- `createdBy` tracking: if `countByCreatedBy(currentUser) == 0` and not grandfathered (V5 set `created_by_id = id`), show empty state
- **SendGrid**: HTTPS API (not SMTP) — works on all Railway plans. Set `SENDGRID_API_KEY` and `EMAIL_FROM` env vars.