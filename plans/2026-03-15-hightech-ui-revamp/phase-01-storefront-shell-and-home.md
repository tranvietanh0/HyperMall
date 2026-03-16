# Phase 01 - Storefront Shell And Home

## Context links
- `docs/codebase-summary.md`
- `docs/code-standards.md`
- `docs/system-architecture.md`
- `docs/project-overview-pdr.md`
- `plans/2026-03-15-hightech-ui-revamp/research/researcher-01-visual-direction.md`
- `plans/2026-03-15-hightech-ui-revamp/research/researcher-02-motion-strategy.md`
- `plans/2026-03-15-hightech-ui-revamp/scout/scout-01-frontend-ui-map.md`

## Overview
- Reset public perception fast. Revamp the shell users see first: home, header, footer, cart drawer, layout frame.

## Key Insights
- `Home` is placeholder-heavy now. Biggest visual upside.
- Header/cart drawer hold core commerce actions. Break them and the whole revamp fails.
- Premium signal should come from hierarchy, spacing, type, surfaces. Not from noise.

## Requirements
- Build one coherent storefront language across hero, nav, merchandising, cart entry, and footer.
- Keep existing route and store behavior intact.
- Make mobile quality equal priority to desktop.

## Architecture
- Treat `MainLayout` as the stable storefront frame.
- Make `Header`, `Footer`, and `CartDrawer` the shell primitives for spacing, accent, and interaction rhythm.
- Restructure `Home` into progressive disclosure: hero -> trust/utility -> categories -> flash sale -> featured modules.

## Related code files
- `frontend/hypermall-web/src/components/layout/MainLayout/index.tsx`
- `frontend/hypermall-web/src/components/layout/Header/index.tsx`
- `frontend/hypermall-web/src/components/layout/Footer/index.tsx`
- `frontend/hypermall-web/src/components/cart/CartDrawer.tsx`
- `frontend/hypermall-web/src/pages/Home/index.tsx`
- `frontend/hypermall-web/src/App.tsx`
- `frontend/hypermall-web/src/routes/index.tsx`

## Implementation Steps
1. Lock visual direction: graphite/off-white shell, controlled jade-cyan accent, premium spacing, sharper type hierarchy.
2. Rebuild header information architecture: compact utility row, stronger search, cleaner auth/cart actions, better sticky behavior.
3. Rebuild home sections with progressive disclosure and clearer commerce priorities. Keep placeholder data structure replaceable.
4. Rework footer into trust-heavy commerce footer, not link dump.
5. Redesign cart drawer chrome and hierarchy without changing `uiSlice` or `useCart` behavior.
6. Verify route shell spacing and fallback/loading interactions still work through `MainLayout` and `App`.

## Todo list
- [ ] Finalize storefront visual spec and surface rules
- [ ] Define home section hierarchy and module density
- [ ] Map sticky header states: desktop, mobile, search, auth, cart count
- [ ] Map cart drawer states: empty, populated, quantity change, checkout CTA
- [ ] Define footer trust/payment/shipping blocks

## Success Criteria
- Landing experience looks premium and distinct within one screen.
- Search, cart, login, mobile menu, and checkout entry remain obvious and fast.
- Cart drawer feels upgraded but preserves existing behavior and keyboard accessibility.

## Risk Assessment
- Header over-styling can bury search and cart.
- Home placeholder content can lead to fake-polished layouts that collapse when real data lands.
- Footer can become visually heavy if every trust block competes.

## Security Considerations
- Do not weaken focus states or auth visibility in header flows.
- Do not hide cart totals or checkout CTAs behind motion-heavy transitions.
- Keep external link handling and user avatar rendering safe.

## Next steps
- Phase 02 must extract the visual language into reusable tokens and primitives before more page work spreads.
