# Phase 04 - Admin Shell Refresh

## Context links
- `docs/codebase-summary.md`
- `docs/project-overview-pdr.md`
- `plans/2026-03-15-hightech-ui-revamp/research/researcher-01-visual-direction.md`
- `plans/2026-03-15-hightech-ui-revamp/scout/scout-01-frontend-ui-map.md`

## Overview
- Bring admin into the same premium system. More operational. Less editorial. Same DNA.

## Key Insights
- Admin already has shell boundaries in place. Good for focused restyle.
- Dashboard content is mock-heavy. Design must survive real data density later.
- Admin needs stronger table/card clarity than storefront glamour.

## Requirements
- Reuse storefront tokens and primitives where sensible.
- Increase density and contrast for data work.
- Keep admin route isolation and sidebar behavior intact.

## Architecture
- `AdminLayout` becomes the admin frame: sidebar, top bar, workspace rhythm, surface tiers.
- `Dashboard` becomes the pattern setter for stat cards, tables, badges, and empty/loading states.
- Motion stays lighter than storefront. Utility first.

## Related code files
- `frontend/hypermall-web/src/components/admin/AdminLayout.tsx`
- `frontend/hypermall-web/src/pages/Admin/Dashboard/index.tsx`
- `frontend/hypermall-web/src/App.tsx`
- `frontend/hypermall-web/src/routes/AdminRoutes.tsx`
- `frontend/hypermall-web/src/components/common/Button/index.tsx`
- `frontend/hypermall-web/src/components/common/Loading/index.tsx`

## Implementation Steps
1. Redefine admin shell surfaces, sidebar states, and top bar hierarchy using phase 02 tokens.
2. Refresh dashboard stat cards for tabular numbers, contrast, and cleaner change indicators.
3. Refresh table styling, badges, loading states, and hover/focus behavior for operational readability.
4. Keep mobile sidebar interaction safe and obvious.
5. Verify admin routes still isolate admin shell from storefront shell cleanly.

## Todo list
- [ ] Define admin density rules
- [ ] Define stat card and table patterns
- [ ] Define badge/status semantics without purple drift
- [ ] Verify sidebar mobile and desktop behavior
- [ ] Review loading and empty states

## Success Criteria
- Admin feels modern and aligned with storefront, but more compact and work-oriented.
- Dashboard supports future real data density without redesign.
- Sidebar and top bar remain predictable on mobile and desktop.

## Risk Assessment
- Copying storefront styling directly will hurt admin usability.
- Dashboard mocks can hide data-overflow issues.
- Sidebar changes can break active-state clarity and mobile dismissal.

## Security Considerations
- Keep logout, role context, and navigation clarity prominent.
- Preserve keyboard and focus behavior for sidebar interactions.
- Do not reduce contrast on statuses that drive admin decisions.

## Next steps
- Phase 05 closes with QA, accessibility, performance, and rollout hardening across both shells.
