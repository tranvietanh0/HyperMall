# Phase 2: Input Validation

**Date:** 2026-03-13
**Priority:** High
**Status:** Pending

## Overview

Add proper input validation across all microservices endpoints.

## Key Insights

- Inconsistent use of `@Valid` annotations on request bodies
- IDOR risk in user endpoints
- Missing Jakarta Validation annotations

## Requirements

1. Add `@Valid` to all POST/PUT endpoints
2. Add `@PreAuthorize` for ownership checks
3. Add validation DTOs in common-lib

## Implementation Steps

1. Audit all controllers for missing validation
2. Add validation annotations to request DTOs
3. Add ownership checks for sensitive endpoints
