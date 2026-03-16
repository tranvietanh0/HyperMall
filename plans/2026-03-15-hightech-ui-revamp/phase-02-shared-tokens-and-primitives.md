# Phase 02 - Shared Tokens And Primitives

## Context links
- `docs/codebase-summary.md`
- `docs/code-standards.md`
- `plans/2026-03-15-hightech-ui-revamp/research/researcher-01-visual-direction.md`
- `plans/2026-03-15-hightech-ui-revamp/scout/scout-01-frontend-ui-map.md`

## Overview
- Turn the new look into reusable defaults. Do it without reckless component API churn.

## Key Insights
- `globals.css` is the current style choke point.
- Button/Input/Loading/Modal already centralize behavior. Good leverage. High break risk if props change.
- No CSS variable system exists yet. Add only what the revamp actually needs.

## Requirements
- Introduce minimal design tokens for color, surface, radius, spacing, focus, elevation, and type roles.
- Refresh primitives while preserving current public props where possible.
- Keep Tailwind-first workflow. No heavy framework swap.

## Architecture
- Token layer lives in `globals.css` first. CSS vars only for shared semantic values that Tailwind utilities alone cannot manage cleanly.
- Primitives consume semantic tokens, not hard-coded page colors.
- Old utility class names stay if possible; their internals change.

## Related code files
- `frontend/hypermall-web/src/styles/globals.css`
- `frontend/hypermall-web/src/components/common/Button/index.tsx`
- `frontend/hypermall-web/src/components/common/Input/index.tsx`
- `frontend/hypermall-web/src/components/common/Loading/index.tsx`
- `frontend/hypermall-web/src/components/common/Modal/index.tsx`
- `frontend/hypermall-web/src/main.tsx`

## Implementation Steps
1. Define semantic tokens: canvas, surface, surface-strong, text tiers, accent, success, warning, danger, focus ring, border, elevation.
2. Replace generic `.btn`, `.input`, `.card`, `.badge`, heading helpers with premium but practical defaults.
3. Refresh Button/Input states first. They carry most perceived quality.
4. Refresh Modal and Loading to match the new shell depth, focus, and motion rules.
5. Align toast styling in `main.tsx` with the same semantic token language.
6. Audit usage for visual regressions before phase 03 starts.

## Todo list
- [ ] Define minimal token inventory
- [ ] Freeze which existing utility classes stay public
- [ ] Document Button/Input visual variants and state rules
- [ ] Define modal/drawer depth and overlay rules
- [ ] Align loading and toast appearance

## Success Criteria
- Shared primitives look consistent across storefront surfaces.
- Existing imports and prop signatures mostly survive unchanged.
- New page work can use tokens instead of re-inventing styles.

## Risk Assessment
- Token sprawl will slow delivery and create fake design-system work.
- Overusing CSS vars can fight Tailwind instead of helping it.
- Primitive restyles can create subtle regressions in forms and dialogs.

## Security Considerations
- Focus states stay high contrast and keyboard-visible.
- Error and destructive states remain semantically distinct.
- Modal layering must not break dismissal, focus trap, or backdrop meaning.

## Next steps
- Phase 03 adds motion rules on top of stabilized structure and tokens. Not before.
