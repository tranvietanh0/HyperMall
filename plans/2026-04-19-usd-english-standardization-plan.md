# Ke hoach chuan hoa USD va tieng Anh cho HyperMall

## Overview
- Muc tieu: (1) chuan hoa gia hien thi tren giao dien va thong diep huong nguoi dung sang USD/dollars, (2) quet toan bo du an de chuyen user-facing language sang tieng Anh.
- Pham vi la cross-cutting: frontend React, backend API messages, AI/notification content, docs, seed scripts, config/comments co anh huong van hanh.
- Nguyen tac: khong doi don vi tien goc trong database hay gateway thanh toan neu no la rang buoc nghiep vu/doi tac; uu tien doi presentation va message contract truoc.

## Requirements

### Functional
- Tat ca text hien thi cho nguoi dung tren web storefront/admin phai la tieng Anh nhat quan.
- Tat ca gia hien thi cho nguoi dung phai dung USD theo cung mot quy uoc dinh dang.
- API success/error messages huong ra client phai la tieng Anh.
- AI prompts, fallback text, suggested actions, notification titles/contents, email templates phai la tieng Anh.
- Docs van hanh/public docs va seed scripts/cofig co text huong nguoi dung/nguoi van hanh phai duoc chuyen sang tieng Anh.

### Non-functional
- Khong lam vo contract thanh toan ben thu ba (vi du VNPay van co the yeu cau `vnp_CurrCode=VND`).
- Khong doi nghia domain data neu chua co quyet dinh nghiep vu ve ty gia/chuyen doi gia.
- Giu thay doi nho, de review, de rollback.

## Architecture

### 1. Currency layer
- Tao mot presentation rule duy nhat cho frontend: mot utility/cau hinh currency trung tam (`USD`, locale uu tien `en-US`, 2 decimal places).
- Tat ca component/page/admin card/AI suggestion dung chung utility nay; cam noi chuoi `VND`, `vi-VN`, `toLocaleString('vi-VN')`, `Intl.NumberFormat('vi-VN', { currency: 'VND' })` truc tiep trong UI.
- Khong doi amount luu tru backend trong phase nay. Gia tri so hoc giu nguyen, chi doi cach hien thi va nhan currency. Neu nghiep vu hien tai dang luu theo VND, can xac nhan ro: phase nay la relabel-only hay convert-by-rate. Khuyen nghi: chi relabel sau khi business xac nhan; neu khong, phai bo sung exchange-rate source.

### 2. Language layer
- Frontend: thay text literal, placeholder, validation text, toast, aria-label, badge, fallback empty states, test assertions.
- Backend: thay message trong `ApiResponse`, `GlobalExceptionHandler`, exception throws, notification titles/content, AI fallback/suggested action labels.
- Docs/scripts/config: thay noi dung huong dan/seed text/comment huong van hanh; giu nguyen proper noun/ten dich vu ben thu ba.

### 3. Review strategy
- Tach 2 truc review:
  1. `display-currency` review cho tat ca noi render so/gia/date-locale.
  2. `language-contract` review cho tat ca string huong user/operator.
- Su dung grep-driven inventory truoc, sau do chot bang manual review cho cac vung nhay cam: AI, payment, notification, docs, validation/tests.

## Scope Breakdown

### Frontend
- Currency utility trung tam: `frontend/hypermall-web/src/utils/format.ts`, `frontend/hypermall-web/src/utils/format.test.ts`.
- Storefront pages/components co text/gia tien:
  - `frontend/hypermall-web/src/pages/Product/ProductListPage.tsx`
  - `frontend/hypermall-web/src/pages/Product/ProductDetailPage.tsx`
  - `frontend/hypermall-web/src/pages/Home/index.tsx`
  - `frontend/hypermall-web/src/pages/Checkout/components/CheckoutOrderSummary.tsx`
  - `frontend/hypermall-web/src/pages/Order/OrderDetailPage.tsx`
  - `frontend/hypermall-web/src/pages/Order/OrderSuccessPage.tsx`
  - `frontend/hypermall-web/src/components/product/ProductCard.tsx`
  - `frontend/hypermall-web/src/components/features/ai/AiChatWidget.tsx`
  - `frontend/hypermall-web/src/components/features/ai/AiChatLauncher.tsx`
  - `frontend/hypermall-web/src/components/features/ai/AiSuggestedProducts.tsx`
  - `frontend/hypermall-web/src/hooks/useAiChat.ts`
- Profile/Auth/Cart/Checkout co mat do tieng Viet cao:
  - `frontend/hypermall-web/src/pages/Profile/**`
  - `frontend/hypermall-web/src/pages/Auth/**`
  - `frontend/hypermall-web/src/pages/Cart/**`
  - `frontend/hypermall-web/src/pages/Checkout/**`
