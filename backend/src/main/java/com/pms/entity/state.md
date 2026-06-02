# State — Tue Jun 02 2026

## Goal
- Admin invite-by-email/link system (done)
- User management enhancements: delete endpoint, createdBy tracking, new admin empty state (done)
- Mobile-friendly messages sidebar fix (done)
- Fix user deletion to handle FK constraints (done)

## Done
- Invite system: `PlatformInvitation` entity, service, controller (`/api/v1/invites`), invite registration flow, invite UI in user-management.html (modal, pending invites, copy link, cancel).
- `@Transactional(readOnly=true)` on `getPendingInvites` to fix `LazyInitializationException`.
- V3 Flyway migration: creates `platform_invitations` table.
- `DELETE /api/v1/users/{id}` endpoint with role-clear before delete.
- `createdBy` self-referencing field on `User` entity.
- New admins see empty user list until they create their first user (checks `GET /api/v1/users/created-by-me/count`).
- V4+V5 Flyway migrations for `created_by_id` column and backfill.
- Mobile messages sidebar fix: `classList.add('open')` on page load.
- **User deletion FK fix**: `UserServiceImpl.deleteUser()` now cleans up related data via native SQL (SET NULL on projects, teams, comments, file_uploads, reports, tasks, platform_invitations; DELETE messages) before removing the user.
- V6 Flyway migration: makes FK columns nullable (`ALTER COLUMN ... DROP NOT NULL`).
- Delete users 5 and 16 from production DB (successful via temp admin `tempadmin123@test.com`).

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