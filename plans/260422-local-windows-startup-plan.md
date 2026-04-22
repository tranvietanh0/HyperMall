# Local Windows startup hardening plan

## Overview

Muc tieu la sua toi thieu de local stack subset thuc su de chay tren may Windows: infra Docker + `service-registry` + `config-server` + `api-gateway` + `user-service` + `product-service` + `cart-service` + frontend. Nguyen nhan goc la script startup hien tai khong nap `backend/.env` vao process Maven, nen `SPRING_PROFILES_ACTIVE=dev` va `JWT_SECRET` khong duoc ap dung; dong thoi Docker daemon chi duoc check tho, chua fail-fast kem huong dan thao tac ro rang.

## Requirements

### Functional

- `scripts/start-dev.bat` tu dong nap bien tu `backend/.env` truoc khi build va truoc khi `mvn spring-boot:run`.
- Script Windows fail-fast neu Docker daemon chua san sang, truoc khi chay compose hay Maven.
- Tai lieu local startup huong dan dung mot flow nhat quan cho Windows, co link verify that.
- Flow chay local subset phai bao dam `service-registry` co the boot duoc ma khong can user tu set `EUREKA_PASSWORD` bang tay.

### Non-functional

- Diff nho, khong doi kien truc service, khong them cong cu moi.
- An toan voi repo: khong commit secret moi, uu tien dung gia tri da co trong `backend/.env.example`.
- Giu startup order hien tai va subset service hien tai.

## Architecture

- Chon chien luoc sua o tang script + docs, khong sua logic Java neu khong can.
- `backend/.env` la single source of truth cho local backend env; `start-dev.bat` se parse file nay va `set` vao shell hien tai truoc khi goi Maven.
- Moi cua so `cmd` duoc `start` cho tung service se nhan env qua `set VAR=... && mvn spring-boot:run`, tranh phu thuoc env cua terminal goc.
- Docker check tach thanh 2 buoc:
  1. check CLI co ton tai;
  2. check `docker info`/`docker version` thanh cong de xac nhan daemon dang chay.
- Docs chi dan ro: neu Docker daemon tat thi repo khong tu sua duoc, user phai mo Docker Desktop roi chay lai script.

## Implementation Steps

1. Xac nhan pham vi startup subset va env toi thieu
   - Giu nguyen subset dang duoc script start: registry, config-server, gateway, user, product, cart, frontend.
   - Ghi ro env toi thieu cho subset: `SPRING_PROFILES_ACTIVE=dev`, `JWT_SECRET`; tuy chon `EUREKA_USERNAME`/`EUREKA_PASSWORD` neu user override.

2. Hardening `scripts/start-dev.bat`
   - Them ham/doc block nap `backend/.env` an toan bang `for /f`, bo qua dong trong va comment.
   - Sau khi nap env, validate fail-fast cac bien toi thieu: it nhat `SPRING_PROFILES_ACTIVE` va `JWT_SECRET`.
   - Chuan hoa command start tung service thanh dang `cmd /k "set SPRING_PROFILES_ACTIVE=... && set JWT_SECRET=... && mvn spring-boot:run"` hoac ke thua full env da duoc `set` trong parent shell.
   - In ra thong diep ro rang neu `backend/.env` khong ton tai: copy tu `backend/.env.example`.
   - Cai tien Docker fail-fast: thong bao rang can mo Docker Desktop va doi daemon ready truoc khi script tiep tuc.

3. Xem xet `scripts/start-dev.sh`
   - Dong bo voi Windows o muc co ban de giam drift: source `backend/.env` truoc khi build va run, va cai tien thong diep Docker.
   - Khong mo rong pham vi, chi giu parity toi thieu voi Windows de docs khong mau thuan.

