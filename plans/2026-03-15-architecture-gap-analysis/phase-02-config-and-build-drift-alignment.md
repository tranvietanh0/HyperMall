# Phase 02: Config and Build Drift Alignment

## Context links
- `backend/pom.xml`
- `backend/ai-service/pom.xml`
- `backend/config-server/src/main/resources/configurations/application.yml`
- `infrastructure/docker/docker-compose.dev.yml`
- `plans/2026-03-15-microservices-platform-research/research/260315-spring-cloud-microservices-best-practices.md`

## Overview with date/priority/statuses
- Date: 2026-03-15
- Priority: High
- Plan status: Drafted
- Implementation status: Not started

## Key Insights
- Parent build omits `ai-service` even though module scaffold exists.
- Config-server coverage misses `inventory`, `shipping`, `promotion`, `review`, `search`, `notification`, and `ai-service`.
- Shared config still uses `ddl-auto=update` and debug-heavy logging, unsafe as shared defaults.

## Requirements
- Reconcile canonical service roster across docs, Maven modules, config-server files, and runtime expectations.
- Move unsafe shared defaults behind dev-only overrides or explicit service opt-in.
- Keep local startup boring: missing config must fail loudly unless a service is intentionally inactive.

## Architecture
- Define one service inventory matrix: active, scaffolded, dormant, retired.
- Match each active service with parent-module entry, config file, port, datasource contract, and gateway ownership.
- Separate shared baseline config from environment-specific overrides; secrets stay outside git-tracked config.

## Related code files
- `backend/pom.xml`
- `backend/ai-service/pom.xml`
- `backend/config-server/src/main/resources/configurations/application.yml`
- `backend/config-server/src/main/resources/configurations/api-gateway.yml`
- `infrastructure/docker/docker-compose.dev.yml`
- `docs/codebase-summary.md`

## Implementation Steps
1. Produce service inventory matrix from repo contents, build files, config-server files, and docs.
2. Decide per missing service: add module/config support now, or mark explicitly out of scope.
3. Update parent POM and config-server coverage to match the approved service roster.
4. Split shared config defaults into safe baseline vs dev-only overrides for schema changes and verbose logs.
5. Add startup/build checks that fail when active services lack module inclusion or config coverage.

## Todo list
- [ ] Approve active-service list
- [ ] Resolve `ai-service` status in parent build
- [ ] Create missing service config files or mark dormant intentionally
- [ ] Remove shared `ddl-auto=update` from non-dev baseline
- [ ] Reduce shared debug logging defaults

## Success Criteria
- Every active service is buildable, configurable, and represented in docs consistently.
- Shared config defaults are safe for non-dev use.
- Missing config or module drift fails in CI/startup instead of surfacing late.

## Risk Assessment
- Risk is exposing incomplete service scaffolds when inventory becomes explicit.
- Mitigate by classifying dormant services instead of forcing all scaffolds live immediately.

## Security Considerations
- Do not move secrets into config-server to fix drift quickly.
- Review config import mode so production does not silently boot with partial defaults.

## Next steps
- Once roster is stable, Phase 3 can align frontend routes with only supported backend capabilities.

## Unresolved Questions
- Are the missing service config files blocked by unfinished implementations or by accidental omission?
- Should inactive scaffolds stay in repo, or be excluded from docs and release expectations?
