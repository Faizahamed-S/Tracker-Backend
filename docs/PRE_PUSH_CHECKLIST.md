# Pre-push verification checklist

Run before pushing to Railway / production.

## Setup

- Start app with prod profile and Railway DB (or staging), e.g. `SPRING_PROFILES_ACTIVE=prod` with `PGHOST`, `PGPORT`, etc. set.
- Optional one-time backfill: `applyzap.backfill.application-timestamps=true` for one startup, then set back to `false`.

## Board (existing users)

1. `GET /board/applications` without `sort` — same count as before, HTTP 200, no 500.
2. `GET /board/applications?sort=added_desc` — ordered list after backfill.
3. `GET /board/applications?sort=invalid` — HTTP 400 with `message`.
4. `PATCH /board/applications/{id}` with status change — response includes `statusUpdatedAt` newer than before.
5. `GET /board/applications?referral=true` — only referral rows.

## Analytics

6. `GET /api/analytics/dashboard` — existing fields unchanged; `summary.referral_count` and `summary.tailored_count` present.

## Create + groups

7. `POST /board/applications` without `groupIds` — 201, body `{ application, groupResults: [] }`.
8. `POST /board/applications` with invalid `groupIds` — personal app still created; `groupResults` shows failure.
9. User who never calls `/api/groups/**` — board-only flows unchanged.

## Invites

10. Invite email already a member — HTTP 409 with `message`, not 500.
11. `GET /api/groups/invites/{token}` — includes `groupId` in JSON.

## Health

12. `GET /actuator/health` — UP.
