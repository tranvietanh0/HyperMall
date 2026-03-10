# AGENTS.md

Operational guide for coding agents working in `HyperMall`.

## 1) Project Snapshot
- Monorepo for a microservices e-commerce platform.
- Frontend: React 18 + TypeScript + Vite + Tailwind in `frontend/hypermall-web`.
- Backend: Spring Boot 3.4.3 + Spring Cloud + Maven multi-module build in `backend`.
- Infra: Docker Compose for MySQL, Redis, RabbitMQ, and Elasticsearch in `infrastructure/docker`.

## 2) Rule Sources Checked
- `.cursor/rules/`: not present.
- `.cursorrules`: not present.
- `.github/copilot-instructions.md`: not present.
- `CLAUDE.md`: present and incorporated into this file.
- If Cursor or Copilot rule files are later added, update this file immediately.

## 3) Repository Layout
- `frontend/hypermall-web`: Vite app; use `src/components`, `src/pages`, `src/services`, `src/store`, and `src/types`.
- `backend`: Maven parent project plus platform and business services.
- `backend/common-lib`: shared DTOs, exceptions, security, utilities, and config.
- `backend/config-server/src/main/resources/configurations`: centralized service config files.
- `infrastructure/docker`: local infra startup files.

## 4) Core Commands
Run commands from the noted directory. Prefer the smallest relevant command before broad builds.

### Frontend (`frontend/hypermall-web`)
- Install dependencies: `npm install`
- Start dev server: `npm run dev`
- Build production bundle: `npm run build`
- Preview production build: `npm run preview`
- Lint: `npm run lint`
- Run tests in watch mode: `npm run test`
- Run tests once with coverage: `npm run test:coverage`
Single-test frontend workflows:
- Single file: `npx vitest run src/path/to/file.test.ts`
- Single TSX file: `npx vitest run src/path/to/file.test.tsx`
- By test name: `npx vitest run -t "test name pattern"`
- By file and name: `npx vitest run src/path/to/file.test.ts -t "pattern"`
- If adding new tests, follow Vitest naming like `*.test.ts` or `*.test.tsx`.

### Backend (`backend`)
- Build all modules, skip tests: `mvn clean install -DskipTests`
- Run all tests: `mvn test`
- Build one module, skip tests: `mvn -pl user-service clean package -DskipTests`
- Run one module locally: `mvn -pl user-service spring-boot:run`
- Test one module: `mvn -pl user-service test`
Single-test backend workflows:
- Single test class across current Maven scope: `mvn test -Dtest=ClassName`
- Single test method across current Maven scope: `mvn test -Dtest=ClassName#methodName`
- Single class in one module: `mvn -pl user-service test -Dtest=ClassName`
- Single method in one module: `mvn -pl user-service test -Dtest=ClassName#methodName`
- If dependencies are needed for isolated module runs, use `-am`: `mvn -pl user-service -am test -Dtest=ClassName`

### Infrastructure (`infrastructure/docker`)
- Start local infra: `docker-compose -f docker-compose.dev.yml up -d`

## 5) Local Startup Order
Bring services up in this order to avoid discovery and config failures:
1. `service-registry` on `8761`
2. `config-server` on `8888`
3. `api-gateway` on `8080`
4. business services such as `user-service`, `product-service`, and `order-service`

## 6) High-Value Endpoints
- Frontend: `http://localhost:3000`
- API gateway: `http://localhost:8080/api/*`
- Eureka dashboard: `http://localhost:8761`
- Swagger UI: `http://localhost:{service-port}/swagger-ui.html`
- RabbitMQ UI: `http://localhost:15672` with `guest/guest`

## 7) Frontend Style Guide

