# Phase 03: Frontend Auth and Routing Consolidation

## Context links
- `frontend/hypermall-web/src/App.tsx`
- `frontend/hypermall-web/src/routes/index.tsx`
- `frontend/hypermall-web/src/routes/AdminRoutes.tsx`
- `frontend/hypermall-web/src/routes/ProtectedRoute.tsx`
- `frontend/hypermall-web/src/services/api.service.ts`
- `frontend/hypermall-web/src/services/auth.service.ts`
- `frontend/hypermall-web/src/store/index.ts`
- `plans/2026-03-13-code-quality-fix/research/260315-react-vite-spring-gateway-frontend-practices.md`

## Overview with date/priority/statuses
- Date: 2026-03-15
- Priority: High
- Plan status: Drafted
- Implementation status: Not started

## Key Insights
- Route ownership is split across `App.tsx`, `routes/index.tsx`, and `routes/AdminRoutes.tsx`.
- `ProtectedRoute.tsx` exists but is not wired into admin or authenticated flows.
- `api.service.ts` refresh logic is single-request only; concurrent 401s can race and leave stale auth state.
- Auth storage writes are spread across service, layout, page, and interceptor code; Redux serializable check is globally disabled.

## Requirements
- Centralize route composition and guard placement by layout boundary.
- Centralize token storage, refresh locking, logout cleanup, and user-state hydration.
- Remove dead or duplicate state tooling unless proven necessary.

## Architecture
- Use one route manifest with public, authenticated, and admin shells.
- Keep one auth/session module responsible for browser storage and refresh orchestration; pages dispatch actions, never touch storage directly.
- Re-enable targeted Redux serializability checks or document narrow exceptions only.

## Related code files
- `frontend/hypermall-web/src/App.tsx`
- `frontend/hypermall-web/src/routes/index.tsx`
- `frontend/hypermall-web/src/routes/AdminRoutes.tsx`
- `frontend/hypermall-web/src/routes/ProtectedRoute.tsx`
- `frontend/hypermall-web/src/services/api.service.ts`
- `frontend/hypermall-web/src/services/auth.service.ts`
- `frontend/hypermall-web/src/components/admin/AdminLayout.tsx`
- `frontend/hypermall-web/src/pages/Profile/index.tsx`
- `frontend/hypermall-web/src/store/index.ts`
- `frontend/hypermall-web/package.json`

## Implementation Steps
1. Collapse duplicated route definitions into one route tree with nested layouts and guard wrappers.
2. Wire `ProtectedRoute` or equivalent guard at authenticated/admin layout boundaries, not ad hoc pages.
3. Refactor auth storage into one module that owns read/write/clear semantics and stale-session cleanup.
4. Add refresh queue/lock in transport layer so only one refresh request runs at a time and all pending requests replay safely.
5. Revisit global `serializableCheck: false`, remove `zustand` if unused, and add regression tests for login/logout/refresh/guard flows.

## Todo list
- [ ] Define final route ownership model
- [ ] Guard admin and authenticated areas consistently
- [ ] Centralize storage mutations
- [ ] Add refresh lock/queue and fail-closed logout path
- [ ] Re-enable targeted Redux middleware checks
- [ ] Remove unused `zustand` if no consumer exists

## Success Criteria
- Admin and authenticated routes are consistently guarded.
- Concurrent 401 responses trigger one refresh flow only.
- No UI component writes auth storage directly.
- Route tree has one canonical source.

## Risk Assessment
- Main risk is user-session churn during refresh refactor.
- Mitigate with backward-compatible storage keys and focused auth regression tests.

## Security Considerations
- Clear access token, refresh token, cached user, and derived auth state on refresh failure.
- Avoid leaking gateway or service URLs into page code while consolidating routes.

## Next steps
- Feed final frontend auth contract into Phase 4 smoke tests and release checklist.

## Unresolved Questions
- Is Redux the intended long-term store for auth, or should auth become mostly service-owned with minimal global state?
- Does backend support refresh-token rotation, or only access-token renewal?