- Admin co currency + locale + text literal:
  - `frontend/hypermall-web/src/pages/Admin/Analytics/index.tsx`
  - `frontend/hypermall-web/src/pages/Admin/Orders/index.tsx`
  - `frontend/hypermall-web/src/pages/Admin/Products/index.tsx`
  - `frontend/hypermall-web/src/pages/Admin/Dashboard/index.tsx`
  - `frontend/hypermall-web/src/pages/Admin/Settings/index.tsx`
- Tests can cap nhat assertion label/message:
  - `frontend/hypermall-web/src/pages/Profile/ProfilePage.test.tsx`
  - `frontend/hypermall-web/src/utils/validation.test.ts`
  - `frontend/hypermall-web/src/utils/format.test.ts`

### Backend API messages
- Shared response surface:
  - `backend/common-lib/src/main/java/com/hypermall/common/dto/ApiResponse.java`
  - `backend/common-lib/src/main/java/com/hypermall/common/exception/GlobalExceptionHandler.java`
- Service-level exception/user-facing strings can quet va sua theo module, uu tien cac module buyer-facing:
  - `backend/user-service/**`
  - `backend/product-service/**`
  - `backend/cart-service/**`
  - `backend/order-service/**`
  - `backend/media-service/**`
  - `backend/seller-service/**`
  - `backend/analytics-service/**`
- AI va notification la scope rieng can review tay ky:
  - `backend/ai-service/src/main/java/com/hypermall/ai/service/ChatService.java`
  - `backend/ai-service/src/main/java/com/hypermall/ai/service/ProductGroundingService.java`
  - `backend/notification-service/src/main/java/com/hypermall/notification/service/NotificationService.java`
  - `backend/notification-service/src/main/resources/templates/email/notification.html`

### Docs
- Docs hien tai pha tron Anh/Viet, trong do `docs/LOCAL_TESTING.md` co nhieu noi dung tieng Viet.
- File uu tien:
  - `README.md`
  - `docs/LOCAL_TESTING.md`
  - `docs/API.md`
  - `docs/DEPLOYMENT.md`
  - `docs/system-architecture.md`
  - `docs/project-overview-pdr.md`
  - `docs/refactor-contract-matrix.md`

### Seed scripts / config
- Seed scripts co error/help text va seeded content can canh chinh:
  - `scripts/seed_products.py`
  - `scripts/seed_flash_sale.py`
- Config/comment huong van hanh:
  - `backend/config-server/src/main/resources/configurations/payment-service.yml`
- Payment integration khong nam trong scope doi USD hien thi neu la giao tiep voi doi tac:
  - `backend/payment-service/src/main/java/com/hypermall/payment/gateway/VNPayGateway.java`
  - `backend/payment-service/src/main/java/com/hypermall/payment/dto/VNPayCallbackRequest.java`

## Risky Areas & Trade-offs
- **Gia USD nhung du lieu goc co the la VND**: day la rui ro lon nhat. Neu chi doi label sang USD ma khong convert, UI sai nghia. Neu convert, can source ty gia, quy tac lam tron, thoi diem cap nhat, va anh huong order/payment/report.
- **Gateway thanh toan**: VNPay dang hardcode `vnp_CurrCode = VND` va `vnp_Locale = vn`; doi cai nay ma khong co xac nhan protocol se vo thanh toan. Khuyen nghi giu nguyen external payment currency o phase nay.
- **AI chat/product grounding**: backend dang nhan ca English va Vietnamese query; chuyen frontend sang English de, nhung xoa logic Vietnamese trong backend la quyet dinh san pham, khong nen auto lam trong dot nay.
- **Date/number locale**: doi `vi-VN` sang `en-US` khong chi doi ngon ngu ma con doi dau phan cach, thu tu ngay/thang. Can review UX va test snapshot/assertions.
- **Validation messages/tests**: thay text se lam fail test tim theo label/toast/error string.
- **Docs/seed content**: auto-replace de gay sai proper noun, ten thuong hieu, ten bang/field SQL, ten enum, code comments ky thuat.

## Recommended Implementation Order
1. Chot quyet dinh nghiep vu ve USD: `display-only relabel` hay `real currency conversion`.
2. Frontend currency foundation: sua `format.ts`, admin price helpers, grep cleanup cho `VND`/`vi-VN`/currency literals.
3. Frontend English pass: storefront -> checkout/cart/order -> profile/auth -> admin -> AI widget.
4. Backend shared contracts: `ApiResponse`, `GlobalExceptionHandler`, common exception messages.
5. Backend module sweep: user/product/cart/order/media/seller/analytics.
6. Backend manual pass cho AI, notification, email template, pay
