# Frontend UI Map

## Candidate pages/layouts to revamp first
- `frontend/hypermall-web/src/pages/Home/index.tsx`: the current hero, categories grid, flash sale, and featured product sections are placeholders with heavy Tailwind blocks—this is the public-facing entry point where new visuals and motion should land immediately.
- `frontend/hypermall-web/src/components/layout/MainLayout/index.tsx`, `Header/index.tsx`, `Footer/index.tsx`, and `components/cart/CartDrawer.tsx`: this set stitches the storefront together (sticky header with search, cart drawer, MegaFooter) so any new spacing, typography, or nav behavior must flow through them.
- `frontend/hypermall-web/src/components/admin/AdminLayout.tsx` and `frontend/hypermall-web/src/pages/Admin/Dashboard/index.tsx`: the admin shell (sidebar + workspace) and its dashboard cards/table are the only current admin experiences; modernizing them now sets the standard for every `/admin/*` page.
- Routing entry points (`frontend/hypermall-web/src/App.tsx`, `src/routes/index.tsx`, `src/routes/AdminRoutes.tsx`, `src/main.tsx`) define the layout boundaries (admin vs. storefront) and global providers (router, store, `react-hot-toast`), so any layout/animation overhaul must respect these routes and their suspense/loading fallbacks.

## Reusable components to preserve or replace
- `frontend/hypermall-web/src/components/common/Button/index.tsx`, `Input/index.tsx`, `Loading/index.tsx`, `Modal/index.tsx`: these primitives already wrap Tailwind transitions, icons, and Headless UI/Spinner helpers—decide whether to refresh their tokens (variant palettes, roundedness, spacing) or swap in a new design system while keeping their API surface intact.
- `components/cart/CartDrawer.tsx`: heavily tied to `uiSlice` states and Headless UI `Transition`/`Dialog` animations; reuse its behavior while refreshing the visual chrome so the cart drawer still plugs into `setCartDrawerOpen` and `useCart`.
- `store/slices/uiSlice.ts`: exposes menu, drawer, theme, and loading toggles that the header/main layout poll; any refactor to hamburger, cart, or global loading must keep this shape or carefully migrate the consuming selectors.

## Styling constraints
- `frontend/hypermall-web/src/styles/globals.css` layers Tailwind with custom `.btn`, `.input`, `.badge`, `.card`, `.container`, `.heading-*`, and utility classes plus the `Inter` font/scrollbar tweaks—modern visuals must either adopt these helpers or provide replacements in the same CSS scope.
- Primary colors rely on `primary-600/700`/`secondary-600/700` etc., so refreshes should align with the Tailwind config (no new CSS variables currently present) and keep Tailwind utility conventions to avoid mismatched tokens.
- `react-hot-toast` options in `src/main.tsx` set toast colors; new components should respect or override that theme when rendering cross-app notifications.
- Motion is delivered through Tailwind `transition-*` utilities, Heroicons, and Headless UI transitions (modals, drawer). There is no dedicated animation library, so more complex motion will need a new dependency or carefully layered CSS/Framer Motion wrappers.

## Risk areas
- The sticky header (`Header/index.tsx`) mixes search, authentication state, and localized buttons with `uiSlice` dispatches—overhauling it risks breaking cart/mobile menu toggles unless the state shape is preserved.
- `CartDrawer` and `AdminLayout` already rely on Headless UI `Dialog`/`Transition` plus `clsx` for sidebar states; changing the markup/structure can easily break keyboard focus/backdrop behavior if transitions are not matched.
- `MainLayout` and admin shell both wrap the router `<Outlet />`; duplicating layout logic across routes can introduce inconsistencies (e.g., syncing `Header`/`Footer` spacing) unless changes stay centralized in those files.
- Placeholder data in `Home` and `AdminDashboard` means visual work should avoid anchoring to static content that will later be data-driven.
- Global styles live in a single `globals.css`; adding new design tokens may collide with existing `.btn`/`.container` classes if not namespaced carefully.

## Recommended implementation order
1. Refresh `MainLayout`, `Header`, `Footer`, and the `Home` hero/sections so the landing experience and navigation set the new look before cascading to inner modules.
2. Rework `Button`, `Input`, `Loading`, `Modal`, and the `CartDrawer` chrome plus global styles (`globals.css`) so the shared primitives and utility classes carry the new language.
3. Modernize the admin shell (`AdminLayout`) and `Admin/Dashboard` page, ensuring the `App.tsx`/`routes/*` structure still isolates admin vs. storefront layouts while reusing the refreshed primitives.
4. Add or adjust motion/animation rules (via Tailwind utilities or a motion library) around the refreshed components once the structural layout is stable.
