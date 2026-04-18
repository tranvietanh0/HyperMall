# HyperMall Code Standards and Conventions

This document codifies the practices embodied in the HyperMall codebase. Keep `docs/` as the source of truth, and update this page whenever naming, layering, or testing expectations shift.

## 1. Principles

- **Clarity**: Favor meaningful names, short methods, and self-explanatory structure.
- **Maintainability**: Isolate each domain (entity, controller, service, repository, DTO) and avoid side effects.
- **Security & Observability**: Guard every endpoint with validation, log context-rich information, and emit metrics via Spring Boot Actuator + Prometheus.
- **Consistency**: Apply the same patterns shared libraries (`common-lib`, `frontend/src/services`) expose—mismatches must be documented in this file.

## 2. Java Backend Standards

### Project structure

Each module follows the `service-name` package root (e.g., `com.hypermall.user`). The folder layout is always:

```
src/main/java/com/hypermall/{service}/
├── {ServiceName}Application.java
├── config/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
│   ├── request/
│   └── response/
├── mapper/
├── exception/
└── security/
```

### Naming and style

- Class names use PascalCase, constants use `SCREAMING_SNAKE`, methods/fields use camelCase.
- Controllers end with `Controller`, services with `Service`/`ServiceImpl`, repositories with `Repository`, DTOs with `Request`/`Response` suffixes, entities with domain nouns.
- Prefer constructor injection (`@RequiredArgsConstructor`). Keep methods under ~30 lines—extract complex logic to private helpers.
- Use Lombok for DTOs and entities (`@Data`, `@Getter/@Setter`, `@Builder`) but never hide critical behavior behind `@SneakyThrows`.

### Shared concerns

- `common-lib` supplies `ApiResponse<T>`, `PageResponse<T>`, exception hierarchy (`BadRequestException`, `ResourceNotFoundException`, `ValidationException`), security helpers (`JwtTokenProvider`, `JwtAuthenticationFilter`, `@CurrentUser`), RabbitMQ publishers, and configuration beans (Jackson, Redis, Async). Import this module in every service.
- Document `springdoc-openapi-starter-webmvc-ui` annotations on every controller to keep Swagger specs current.
- MapStruct mappers live under `mapper/`; annotate interfaces with `@Mapper(componentModel = "spring")` and rely on the generated `*MapperImpl` classes.

### Configuration & security

- External config: `Spring Cloud Config Server` (credentials `config/config123`). Keep cloud config yaml in `config-server/src/main/resources/configurations/` and update it with any new service property.
- API Gateway enforces JWT tokens (15-minute access, 7-day refresh) and rate limiting (100 req/min anonymous, 500 req/min authenticated). Services should not replicate token validation logic beyond `common-lib` utilities.

### Testing expectations

- Backends run `mvn test` per module. New business logic must come with controller/unit tests that exercise `Service` methods. Required coverage applies especially to buyer-facing modules (user/product/cart/order) plus payment/shipping.
- Document the current testing gaps: `analytics-service`, `cart-service`, `inventory-service`, `media-service`, `notification-service`, `payment-service`, `product-service`, `promotion-service`, `review-service`, `search-service`, `seller-service`, `shipping-service`, and `common-lib` still lack `src/test` coverage.
- Use Mockito, `@SpringBootTest` (lightweight), or `@WebMvcTest` depending on the scope. Record any unstable tests in `logs/` and do not suppress `Console` output without reason.

## 3. TypeScript / React Standards

### Structure & aliases

- Entry points: `src/main.tsx`, `src/App.tsx`, `src/routes/index.tsx`.
- Path aliases (`tsconfig.json`):
  - `@/*` → `src/*`
  - `@components/*`, `@pages/*`, `@hooks/*`, `@services/*`, `@store/*`, `@types/*`, `@config/*`, `@utils/*`.
- Keep UI primitives under `src/components/`, routing views in `src/pages/`, hooks under `src/hooks/`, API helpers under `src/services/`, and Redux logic inside `src/store/` (with slices in `src/store/slices`).

### Naming and components

- Components use PascalCase filenames (`ProductCard.tsx`).
- Hooks use `use` prefix (`useCart`, `useProductFilters`).
- API service functions (e.g., `productService.getProducts`) live next to typed request/response DTOs (`src/types/`). Keep axios instances in `src/services/api.service.ts` and configuration in `src/config/api.config.ts`.
- Tailwind classes follow the order: layout → spacing → typography → state (e.g., `flex flex-col gap-4 text-sm text-slate-600`). Use `clsx` to manage conditionals.

### Tooling & linting

- `frontend/hypermall-web/.eslintrc.cjs` extends `eslint:recommended`, `@typescript-eslint/recommended`, and `react-hooks/recommended`. Customizations:
  - Warn on `@typescript-eslint/no-unused-vars` (args starting with `_` ignored).
  - Warn on `@typescript-eslint/no-explicit-any`.
  - `react-refresh/only-export-components` warning mode allows constant exports.
- `tsconfig` enforces `strict`, `noUnusedLocals`, `noUnusedParameters`, `noFallthroughCasesInSwitch`, and bundler resolution.

### State & effects

- Global state prefers Redux Toolkit slices with `createAsyncThunk` for API side effects. `src/store/index.ts` wires the root reducer and middleware.
- Local state uses `useState` or `useReducer` when necessary; complex shared state may also live in `zustand` stores alongside Redux if the feature demands it.

### Testing

- Vitest drives the front-end suite: `npm run test`, `npm run test:coverage`, `npx vitest run [path]`. The global setup file (`src/test/setup.ts`) mocks `window.matchMedia`, `localStorage`, and `IntersectionObserver` to keep tests deterministic.
- Keep `React Router DOM` routes declarative; avoid dynamic route arrays that break server-side hydration. When Vitest warns about React Router future flags, capture the warning text before bumping to v7.

## 4. API & Response Standards

- Every REST endpoint returns `ApiResponse<T>` (success flag, message, payload). Paginated endpoints wrap results in `PageResponse<T>` with page/size/total metadata.
- Controllers should annotate with `@Operation(summary = "...")` and `@ApiResponse` where appropriate so Swagger/OpenAPI docs stay in sync.
- Error handlers rely on `ErrorResponse` objects; custom exceptions extend `BaseException` from `common-lib`.

## 5. Documentation Standards

- `docs/` is canonical; README acts as a pointer. When code changes, edit the related docs inside `docs/` (e.g., API changes → `docs/API.md`, deployment changes → `docs/DEPLOYMENT.md`).
- Add metadata (version, last updated, author) where helpful and use Markdown tables for configuration values.
- Mention known runtime artifacts (JVM crash logs `hs_err_pid*.log`, `replay_pid*.log`) in `docs/system-architecture.md` or release notes so future engineers know why the files exist.

## 6. Continuous Improvement

- Pull requests must include tests (unit/integration), follow `feat|fix|docs|test|chore` commit conventions, and link to the relevant plan/ticket.
- Any change touching `docs/` should call out the files that now describe the new contract; e.g., adding a new service requires a row in `docs/system-architecture.md` and a mention in `docs/API.md`.
