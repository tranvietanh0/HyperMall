# Ke hoach MVP Seller cho HyperMall

## Overview

MVP seller duoc de xuat theo huong "seller center co the van hanh duoc" thay vi mo rong thanh marketplace day du. Pham vi MVP tap trung vao 4 nang luc can thiet: onboarding ho so shop, dashboard/trang thai duyet, quan ly san pham cua shop, va quan ly don hang cua shop. Admin giu vai tro phe duyet/tam khoa seller.

Khuyen nghi chot scope MVP nhu sau:

- Seller dang nhap voi role `SELLER` co the tao ho so shop neu chua co.
- Seller co khu vuc rieng `/seller/*` de xem trang thai, cap nhat thong tin shop, CRUD san pham, va theo doi/cap nhat trang thai don hang.
- Admin page `/admin/sellers` bo mock data, dung API that de xem danh sach, loc theo status, va approve/suspend.
- Khong dua vao MVP: payout, bao cao nang cao, chat seller-buyer, review reply, kho van/ship workflow nang cao, media upload thuc su, analytics-service.

## Requirements

### Functional requirements

1. Khi user co role `SELLER` dang nhap lan dau, he thong xac dinh seller profile da ton tai hay chua.
2. Neu chua co profile, user duoc dieu huong den seller onboarding de tao shop.
3. Seller co the xem dashboard co thong tin co ban: trang thai shop, slug, ngay tham gia, tong san pham, tong followers.
4. Seller ACTIVE co the tao, sua, xoa mem, va xem danh sach san pham cua minh qua `product-service`.
5. Seller ACTIVE co the xem danh sach don hang cua minh va cap nhat order status theo luong co san trong `order-service`.
6. Admin co the xem danh sach seller that, tim kiem, loc, approve, suspend tu `seller-service`.
7. Public site co the tiep tuc doc seller profile theo `id`/`slug`; neu can link sang san pham seller thi dung `userId` hien co de tranh migration lon.

### Non-functional requirements

1. Giu kien truc microservice hien tai, khong them framework lon moi.
2. Tuan thu `ApiResponse<T>`, `PageResponse<T>`, DTO layer, validation, MapStruct, strict TypeScript.
3. Bao ve seller endpoints khoi truy cap trai phep, khong chi gate bang UI.
4. Thay doi theo huong KISS/YAGNI: tai su dung `seller-service`, `SellerProductController`, `SellerOrderController`, `ProtectedRoute`, `api.service.ts`.
5. Co test muc tieu cho seller-service, frontend seller services/pages, va luong admin approve.

## Architecture

### 1. Scope kien truc MVP

MVP nen dung mo hinh "frontend composition + backend guard".

- `seller-service`: nguon su that cho seller profile, onboarding, status, admin approval.
- `product-service`: tiep tuc giu CRUD san pham seller, nhung phai xac thuc them seller profile ton tai va dang `ACTIVE` truoc khi thao tac.
- `order-service`: tiep tuc giu seller order workflow, va phai xac thuc seller dang `ACTIVE`.
- Frontend React: them seller route tree rieng, service client rieng, page onboarding/dashboard/products/orders/settings.

### 2. Dinh danh seller can giu on dinh trong MVP

Hien tai co 2 dinh danh seller khac nhau:

- `seller-service` co `Seller.id` (id ho so shop) va `Seller.userId` (chu shop).
- `product-service` va `order-service` dang dung truong `sellerId` nhung thuc chat dang nhan `currentUser.getId()` -> tuc la `userId` cua seller.

Khuyen nghi MVP:

- **Khong migration du lieu `product-service`/`order-service` sang `Seller.id`**.
- Chinh thuc hoa quy uoc: trong product/order, `sellerId` hien tai duoc xem la `sellerUserId`.
- Tren frontend seller/public pages, khi can lay san pham theo seller thi dung `seller.userId` tu `SellerResponse`.
- Ghi ro quy uoc nay trong docs/API va docs/system-architecture de tranh nham lan.

Ly do: doi sang `Seller.id` se keo theo migration schema, checkout, product detail, order creation, va rui ro vo du lieu cao; khong phu hop MVP.

### 3. Guard seller status o backend

Khong nen chi gate bang `ProtectedRoute` vi seller `PENDING`/`SUSPENDED` van co JWT role `SELLER`.

De xuat guard toi thieu:

