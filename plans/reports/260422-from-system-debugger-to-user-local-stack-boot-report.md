# HyperMall local stack boot debug report

## Executive summary

- Local stack khong boot day du vi co 2 nhom blocker chinh: cau hinh local khong kich hoat profile `dev`/khong nap env khi chay `mvn spring-boot:run`, va ha tang Docker dang tat nen MySQL/Redis khong co san.
- Blocker dau tien co the fix trong repo (script/docs/cach nap env). Blocker thu hai phu thuoc may local.
- Frontend khong phai blocker chinh: mac dinh dung `/api` va proxy sang gateway `http://localhost:8080`.

## Findings

### 1. `spring-boot:run` dang chay voi base config thay vi `dev`

- `backend/service-registry/src/main/resources/application.yml:9` va `backend/service-registry/src/main/resources/application.yml:10` bat buoc `EUREKA_USERNAME`/`EUREKA_PASSWORD`.
- Default local cho 2 gia tri nay chi nam trong `backend/service-registry/src/main/resources/application-dev.yml:4` va `backend/service-registry/src/main/resources/application-dev.yml:5`.
- Script khoi dong dang goi `mvn spring-boot:run` truc tiep, khong set profile: `scripts/start-dev.bat:54`, `scripts/start-dev.sh:47`.
- File env co `SPRING_PROFILES_ACTIVE=dev` nhung chi nam trong file `backend/.env:18`; script hien tai khong `set`/`export` file nay truoc khi chay Maven.

### 2. Cung van de do profile/env voi config-server va gateway

- `backend/config-server/src/main/resources/application.yml:11` va `backend/config-server/src/main/resources/application.yml:12` bat buoc `CONFIG_USERNAME`/`CONFIG_PASSWORD`.
- Default local chi co trong `backend/config-server/src/main/resources/application-dev.yml:4`, `backend/config-server/src/main/resources/application-dev.yml:5`, `backend/config-server/src/main/resources/application-dev.yml:10`.
- `backend/api-gateway/src/main/resources/application.yml:8` dung `CONFIG_SERVER_USERNAME`/`CONFIG_SERVER_PASSWORD`; `backend/api-gateway/src/main/resources/application.yml:40` bat buoc `JWT_SECRET`.
- Default local cho config import/Eureka cua gateway chi co trong `backend/api-gateway/src/main/resources/application-dev.yml:3` va `backend/api-gateway/src/main/resources/application-dev.yml:11`.

### 3. `backend/.env` khong du de chay local neu khong duoc source vao process

- `backend/.env:15` chua `JWT_SECRET`, `backend/.env:18` chua `SPRING_PROFILES_ACTIVE=dev`.
- Tai lieu quick start van huong dan chay plain `mvn spring-boot:run`: `docs/DEPLOYMENT.md:61`, `docs/DEPLOYMENT.md:62`, `docs/DEPLOYMENT.md:63`, `docs/DEPLOYMENT.md:64`, `docs/DEPLOYMENT.md:65`, `docs/DEPLOYMENT.md:66`.
- Nghia la local env file hien co khong tu dong duoc ap dung vao cac process backend.

### 4. Docker daemon dang tat chan infra toi thieu

- Script startup dung `docker info` de gate ngay tu dau: `scripts/start-dev.bat:11`, `scripts/start-dev.sh:16`.
- Compose dev chua MySQL va Redis: `infrastructure/docker/docker-compose.dev.yml:7`, `infrastructure/docker/docker-compose.dev.yml:20`.
- `user-service` can MySQL/Redis: `backend/user-service/src/main/resources/application.yml:15`, `backend/user-service/src/main/resources/application.yml:29`.
- `product-service` can MySQL/Redis: `backend/product-service/src/main/resources/application.yml:15`, `backend/product-service/src/main/resources/application.yml:29`.
- `cart-service` can Redis: `backend/cart-service/src/main/resources/application.yml:15`.
- `api-gateway` can Redis rate limiter: `backend/api-gateway/src/main/resources/application.yml:33`, `backend/api-gateway/src/main/resources/application.yml:34`, `backend/api-gateway/src/main/java/com/hypermall/gateway/config/RateLimitConfig.java:33`.

### 5. Frontend khong phai blocker chinh

- Frontend mac dinh goi `/api`: `frontend/hypermall-web/src/config/api.config.ts:11`.
- Vite proxy `/api` sang gateway `http://localhost:8080`: `frontend/hypermall-web/vite.config.ts:23`, `frontend/hypermall-web/vite.config.ts:24`.
- Co do lech tai lieu: docs ghi dev server `5173` (`docs/DEPLOYMENT.md:240`) nhung config that la `3000` (`frontend/hypermall-web/vite.config.ts:21`). Day la drift tai lieu, khong phai blocker boot chinh.

## Minimal env/config needed

- `SPRING_PROFILES_ACTIVE=dev`
- `JWT_SECRET=<base64-secret>`
- Neu khong dung profile `dev`, phai co them: `EUREKA_USERNAME`, `EUREKA_PASSWORD`, `CONFIG_USERNAME`, `CONFIG_PASSWORD`, `CONFIG_SERVER_USERNAME`, `CONFIG_SERVER_PASSWORD`
- Infra toi thieu cho subset frontend + gateway + user/product/cart: MySQL (`3306`) va Redis (`6379`)

## Recommended startup sequence

1. Bat Docker Desktop / Docker daemon.
2. Start infra toi thieu (`docker-compose.dev.yml`) de co MySQL + Redis.
3. Export env local cho shell/backend process, it nhat `SPRING_PROFILES_ACTIVE=dev` va `JWT_SECRET`.
4. Start `service-registry` (`8761`).
5. Start `config-server` (`8888`).
6. Start `api-gateway` (`8080`).
7. Start `user-service` (`8081`), `product-service` (`8082`), `cart-service` (`8083`).
8. Start frontend (`3000`).

## Test URLs once unblocked

- Frontend: `http://localhost:3000`
- Eureka: `http://localhost:8761`
- Gateway health: `http://localhost:8080/actuator/health`
- Products via gateway: `http://localhost:8080/api/products`
- Categories via gateway: `http://localhost:8080/api/categories`
