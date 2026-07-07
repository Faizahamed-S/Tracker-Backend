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
9. `PATCH /board/applications/{id}` with `groupIds` — 200, response includes `groupResults`; omitting `groupIds` leaves response unchanged (no `groupResults`).
10. User who never calls `/api/groups/**` — board-only flows unchanged.
11. `DELETE /api/groups/{id}` as owner (group with jobs/members if possible) — HTTP 204; group absent from `GET /api/groups`.
12. `PATCH /api/groups/{id}` as owner with `{ "name": "Renamed" }` — HTTP 200; `GET /api/groups` shows updated name.

## Invites

10. Invite email already a member — HTTP 409 with `message`, not 500.
11. `GET /api/groups/invites/{token}` — includes `groupId` in JSON.

## Health

12. `GET /actuator/health` — UP.

## Referral Base CRM

13. `GET /api/referrals` — `[]` for new user, HTTP 200.
14. `POST /api/referrals` with `{ "name": "Test Contact" }` — 201; `GET /api/referrals` lists it.
15. `PUT /api/referrals/field-template` with `{ "fields": [{ "key": "met_at", "label": "Met At", "order": 0 }] }` — `GET /api/referrals/field-template` returns same fields; other `trackerConfig` keys unchanged if present.
16. `POST /board/applications` with `referralContactId` set — response `application.referral` is `true`, `referralContactId` matches; `GET /api/referrals/{id}` shows `associatedApplications`.
17. `PATCH /board/applications/{id}` with `referral: false` — `referralContactId` cleared on GET list.
18. `DELETE /api/referrals/{id}` — linked apps have `referral=false` and no `referralContactId`.
19. Legacy app with `referral=true` only (no contact) — still returned by `GET /board/applications?referral=true`; `referral_count` unchanged on dashboard.
