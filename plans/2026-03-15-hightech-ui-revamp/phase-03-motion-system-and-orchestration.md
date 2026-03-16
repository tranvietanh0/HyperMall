# Phase 03 - Motion System And Orchestration

## Context links
- `docs/code-standards.md`
- `plans/2026-03-15-hightech-ui-revamp/research/researcher-02-motion-strategy.md`
- `plans/2026-03-15-hightech-ui-revamp/scout/scout-01-frontend-ui-map.md`

## Overview
- Add calm, intentional motion. Enough to feel premium. Never enough to slow shopping.

## Key Insights
- Current stack is Tailwind + Headless UI transitions. Fine for basics, weak for orchestration.
- Motion needs one language across hero, nav, overlays, cards, and route boundaries.
- Reduced motion must be first-class, not cleanup work.

## Requirements
- Choose one motion approach and keep it small.
- Restrict animation mostly to `transform` and `opacity`.
- Limit heavy transitions to storefront-first surfaces. Keep checkout/account immediate.

## Architecture
- Recommendation: add `motion` package with `LazyMotion` for React-aware layout/enter-exit orchestration.
- Keep Tailwind transitions for hover/focus/press microstates.
- Keep Headless UI dialog primitives for accessibility; layer motion choreography around them only where needed.

## Related code files
- `frontend/hypermall-web/package.json`
- `frontend/hypermall-web/src/pages/Home/index.tsx`
- `frontend/hypermall-web/src/components/layout/Header/index.tsx`
- `frontend/hypermall-web/src/components/cart/CartDrawer.tsx`
- `frontend/hypermall-web/src/components/common/Modal/index.tsx`
- `frontend/hypermall-web/src/App.tsx`
- `frontend/hypermall-web/src/main.tsx`

## Implementation Steps
1. Define motion tokens: durations, easing, distance, opacity ranges, overlay behavior, reduced-motion fallback.
2. Add `motion` only if phase 02 review confirms Tailwind-only is too limited for targeted interactions.
3. Implement signature motions: hero reveal, cart drawer entry polish, active nav/cart badge continuity, section enters.
4. Avoid global route theatrics. Keep main shell stable. Skip heavy checkout transitions.
5. Add reduced-motion rules across CSS and motion wrappers.
6. Measure bundle and runtime impact before motion spreads to more routes.

## Todo list
- [ ] Decide `motion` vs Tailwind-only after phase 02 review
- [ ] Define motion token table
- [ ] Define reduced-motion substitutions
- [ ] Cap which surfaces get choreography
- [ ] Add perf check on low-end mobile profile

## Success Criteria
- Motion improves clarity and perceived polish without delaying interaction.
- Cart drawer, hero, and nav feel cohesive.
- Reduced-motion mode remains usable and calm.

## Risk Assessment
- Easy to over-animate premium UIs into gimmick UIs.
- Route-level motion can hurt scan speed and conversion.
- New dependency without scope control can bloat payload.

## Security Considerations
- Motion must not hide critical state changes, price updates, or destructive actions.
- Reduced-motion path must retain all content and feedback.
- No animation should interfere with dialog focus management.

## Next steps
- Phase 04 reuses the motion/tokens selectively in admin, with denser utility bias and less spectacle.
