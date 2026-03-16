# Phase 05 - QA Perf Accessibility

## Context links
- `docs/code-standards.md`
- `docs/system-architecture.md`
- `docs/project-overview-pdr.md`
- `plans/2026-03-15-hightech-ui-revamp/research/researcher-02-motion-strategy.md`
- `plans/2026-03-15-hightech-ui-revamp/scout/scout-01-frontend-ui-map.md`

## Overview
- Final pass. Kill regressions. Keep the polish. Protect speed, accessibility, and commerce trust.

## Key Insights
- Revamp risk is not just visual mismatch. It is broken cart/auth/menu flows, mobile jank, and low-contrast states.
- Motion and new tokens are where most subtle regressions hide.
- Commerce UI fails hard when perceived polish beats functional clarity.

## Requirements
- Validate storefront and admin on desktop and mobile.
- Validate reduced-motion, keyboard flows, contrast, loading states, and fallback behavior.
- Prove performance is still acceptable for the product goals.

## Architecture
- QA runs against phase boundaries: shell, primitives, motion, admin.
- Validation should combine targeted unit/regression checks, manual interaction sweeps, and visual screenshot review.
- No new tooling unless current stack cannot cover a critical gap.

## Related code files
- `frontend/hypermall-web/src/App.tsx`
- `frontend/hypermall-web/src/main.tsx`
- `frontend/hypermall-web/src/styles/globals.css`
- `frontend/hypermall-web/src/components/layout/MainLayout/index.tsx`
- `frontend/hypermall-web/src/components/layout/Header/index.tsx`
- `frontend/hypermall-web/src/components/cart/CartDrawer.tsx`
- `frontend/hypermall-web/src/components/admin/AdminLayout.tsx`

## Implementation Steps
1. Run regression sweep on key flows: search, auth entry, cart open/update, cart to checkout, admin nav, modal interactions.
2. Run responsive review for mobile, tablet, desktop on high-value pages first.
3. Run accessibility review: focus order, visible focus, contrast, semantics, reduced motion, dialog behavior.
4. Run performance review: bundle delta, animation smoothness, image/layout stability, shell responsiveness.
5. Capture before/after screenshots for approval and bug triage.
6. Fix critical regressions before expanding revamp to secondary pages.

## Todo list
- [ ] Define regression checklist per high-value flow
- [ ] Define responsive review matrix
- [ ] Define a11y review matrix
- [ ] Define perf budget and bundle delta check
- [ ] Capture approval screenshots

## Success Criteria
- No P1 regressions in cart, auth, nav, or admin access flows.
- Reduced-motion and keyboard navigation are fully usable.
- Visual quality improves without violating product performance goals.

## Risk Assessment
- Performance drift from fonts, motion, and layered surfaces.
- Accessibility regressions from low contrast and custom focus styling.
- Visual inconsistencies if primitives and page overrides diverge.

## Security Considerations
- Preserve safe auth/logout/navigation behavior after visual changes.
- Keep trust signals around totals, promotions, and destructive actions explicit.
- Avoid UI states that mislead users about payment, status, or action confirmation.

## Next steps
- If phase 05 passes, expand the same system to PLP, PDP, checkout, profile, and seller surfaces in separate scoped plans.