- Them internal endpoint trong `seller-service` tra ve seller profile theo `userId` va status.
- `product-service` va `order-service` goi endpoint nay qua `RestClient` truoc khi cho phep seller thao tac.
- Dung internal shared token (`X-Internal-Token`) tu config-server de bao ve internal endpoint.

Pseudo flow:

```text
Seller request -> product/order service -> SellerStatusClient
  -> seller-service /api/internal/sellers/users/{userId}
  -> require status == ACTIVE
  -> allow business action
```

### 4. Frontend seller center

Them route tree rieng ben canh admin:

- `/seller` -> dashboard
- `/seller/onboarding` -> tao/cap nhat ho so ban dau
- `/seller/products` -> danh sach san pham
- `/seller/products/new` -> tao san pham
- `/seller/products/:id/edit` -> sua san pham
- `/seller/orders` -> danh sach don hang
- `/seller/settings` -> thong tin shop

Routing rules:

- `requiredRole="SELLER"` cho toan bo seller area.
- Them seller bootstrap/guard component:
  - co role `SELLER` + chua co seller profile -> redirect `/seller/onboarding`
  - co profile `PENDING`/`SUSPENDED` -> cho vao dashboard/settings nhung khoa products/orders actions
  - co profile `ACTIVE` -> truy cap day du

### 5. Seller dashboard cho MVP

Khong dung analytics-service trong MVP. Dashboard can ghep du lieu tu cac API san co:

- `GET /api/sellers/me/dashboard` -> metadata shop
- `GET /api/seller/products` -> tong so san pham (`totalElements`)
- `GET /api/seller/orders` -> don moi nhat va tong don theo trang thai (co the goi 1-3 lan voi filter `status` neu can)

UI hien thi:

- status badge (`PENDING`, `ACTIVE`, `SUSPENDED`)
- card tong san pham
- card tong followers
- card don cho xu ly
- bang 5 don hang moi nhat
- CTA ro rang cho tung trang thai (`Hoan tat ho so`, `Cho admin duyet`, `Lien he ho tro`)

### 6. Admin seller page

Trang `frontend/hypermall-web/src/pages/Admin/Sellers/index.tsx` hien dang mock va dang doi contract khong ton tai (`ownerName`, `email`, `phone`).

Khuyen nghi MVP:

- Doi UI sang contract thuc te cua `SellerResponse`.
- Hien `shopName`, `shopSlug`, `businessType`, `status`, `rating`, `totalProducts`, `totalFollowers`, `createdAt`.
- Neu can thong tin owner trong MVP, hien `userId` thay vi co gang join `user-service`.
- Nut approve/suspend goi `PUT /api/admin/sellers/{id}/status`.
- Search/filter dung `GET /api/admin/sellers/search`.

Dieu nay giup tranh tao coupling moi voi `user-service` chi de doan MVP.

## Implementation Steps

### Phase 1 - Chot contract va dong bo identifier

1. Ra soat va cap nhat docs de ghi ro `seller-service.id` khac voi `product/order sellerId (= seller userId)`.
2. Xac nhan frontend public seller/product detail khi can lay san pham seller se dung `SellerResponse.userId`.
3. Mo rong `docs/API.md` cho seller onboarding, seller dashboard, admin seller APIs, va ghi chu ve seller identifier.

### Phase 2 - Hoan thien backend seller-service cho onboarding/admin/internal check

1. Bo sung DTO nhe cho internal status lookup, vi du `InternalSellerStatusResponse`.
2. Bo sung endpoint internal tra ve seller theo `userId`.
3. Bao ve internal endpoint bang shared token trong header, khong public ra internet.
4. Bo sung service method `getSellerByUserId` va `requireActiveSellerByUserId` (neu muon tai su dung).
5. Can nhac bo sung validation khi register/update:
   - slug duy nhat
   - bank/tax/business fields dai hop le
   - chan register trung profile
6. Giu logic admin status update o `AdminSellerController`, bo sung test cho register/search/updateStatus/internal lookup.

### Phase 3 - Guard seller status trong product-service va order-service

1. Them `seller-service` base URL + internal token vao `product-service.yml` va `order-service.yml`.
2. Tao `SellerStatusClient`/`SellerGuardService` nho gon trong moi service bang `RestClient`.
3. Trong `SellerProductController` flow:
   - validate seller ACTIVE truoc `create/update/delete/getMyProducts`
4. Trong `SellerOrderController` flow:
   - validate seller ACTIVE truoc `getSellerOrders/updateOrderStatus`
