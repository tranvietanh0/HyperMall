# Research Report: React 18 + Vite + TypeScript Frontend with Spring Cloud Gateway Backend

Conducted: 2026-03-15

## Scope
- Focus: API client layering, auth/token refresh, state boundaries, route/layout organization, env/base URL handling, and frontend-backend contract stability.
- Constraint: max 3 sources, favor official docs.

## Sources
1. React docs, "Scaling Up with Reducer and Context" - https://react.dev/learn/scaling-up-with-reducer-and-context
2. Vite docs, "Env Variables and Modes" - https://vite.dev/guide/env-and-mode
3. Spring Cloud Gateway reference - https://docs.spring.io/spring-cloud-gateway/reference/

## Key Practices

### 1. API client layering
- Keep a thin transport layer for HTTP concerns only: base URL, headers, retries, auth attachment, 401 handling, and normalized error mapping; keep domain-specific methods in feature services above it.
- Keep React components/hooks free of raw gateway paths; depend on typed feature services so route changes, header changes, and gateway rewrite rules stay localized.
- Treat the gateway as the single frontend edge, even if it fans out to many services internally; this matches Spring Cloud Gateway's role for routing and cross-cutting concerns like security, resiliency, and metrics. [3]

### 2. Auth and token refresh
- Prefer gateway-enforced auth and centralized token forwarding/relay over each feature inventing its own header rules; Spring Cloud Gateway explicitly supports cross-cutting security and `TokenRelay`. [3]
- In the frontend, keep refresh logic in one place only, usually the transport/interceptor layer; queue concurrent 401s behind one refresh attempt to avoid refresh storms.
- Fail closed on refresh failure: clear session state, invalidate cached user data, and route to login instead of looping requests.
- Avoid storing long-lived secrets in Vite client env; Vite warns `VITE_*` values are bundled into client code. [2]

### 3. State management boundaries
- Keep server state and UI state separate. Use React context/reducer for app-level UI/workflow state, not as a dump for all fetched API data. React recommends using reducer + context to scale shared state while keeping wiring separate from display components. [1]
- Keep auth/session state minimal and global; keep feature data close to the feature boundary or dedicated data-fetching layer.
- Split read state from write actions where useful; React's separate state/dispatch contexts reduce prop drilling and help avoid broad rerenders. [1]

### 4. Route and layout organization
- Organize routes around layouts and capability areas: public shell, authenticated shell, admin shell, checkout shell. Put shared guards, navigation, and page chrome at layout boundaries rather than duplicating per page.
- Keep route modules responsible for composition, not transport details; they should call feature hooks/services, not manually assemble gateway URLs.
- Align route boundaries with backend bounded contexts where possible so ownership, caching, and failure handling stay understandable.

### 5. Env and base URL management
- Read frontend env only through `import.meta.env`; Vite statically replaces these values and exposes built-ins like `MODE`, `BASE_URL`, `DEV`, and `PROD`. [2]
- Expose only non-secret client config with `VITE_*`; type expected keys in `vite-env.d.ts` to catch drift early. [2]
- Keep one explicit public API base URL per deploy environment; avoid mixing hard-coded service URLs with gateway URLs.
- Restart Vite after env changes, and use mode-specific files like `.env.production` or `.env.staging` deliberately; Vite loads `.env`, `.env.local`, and mode-specific files with clear precedence rules. [2]

### 6. Frontend-backend contract stability
- Keep the gateway contract boring: stable resource paths, predictable status codes, consistent pagination/error envelopes, and additive response evolution.
- Use gateway rewrites/filters sparingly. They are powerful, but overusing path/body/header mutation can hide backend drift and make frontend debugging harder; Spring Cloud Gateway supports many rewrite and modify filters, so discipline matters. [3]
- Version only when needed, but always avoid silent breaking changes in response shape; frontend typing catches some drift, not semantic drift.
- Treat shared DTO docs and example payloads as release artifacts. If backend changes a field, frontend types, mocks, and integration tests should change in the same release window.

## Common Failure Modes
- API logic spread across components, hooks, and slices, causing inconsistent headers, duplicated retry logic, and fragile gateway migrations.
- Multiple simultaneous 401 responses each trigger refresh, causing token races, duplicate writes, or logout loops.
- Putting fetched collections into broad global context, causing unnecessary rerenders and stale data ownership confusion. [1]
- Hard-coding `http://localhost:<service>` in frontend code, bypassing the gateway and breaking prod parity.
- Leaking secrets into `VITE_*` vars or assuming non-prefixed env vars are available in the browser. [2]
- Letting gateway filters rewrite too much request/response structure, so frontend behavior depends on hidden infra rules instead of stable contracts. [3]
- Treating gateway/network errors the same as domain validation errors, which produces poor UX and weak observability.

## Practical Recommendations
- Standardize on: `components -> feature hooks/services -> shared API client -> API gateway`.
- Keep one auth module responsible for login state, token attachment, refresh orchestration, and logout.
- Keep one config module responsible for validated `import.meta.env` access and base URL derivation.
- Put layout shells and auth guards at route boundaries; keep page components mostly declarative.
- Require backend changes that affect JSON shape, status codes, or error envelopes to ship with contract notes and frontend verification.

## Bottom Line
- The safest frontend shape is thin pages, typed feature services, one shared transport client, one gateway base URL, and one centralized auth-refresh path.
- The most common breakages are not React-specific; they come from blurred ownership boundaries between UI, transport, auth, gateway rewrites, and backend contracts.

## Unresolved Questions
- Does the gateway terminate auth itself, or only forward bearer tokens to downstream Spring services?
- Is refresh token handling browser-based, BFF-based, or fully delegated to the gateway/security layer?
- Is there already a canonical error envelope and pagination contract shared across services behind the gateway?
- Are frontend routes currently aligned to backend bounded contexts, or has page composition drifted across service domains?