4. Cap nhat tai lieu local run
   - Sua `docs/LOCAL_TESTING.md` thanh flow uu tien cho Windows: `scripts\start-dev.bat`, Docker Desktop truoc, env file o `backend/.env`.
   - Sua `docs/DEPLOYMENT.md` quick-start/backend sections de bo huong dan `mvn spring-boot:run` tran, thay bang ghi chu phai nap env hoac dung startup script.
   - Chinh lai link verify thuc te: frontend `http://localhost:3000`, Eureka `http://localhost:8761`, gateway health `http://localhost:8080/actuator/health`, products `http://localhost:8080/api/products`, categories `http://localhost:8080/api/categories`.

5. Kiem tra va validation
   - Dry-run tren Windows: tat Docker de xac nhan script fail-fast dung thong diep.
   - Bat Docker, chay script va xac nhan `service-registry` boot voi profile `dev`.
   - Xac nhan frontend vao duoc va gateway/product endpoint tra ket qua.

## Files to Modify/Create/Delete

- Modify `scripts/start-dev.bat`
- Modify `scripts/start-dev.sh`
- Modify `docs/LOCAL_TESTING.md`
- Modify `docs/DEPLOYMENT.md`
- Optional modify `README.md` neu can them 1 dong tro den startup script Windows an toan; chi lam neu sau khi sua docs van con drift

## Testing Strategy

- Script validation:
  - Windows PowerShell/CMD: chay `scripts\start-dev.bat` khi Docker daemon tat, ky vong exit code `1` va message huong dan mo Docker Desktop.
  - Windows PowerShell/CMD: chay khi `backend/.env` bi doi ten, ky vong fail-fast va goi y copy tu `backend/.env.example`.
- Runtime validation:
  - `http://localhost:8761` load duoc dashboard.
  - `http://localhost:8080/actuator/health` tra `UP` hoac payload health hop le.
  - `http://localhost:8080/api/products` va `http://localhost:8080/api/categories` tra response thanh cong.
  - `http://localhost:3000` load frontend khong loi proxy co ban.

## Security Considerations

- Khong dua secret moi vao repo; tai su dung `backend/.env.example` va `backend/.env` hien co.
- Khong in gia tri `JWT_SECRET` ra console/log.
- Neu script validate env, chi thong bao ten bien bi thieu, khong dump toan bo env.

## Performance Considerations

- Khong thay doi runtime service.
- Fail-fast som giup giam thoi gian cho dev thay vi build backend roi moi phat hien Docker/env sai.
- Khong them retry phuc tap; chi giu wait MySQL hien co.

## Risks & Mitigations

- Risk: parser `.env` trong batch de vo voi gia tri co ky tu dac biet.
  - Mitigation: chi support format `KEY=VALUE` don gian, document ro, test voi `JWT_SECRET` base64 hien tai.
- Risk: env duoc `set` trong parent shell nhung khong di theo cua so `start` tren mot so case quoting.
  - Mitigation: nhung ro `set ... && mvn spring-boot:run` trong moi lenh `start` quan trong.
- Risk: docs van drift voi cong thuc startup thuc te.
  - Mitigation: sua dong thoi `docs/DEPLOYMENT.md` va `docs/LOCAL_TESTING.md`, uu tien script thay vi manual plain Maven.
- Risk: user ky vong full stack thay vi subset.
  - Mitigation: ghi ro trong docs/test links rang script chi bao phu subset local can thiet, service khac start them thu cong.

## TODO Tasks

- [ ] Cap nhat `scripts/start-dev.bat` de load `backend/.env` va validate env toi thieu
- [ ] Them Docker daemon fail-fast ro rang trong `scripts/start-dev.bat`
- [ ] Dong bo `scripts/start-dev.sh` o muc parity toi thieu
- [ ] Sua `docs/LOCAL_TESTING.md` theo flow Windows thuc te
- [ ] Sua `docs/DEPLOYMENT.md` de bo plain `mvn spring-boot:run` khong env
- [ ] Verify 4 link thuc te: frontend, Eureka, gateway health, products/categories