5. Dinh nghia loi ro rang:
   - `PENDING` -> `ForbiddenException` voi thong diep "Seller account is pending approval"
   - `SUSPENDED` -> `ForbiddenException` voi thong diep "Seller account is suspended"
   - khong co profile -> `ForbiddenException`/`ResourceNotFoundException` phu hop

### Phase 4 - Frontend seller module

1. Them seller types vao `src/types`:
   - `SellerProfile`
   - `SellerDashboard`
   - `CreateSellerRequest`
   - `UpdateSellerRequest`
   - `SellerProductFormValues`
2. Them seller API endpoints vao `src/config/api.config.ts`.
3. Tao service clients moi:
   - `seller.service.ts`
   - `seller-product.service.ts`
   - `seller-order.service.ts`
4. Them route config moi `SellerRoutes.tsx` va gan vao `src/App.tsx`.
5. Tao `SellerLayout` va `SellerGuard`.
6. Tao cac page MVP:
   - Dashboard
   - Onboarding/Profile
   - Product List
   - Product Form (create/edit)
   - Order List
7. Tai su dung component chung (`Button`, `Input`, `Loading`, `Modal`) va khong tao design system moi.

### Phase 5 - Chuyen admin seller page tu mock sang API that

1. Loai bo `setTimeout` va mock sellers.
2. Goi `seller.service.searchAdminSellers(...)`.
3. Map `SellerResponse` sang UI model toi gian.
4. Noi search, status filter, approve, suspend vao API.
5. Bo sung loading/error/empty states.

### Phase 6 - Seller UX gating

1. Sau login/register voi role `SELLER`, dieu huong ve `/seller` thay vi `/`.
2. Neu chua co profile -> `/seller/onboarding`.
3. Neu profile `PENDING` -> dashboard hien banner "Cho duyet" va disable products/orders CTA.
4. Neu profile `SUSPENDED` -> dashboard hien banner "Tam khoa" va khoa thao tac ghi.
5. Neu `ACTIVE` -> mo day du navigation.

### Phase 7 - Testing va docs

1. Backend tests:
   - `seller-service`: register, getMyProfile, updateStatus, internal lookup.
   - `product-service`: seller ACTIVE/PENDING/SUSPENDED guards.
   - `order-service`: seller ACTIVE/PENDING/SUSPENDED guards.
2. Frontend tests:
   - `SellerGuard`
   - onboarding redirect
   - admin seller page fetch/approve flow
   - seller services request mapping
3. Cap nhat docs: `docs/API.md`, `docs/system-architecture.md`, `docs/codebase-summary.md` neu thay doi surface area.

## Files to Modify/Create/Delete

### Backend - seller-service

- `backend/seller-service/src/main/java/com/hypermall/seller/service/SellerService.java` - them lookup theo `userId`, internal response, helper status.
- `backend/seller-service/src/main/java/com/hypermall/seller/controller/SellerController.java` - neu can bo sung endpoint profile/status phuc vu frontend ro hon.
- `backend/seller-service/src/main/java/com/hypermall/seller/controller/AdminSellerController.java` - giu approve/suspend/search, co the bo sung filter nho.
- `backend/seller-service/src/main/java/com/hypermall/seller/config/SecurityConfig.java` - them rule cho internal endpoint + internal token filter/validator.
- `backend/seller-service/src/main/java/com/hypermall/seller/repository/SellerRepository.java` - them `findByUserId`/query support neu can.
- `backend/seller-service/src/main/java/com/hypermall/seller/dto/response/InternalSellerStatusResponse.java` - moi.
- `backend/seller-service/src/main/java/com/hypermall/seller/controller/InternalSellerController.java` - moi.
- `backend/seller-service/src/test/java/...` - moi cho service/controller.

### Backend - product-service

- `backend/product-service/src/main/java/com/hypermall/product/controller/SellerProductController.java` - co the giu controller mong, delegate guard vao service/helper.
- `backend/product-service/src/main/java/com/hypermall/product/service/ProductService.java` - goi guard truoc thao tac seller.
- `backend/product-service/src/main/java/com/hypermall/product/config/SecurityConfig.java` - giu role guard, khong thay doi lon.
- `backend/product-service/src/main/java/com/hypermall/product/client/SellerStatusClient.java` - moi.
- `backend/product-service/src/main/java/com/hypermall/product/config/SellerClientConfig.java` - moi.
- `backend/product-service/src/test/java/...` - moi/bo sung.
- `backend/config-server/src/main/resources/configurations/product-service.yml` - them base URL/internal token seller-service.

