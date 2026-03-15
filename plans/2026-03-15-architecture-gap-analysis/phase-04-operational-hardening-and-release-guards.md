# Phase 04: Operational Hardening and Release Guards

## Context links
- `infrastructure/docker/docker-compose.dev.yml`
- `backend/api-gateway/src/main/resources/application.yml`
- `backend/config-server/src/main/resources/configurations/application.yml`
- `docs/codebase-summary.md`
- `plans/2026-03-15-microservices-platform-research/research/260315-spring-cloud-microservices-best-practices.md`

## Overview with date/priority/statuses
- Date: 2026-03-15
- Priority: Medium
- Plan status: Drafted
- Implementation status: Not started

## Key Insights
- Current local infra file starts infra only; platform readiness still depends on manual startup order and hidden config assumptions.
- Shared config and gateway posture need smoke checks, otherwise drift returns after refactors.
- Earlier phases reduce immediate risk; this phase keeps it from regressing.

## Requirements
- Add cheap, repeatable validation around gateway auth, config presence, service startup order, and frontend auth flows.
- Keep checks narrow and automatable; avoid a heavy new platform layer.
- Document release-time ownership and evidence required before adding services or routes.

## Architecture
- Use layered validation: static inventory checks, targeted build checks, startup smoke tests, then route/auth smoke tests.
- Keep local and CI checks aligned with the same service inventory matrix from Phase 2.
- Treat gateway route audit and config coverage audit as release gates, not optional docs work.

## Related code files
- `infrastructure/docker/docker-compose.dev.yml`
- `backend/pom.xml`
- `backend/api-gateway/src/main/resources/application.yml`
- `backend/config-server/src/main/resources/configurations/application.yml`
- `frontend/hypermall-web/src/services/api.service.ts`
- `docs/codebase-summary.md`

## Implementation Steps
1. Define minimal smoke suite: config-server reachable, registry auth works, gateway protected route rejects anonymous, frontend refresh failure logs out cleanly.
2. Add static checks for service inventory mismatches across parent POM, config-server files, and docs.
3. Document startup order, required env vars, and per-phase rollback points.
4. Add route/config review checklist for new services or gateway path changes.
5. Capture operating runbook updates after phases 1-3 complete.

## Todo list
- [ ] Define CI-safe smoke checks
- [ ] Add service inventory drift check
- [ ] Update startup and rollback docs
- [ ] Create release checklist for routes/config/auth changes
- [ ] Assign ownership for ongoing route and config reviews

## Success Criteria
- Regressions in route security, config coverage, or service inventory are caught before merge/release.
- Local and CI guidance match actual startup dependencies.
- Team has one release checklist for gateway/config/auth changes.

## Risk Assessment
- Risk is overbuilding test harnesses beyond current repo maturity.
- Mitigate by keeping checks narrow, fast, and focused on high-risk seams only.

## Security Considerations
- Smoke tests must use non-production credentials and no long-lived secrets.
- Do not expose sensitive config in logs while adding diagnostics.

## Next steps
- Close this phase by updating the architecture baseline docs and archiving the approved inventory matrix.

## Unresolved Questions
- Which checks belong in CI now vs manual release verification?
- Who owns ongoing service-inventory and gateway-route review after cleanup?
