# HyperMall project health check

## Problem
- User asked: du an hien con van de gi khong?
- Goal: check codebase health, build/test baseline, production-readiness gaps, no implementation.

## What I checked
- Read root docs/config: `README.md`, `frontend/hypermall-web/package.json`, `backend/pom.xml`
- Ran frontend `npm run lint`, `npm run test:coverage`, `npm run build`
- Ran backend `mvn test`
- Sampled risky files and logs in backend/frontend

## Build/Test baseline
- Frontend lint: pass
- Frontend tests: pass, 12 files / 79 tests
- Frontend coverage: low, total ~32.64%
- Frontend build: pass
- Backend tests: pass, but only a few modules have real tests; many modules show `No tests to run`

## High-risk issues
- Core flows still stubbed/incomplete:
  - cart missing product/inventory/promotion integration in `backend/cart-service/src/main/java/com/hypermall/cart/service/CartService.java:47`
  - cart shipping fee hardcoded `30000` in `backend/cart-service/src/main/java/com/hypermall/cart/service/CartService.java:141`
  - refund logic still fake/stub in `backend/payment-service/src/main/java/com/hypermall/payment/service/PaymentService.java:246`
  - forgot-password does not send email in `backend/user-service/src/main/java/com/hypermall/user/service/AuthService.java:119`
- Runtime stability red flags:
  - JVM crash logs still checked into workspace, eg `backend/hs_err_pid121932.log:2`
  - README already flags prior instability in `README.md:112`
- Dev-grade config still too close to runtime defaults:
  - local DB root/root and `ddl-auto: update` in `backend/user-service/src/main/resources/application.yml:15`
  - default config/eureka creds in `backend/api-gateway/src/main/resources/application-dev.yml:3`
  - default eureka creds in `backend/service-registry/src/main/resources/application-dev.yml:4`
  - example env exposes fixed JWT secret value in `backend/.env.example:15`

## Medium-risk issues
- Test coverage is thin relative to repo size:
  - backend has 19 modules in `backend/pom.xml:22`, but only 8 Java test files found
  - frontend coverage leaves many routes/pages/services at 0%
- Microservice breadth is too wide for current hardening level:
  - many services compile, but several business integrations are placeholders
- Dev startup automation is incomplete:
  - `scripts/start-dev.bat:49` starts only registry/config/gateway/user/product/cart + frontend
- CI/CD is incomplete:
  - Docker build/push workflow still commented out in `.github/workflows/ci.yml:154`

## Hard truth
- Repo is in a decent demo/dev state, not a production-ready state.
- The dangerous part is not failing builds. The dangerous part is false confidence: green tests mostly prove a narrow slice, not end-to-end business correctness.
- Biggest gap is not architecture; it is unfinished integration + shallow verification.

## Recommended path
1. Freeze scope and choose 3-5 truly critical flows only: auth, browse, cart, checkout, payment callback.
2. Finish real integrations for those flows before adding more services/features.
3. Remove dev-default secrets/credentials and stop relying on `ddl-auto: update` outside local-only mode.
4. Add regression tests around cart, payment refund/callback, inventory validation, and password reset.
5. Decide honestly whether all current microservices are needed now; some may be premature YAGNI overhead.

## Success criteria
- Critical flows work without TODO/stub branches
- No hardcoded/default credentials in git-tracked runtime config
- Coverage improves on business-critical modules, not vanity-wide
- One command can boot the minimum viable stack for local verification
- CI validates the same critical flows the team actually depends on