### Backend - order-service

- `backend/order-service/src/main/java/com/hypermall/order/controller/SellerOrderController.java` - giu surface API, delegate guard vao service/helper.
- `backend/order-service/src/main/java/com/hypermall/order/service/OrderService.java` - goi guard truoc thao tac seller.
- `backend/order-service/src/main/java/com/hypermall/order/client/SellerStatusClient.java` - moi.
- `backend/order-service/src/main/java/com/hypermall/order/config/SellerClientConfig.java` - moi.
- `backend/order-service/src/test/java/...` - moi/bo sung.
- `backend/config-server/src/main/resources/configurations/order-service.yml` - them base URL/internal token seller-service.

### Frontend

- `frontend/hypermall-web/src/App.tsx` - gan seller route tree.
- `frontend/hypermall-web/src/config/api.config.ts` - them seller/admin seller endpoints.
- `frontend/hypermall-web/src/routes/SellerRoutes.tsx` - moi.
- `frontend/hypermall-web/src/routes/ProtectedRoute.tsx` - giu role guard, khong nhat thiet sua lon.
- `frontend/hypermall-web/src/components/seller/SellerLayout.tsx` - moi.
- `frontend/hypermall-web/src/components/seller/SellerGuard.tsx` - moi.
- `frontend/hypermall-web/src/pages/Seller/Dashboard/index.tsx` - moi.
- `frontend/hypermall-web/src/pages/Seller/Onboarding/index.tsx` - moi.
- `frontend/hypermall-web/src/pages/Seller/Products/index.tsx` - moi.
- `frontend/hypermall-web/src/pages/Seller/Products/ProductForm.tsx` - moi.
- `frontend/hypermall-web/src/pages/Seller/Orders/index.tsx` - moi.
- `frontend/hypermall-web/src/pages/Seller/Settings/index.tsx` - moi.
- `frontend/hypermall-web/src/pages/Admin/Sellers/index.tsx` - bo mock, dung API that.
- `frontend/hypermall-web/src/services/seller.service.ts` - moi.
- `frontend/hypermall-web/src/services/seller-product.service.ts` - moi.
- `frontend/hypermall-web/src/services/seller-order.service.ts` - moi.
- `frontend/hypermall-web/src/services/index.ts` - export them services moi.
- `frontend/hypermall-web/src/types/seller.types.ts` - moi.
- `frontend/hypermall-web/src/types/index.ts` - export types moi.
- `frontend/hypermall-web/src/hooks/useAuth.ts` - redirect seller sau login/register neu can.
- `frontend/hypermall-web/src/store/slices/authSlice.ts` - co the khong can sua neu redirect xu ly o hook.
- `frontend/hypermall-web/src/test/...` - bo sung test.

### Documentation

- `docs/API.md`
- `docs/system-architecture.md`
- `docs/codebase-summary.md`
- `plans/260422-seller-mvp-plan.md`

### Delete

- Khong can xoa file nao trong MVP. Neu sau nay tach lai mock helpers o admin sellers thi co the xoa code mock noi bo trong file page hien tai.

## Testing Strategy

### Backend

- `seller-service`: unit test cho `registerSeller`, `getMySellerProfile`, `updateSellerStatus`, internal lookup theo `userId`.
- `product-service`: test seller guard cho `ACTIVE`, `PENDING`, `SUSPENDED`, `no profile`.
- `order-service`: test tuong tu cho list/update status.
- Chay uu tien theo module:
  - `mvn -pl seller-service test`
  - `mvn -pl product-service test`
  - `mvn -pl order-service test`

### Frontend

- Vitest cho seller services mapping va error handling.
- Component/page tests cho `SellerGuard`, onboarding redirect, pending banner, admin approve action.
- Chay muc tieu:
  - `npx vitest run src/pages/Admin/Sellers/index.test.tsx`
  - `npx vitest run src/components/seller/SellerGuard.test.tsx`
  - `npx vitest run src/services/seller.service.test.ts`

### Manual validation

1. Dang ky tai khoan `SELLER`.
2. Dang nhap -> redirect `/seller/onboarding`.
3. Tao shop -> status `PENDING`.
4. Admin approve tai `/admin/sellers`.
5. Seller vao lai `/seller`, tao san pham, xem don hang, doi status don.
6. Thu seller `SUSPENDED` va xac nhan bi chan backend, khong chi UI.

## Security Considerations

