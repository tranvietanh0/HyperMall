# Phase 1: Security Hardening

**Date:** 2026-03-13
**Priority:** Critical
**Status:** DONE

## Overview

Address critical and high-priority security vulnerabilities identified in the codebase.

## Key Insights

- **16+ files** contain hardcoded JWT secrets as default values
- **XSS vulnerability** in product detail page allows arbitrary HTML injection
- **localStorage** used for token storage - vulnerable to XSS attacks

## Requirements

1. Remove hardcoded JWT secrets from all configuration files
2. Fix XSS vulnerability in ProductDetailPage
3. Add security headers to backend services

## Architecture

### Changes Needed

**Backend:**
- Add `@Bean` for SecurityHeadersFilter in common-lib
- Update all application.yml files to require env vars

**Frontend:**
- Install DOMPurify for HTML sanitization
- Replace dangerouslySetInnerHTML with sanitized content
- Consider httpOnly cookie option for token storage

## Related Code Files

- `frontend/hypermall-web/src/pages/Product/ProductDetailPage.tsx`
- `backend/*/src/main/resources/application.yml`
- `backend/common-lib/src/main/java/.../config/`

## Implementation Steps

### Step 1: Fix XSS Vulnerability (Frontend)
1. Install `dompurify` package
2. Create sanitization utility
3. Update ProductDetailPage to sanitize HTML

### Step 2: Add Security Headers (Backend)
1. Create SecurityHeadersFilter in common-lib
2. Configure filter in each service

### Step 3: Remove Hardcoded Secrets
1. Update all application.yml to use required env vars
2. Document required environment variables

## Todo List

- [ ] Install DOMPurify in frontend
- [ ] Create HTML sanitization utility
- [ ] Fix ProductDetailPage.tsx
- [ ] Add SecurityHeadersFilter
- [ ] Configure security headers
- [ ] Update application.yml files

## Success Criteria

- [ ] No hardcoded secrets in git-tracked files
- [ ] Product descriptions are sanitized before rendering
- [ ] Security headers present in all API responses

## Risk Assessment

- **Low Risk**: XSS fix is isolated to one component
- **Medium Risk**: Security header changes may affect existing functionality

## Security Considerations

- Test all endpoints after adding security headers
- Verify token refresh still works after any storage changes