### Imports and module boundaries
- Prefer configured path aliases over deep relative imports.
- Available aliases from `tsconfig.json`: `@/`, `@components`, `@pages`, `@hooks`, `@services`, `@store`, `@types`, `@utils`, `@config`.
- Order imports as: external packages -> aliased internal modules -> local relative modules.
- Use `import type` for type-only imports when practical.
- Reuse existing services, hooks, and shared components before creating new abstractions.
### TypeScript and state
- `strict` mode is enabled; preserve strict typing.
- Avoid `any`; prefer precise interfaces, generics, or `unknown` with narrowing.
- Keep API/domain contracts in `src/types`.
- Type async thunk payloads and rejection values explicitly when extending Redux slices.
- Keep Redux state serializable and colocate slices in `src/store/slices`.
- Respect compiler constraints like `noUnusedLocals` and `noUnusedParameters`.
### Components and naming
- Component files and exported components use PascalCase when the file represents a component, e.g. `ProductCard.tsx`.
- Hooks use `useXxx` naming in `src/hooks`.
- Services use domain-focused names with `*.service.ts`.
- Pages may use folder-based `index.tsx`; preserve the existing pattern in that area.
- Prefer small presentational components and move reusable logic into hooks/services.
### Formatting and UI patterns
- Match existing project formatting: 2-space indentation, semicolons where present, single quotes where present.
- Keep Tailwind class ordering readable; group layout -> spacing -> typography -> color/state when possible.
- Reuse shared UI such as `Button`, `Input`, `Modal`, and `Loading` before adding new primitives.
- Avoid broad refactors of formatting in untouched files.
### Frontend error handling
- Route API access through `src/services/api.service.ts` and existing axios helpers.
- Preserve token refresh behavior and existing storage keys.
- For async actions, return clear user-facing messages through `rejectWithValue`.
- Do not swallow errors silently unless the flow explicitly calls for it.
- Favor safe fallbacks in UI rather than leaving loading/error states undefined.

## 8) Backend Style Guide

### Packages and layering
- Keep standard layers: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `config`.
- Controllers should stay thin and delegate business logic to services.
- Services should own transaction boundaries and domain behavior.
- Repositories should remain declarative and persistence-focused.
- Shared concerns belong in `backend/common-lib` when already standardized there.
### DTOs, mapping, and responses
- Do not expose JPA entities directly from controllers.
- Use DTOs for request and response boundaries.
- Use MapStruct mappers where a mapper already exists or the module follows that pattern.
- Return `ApiResponse<T>` for successful controller responses.
- Use `PageResponse<T>` for paginated endpoints.
### Spring and Java conventions
- Java uses version 17.
- Prefer constructor injection, typically via `@RequiredArgsConstructor`.
- Class/interface names use PascalCase; methods/fields use camelCase; constants use UPPER_SNAKE_CASE.
- Request/response DTO names should follow `XxxRequest` and `XxxResponse`.
- Use enums for bounded domain states such as status or type values.
### Validation, transactions, and persistence
- Put Jakarta Bean Validation annotations on request DTOs.
- Use `@Valid` on controller request bodies/parameters.
- Mark read paths with `@Transactional(readOnly = true)` and write paths with `@Transactional`.
- Keep entity mutation explicit inside service methods.
- Avoid business logic inside controllers and JPA entities.
### Backend error handling and logging
- Throw typed exceptions from `common-lib` such as `BadRequestException`, `ResourceNotFoundException`, `UnauthorizedException`, or `ConflictException`.
- Rely on `GlobalExceptionHandler` for consistent `ErrorResponse` payloads.
- Log meaningful context such as ids or emails, but never log passwords, tokens, or secrets.
- Prefer user-safe API messages over leaking infrastructure details.

## 9) Testing Expectations
- Add or update tests when making functional changes in an area that already has test coverage.
- For bug fixes, add at least one regression-oriented test when feasible.
- Frontend tests should verify behavior, not implementation details.
- Backend tests should focus on service logic first, then controller/repository boundaries as needed.
- Run the narrowest relevant test command first, then broaden only if needed.

## 10) Agent Working Rules
- Identify the target area first: `frontend/hypermall-web` or a specific backend module.
- Keep edits minimal and aligned with existing architecture.
- Prefer targeted module commands over repo-wide commands.
- Do not introduce new frameworks, linters, or formatting tools unless explicitly requested.
- Respect existing local changes in the worktree; do not revert unrelated user work.
- When updating this file later, keep command examples synchronized with `package.json` and Maven configuration.

## 11) Maintenance Notes
- If `.cursor/rules/`, `.cursorrules`, or `.github/copilot-instructions.md` appear later, fold their guidance into this file.
- If frontend scripts or Maven module names change, update command examples here immediately.
- If dedicated lint/format tools are added later, document their exact invocation and scope.