1. Khong tin UI role/status gating; backend phai validate seller status cho seller product/order APIs.
2. Internal seller status endpoint phai duoc bao ve bang token rieng hoac co che service-to-service toi thieu; khong expose cong khai.
3. Khong log bank account number, tax code day du, hoac internal token.
4. Admin seller endpoints phai tiep tuc yeu cau `ADMIN` role.
5. Neu sau nay can lay owner data tu `user-service`, phai them auth dung cach; khong goi endpoint "admin only" ma chua co `@PreAuthorize` thuc te.

## Performance Considerations

1. Dashboard frontend nen goi song song 2-3 API doc lap (`Promise.all`) thay vi tao endpoint tong hop moi cho MVP.
2. Admin seller search nen dung server-side filter/page thay vi load tat ca roi loc client-side.
3. Internal seller status lookup can timeout ngan va co fallback loi ro rang; khong retry vo toi va.
4. Truong hop admin page can fetch nhieu ban ghi, tranh waterfall requests sang `user-service` trong MVP.

## Risks & Mitigations

### Risk 1 - Nhieu "seller id" gay sai du lieu

- **Tac dong**: public seller page, product ownership, order ownership de bi map sai.
- **Mitigation**: chot quy uoc MVP: cross-service seller key = `userId`; cap nhat docs va frontend mapping ro rang.

### Risk 2 - Pending seller van tao/sua product neu chi gate UI

- **Tac dong**: vo quy trinh approve.
- **Mitigation**: them internal status guard trong product/order service.

### Risk 3 - Admin seller page doi contract khong ton tai

- **Tac dong**: phai chep them du lieu owner, phat sinh coupling khong can thiet.
- **Mitigation**: doi UI sang contract `SellerResponse` that; defer owner enrichment.

### Risk 4 - Dashboard so lieu seller bi stale

- **Tac dong**: `totalProducts` trong seller-service khong dong bo.
- **Mitigation**: MVP dashboard doc truc tiep tu product/order APIs, khong dua vao denormalized field de tinh KPI.

### Risk 5 - Scope phinh to sang analytics/payout

- **Tac dong**: tre MVP.
- **Mitigation**: khoa scope ngay tu dau, chi giu onboarding + products + orders + admin approval.

## Recommended Option and Trade-offs

### Option A - Frontend-only seller center, khong backend guard

- **Uu diem**: nhanh nhat.
- **Nhuoc diem**: khong an toan; seller `PENDING`/`SUSPENDED` van goi API truc tiep duoc.
- **Danh gia**: khong nen chon.

### Option B - Seller center + internal seller status guard bang `RestClient`

- **Uu diem**: scope vua phai, secure hon, khong can migration du lieu lon.
- **Nhuoc diem**: them coupling nhe giua services va can them internal token config.
- **Danh gia**: **khuyen nghi cho MVP**.

### Option C - Dong bo seller snapshot/event-driven sang product/order

- **Uu diem**: scale tot hon ve lau dai.
- **Nhuoc diem**: qua tam MVP, phuc tap hon rat nhieu.
- **Danh gia**: de sau MVP.

## Acceptance Criteria

- Seller role dang nhap vao `/seller` duoc dieu huong dung theo profile status.
- Seller chua duoc approve khong the CRUD san pham hoac xu ly don hang, ke ca khi goi API truc tiep.
- Seller ACTIVE tao/sua/xoa san pham thanh cong.
- Seller ACTIVE xem duoc danh sach don va update status hop le.
- Admin seller page khong con mock data, approve/suspend thanh cong.
- Docs/API ghi ro quy uoc seller identifier va luong MVP seller.

## TODO Tasks

- [ ] Chot quy uoc identifier: `Seller.id` vs `Seller.userId` va cap nhat docs
- [ ] Them internal seller status endpoint trong `seller-service`
- [ ] Them internal token auth cho internal endpoint
- [ ] Them seller status client vao `product-service`
- [ ] Them seller status client vao `order-service`
- [ ] Viet backend tests cho seller guard va seller-service
- [ ] Them `seller.types.ts` va seller services ben frontend
- [ ] Tao `SellerRoutes.tsx`, `SellerLayout`, `SellerGuard`
- [ ] Tao seller onboarding/dashboard/products/orders/settings pages
- [ ] Chuyen `Admin/Sellers` sang API that
- [ ] Them frontend tests cho seller guard va admin seller flow
- [ ] Cap nhat `docs/API.md`, `docs/system-architecture.md`, `docs/codebase-summary.md`
