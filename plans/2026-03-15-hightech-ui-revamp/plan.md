# High-Tech UI Revamp Plan

## Overview
- Goal: make HyperMall feel premium, smooth, modern. Not neon toy. Not glass spam. Still commerce-first.
- Scope first: storefront shell + home + cart drawer. Then shared primitives/tokens. Then motion system. Then admin shell. Then QA/perf/a11y.
- Guardrails: keep React 18 + Vite + Tailwind stack. No new design-system framework. Preserve existing route/state boundaries.

## Visual direction
- Direction: precision commerce. Graphite/off-white base, disciplined accent family, dense-but-breathable layout, strong price hierarchy, restrained depth.
- Color rule: 70-80% neutral. Accent only for CTA, active state, sale state, trust markers.
- Typography rule: upgrade from generic `Inter` feel to a sharper Vietnamese-friendly stack; single-family by default, optional display accent only if budget holds.
- Surface rule: use spacing, nested surfaces, contrast. Fewer divider lines. Fewer ornamental effects.

## Motion recommendation
- Recommended: add `motion` for React-aware orchestration only where Tailwind + Headless UI are weak.
- Keep Tailwind transitions for hover/focus/press. Keep Headless UI for dialog semantics.
- Use motion on hero reveal, cart drawer polish, nav continuity, section enters, shared layout cues. Skip heavy route transitions in checkout/account.
- Trade-off: one new dependency, better consistency. Cheaper option is Tailwind-only, but it will cap polish and increase ad hoc animation code.

## Rollout
- Wave 1: storefront shell and home. Highest business impact. Immediate brand reset.
- Wave 2: shared tokens and primitives. Make new look reusable without API breakage.
- Wave 3: motion system. Add only after structure is stable.
- Wave 4: admin shell and dashboard. Reuse same core language, denser utility mode.
- Wave 5: regression, perf, accessibility, reduced-motion, screenshots.

## Acceptance gates
- Storefront looks coherent across `Home`, `Header`, `Footer`, `CartDrawer`.
- Shared primitives keep stable public props unless migration is documented.
- Motion stays calm, uses mostly `transform`/`opacity`, respects reduced motion.
- No breakage in auth/cart/menu flows, route layout boundaries, or toast/loading behavior.
- Mobile-first quality holds. Performance stays within current product goals.

## Phase files
- `plans/2026-03-15-hightech-ui-revamp/phase-01-storefront-shell-and-home.md`
- `plans/2026-03-15-hightech-ui-revamp/phase-02-shared-tokens-and-primitives.md`
- `plans/2026-03-15-hightech-ui-revamp/phase-03-motion-system-and-orchestration.md`
- `plans/2026-03-15-hightech-ui-revamp/phase-04-admin-shell-refresh.md`
- `plans/2026-03-15-hightech-ui-revamp/phase-05-qa-perf-accessibility.md`

## Core calls
- Recommended palette mood: cool graphite + soft white + restrained jade/cyan accent. Optional warm accent only for promos.
- Recommended implementation style: tokenize in `globals.css`, refresh primitives, then restyle high-value surfaces.
- Recommended font path: single UI font first; avoid dual-font setup unless hero impact is clearly worth payload.
