# Seller Centre Visibility Fix Plan

## Overview

Fix the front-end bug where an authenticated account that already owns a seller profile still loses access to Seller Centre UI and `/seller` routes when `auth.user.role` remains `BUYER`.

Goal: keep the change minimal, safe, and localized to seller entry points without refactoring the broader auth model.

## Root Strategy

- Stop treating `auth.user.role` as the only source of truth for seller entry.
- Keep `auth.user.role` as-is for the wider app.
- For seller-specific entry points, use seller-profile existence as the deciding signal.
- Let `/seller` stay behind authentication, then let seller-profile check decide between seller workspace vs onboarding.

## Requirements

### Functional

1. If a logged-in user already has a seller profile, the navbar must show `Seller Centre` even when `auth.user.role === 'BUYER'`.
2. A logged-in user with a seller profile must be able to enter `/seller`.
3. A logged-in user without a seller profile should still be redirected to `/seller/onboarding`.
4. Unauthenticated behavior should remain unchanged.

### Non-Functional

1. Prefer the smallest diff possible.
2. Avoid global auth/store refactors.
3. Preserve existing seller onboarding flow.

## Architecture

- `auth.user.role` remains the coarse auth role.
- `sellerService.getMySellerProfile()` becomes the seller-entry truth for:
  - navbar Seller Centre visibility/link target
  - `/seller` access after login
- `ProtectedRoute` should only enforce login for seller routes in this fix path.
- `SellerGuard` remains the route-level seller-profile gate.

## Implementation Steps

1. Update seller route gating in `frontend/hypermall-web/src/routes/SellerRoutes.tsx`:
   - remove `requiredRole="SELLER"` from the `/seller` route wrapper
   - keep `ProtectedRoute` for authentication only
   - keep `SellerGuard` as the seller-profile/onboarding decision point
2. Update navbar visibility in `frontend/hypermall-web/src/components/layout/Header/index.tsx`:
   - replace the current `user?.role === 'SELLER'` / `user?.role !== 'BUYER'` checks for Seller Centre with a seller-profile-aware check
   - fetch seller profile only for authenticated users when needed
   - show `/seller` when profile exists; keep current non-authenticated CTA behavior unchanged unless implementation proves a safer existing path
3. Make `SellerGuard` the explicit source of truth in `frontend/hypermall-web/src/components/seller/SellerGuard.tsx`:
   - keep current profile fetch behavior
   - no behavioral refactor unless needed for consistency with step 2
   - optionally tighten comments/naming only if it helps clarify seller-profile-based gating
4. Leave `frontend/hypermall-web/src/routes/ProtectedRoute.tsx` unchanged unless step 1 reveals a hidden dependency; if changed at all, keep it generic and avoid seller-specific branching there

## Files to Modify/Create/Delete

### Modify

- `frontend/hypermall-web/src/components/layout/Header/index.tsx`
- `frontend/hypermall-web/src/routes/SellerRoutes.tsx`
- `frontend/hypermall-web/src/components/seller/SellerGuard.tsx` (only if needed for consistency or minor clarification)

### Likely Unchanged

- `frontend/hypermall-web/src/routes/ProtectedRoute.tsx`

### Create

- none preferred; only add a tiny shared hook/helper if duplication becomes awkward during implementation

### Delete

- none

## Recommended Edit Order

1. `frontend/hypermall-web/src/routes/SellerRoutes.tsx`
2. `frontend/hypermall-web/src/components/layout/Header/index.tsx`
3. `frontend/hypermall-web/src/components/seller/SellerGuard.tsx` if needed
4. `frontend/hypermall-web/src/routes/ProtectedRoute.tsx` only as fallback

## Testing Strategy

- Manual verification first:
  1. login with account whose auth role is `BUYER` but already has seller profile
  2. confirm `Seller Centre` appears in desktop + mobile navbar and account menu if applicable
  3. open `/seller` directly and confirm seller dashboard loads
  4. login with authenticated non-seller account and confirm `/seller` redirects to onboarding
  5. confirm unauthenticated `/seller` still redirects to `/login`
- If nearby tests already exist, add a narrow regression test around route gating or header conditional rendering; do not introduce a new broad test harness for this fix alone

## Security Considerations

- Do not elevate auth role client-side.
- Do not infer seller access from local UI flags alone.
- Keep seller access dependent on authenticated API response from `getMySellerProfile()`.

## Performance Considerations

- Prefer one lightweight seller-profile check only on authenticated seller-entry surfaces.
- Avoid adding global polling or app-wide seller profile bootstrap for this bug fix.
- If duplicate fetches between Header and SellerGuard become noticeable, treat that as follow-up work, not part of this minimal fix unless implementation is trivial.

## Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Header and SellerGuard duplicate the same API call | Medium | Low | Accept for minimal diff; extract tiny shared helper only if duplication becomes messy |
| `getMySellerProfile()` currently maps all errors to `null`, so transient API failures may hide Seller Centre or misroute to onboarding | Medium | Medium | Preserve existing behavior for minimal fix; document as follow-up if seen during verification |
| Other parts of UI still rely on `auth.user.role === 'SELLER'` and remain inconsistent | Medium | Low | Limit this fix to reported surfaces; note any extra occurrences discovered during implementation |
| Removing `requiredRole="SELLER"` could expose seller layout entry to any logged-in user | Low | Medium | `SellerGuard` still blocks non-sellers and redirects to onboarding |

## Edge Cases

- Authenticated `BUYER` with seller profile: must see Seller Centre and access `/seller`
- Authenticated `BUYER` without seller profile: may see seller CTA only if current UX intentionally allows onboarding entry; `/seller` should land on onboarding
- Authenticated `SELLER` with seller profile: no behavior regression
- Authenticated user during seller-profile fetch loading: avoid flicker or broken link state as much as possible
- Seller profile API temporary failure: UI may fall back to hidden CTA or onboarding redirect under current service behavior

## TODO Tasks

- [ ] Remove hard role gate from `/seller` route usage
- [ ] Make Header Seller Centre visibility depend on seller profile presence instead of role alone
- [ ] Keep SellerGuard as seller-profile-based route decision layer
- [ ] Verify desktop navbar, mobile menu, and direct `/seller` navigation for affected account types
