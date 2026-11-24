# belajar-sesi3 — RESTful COA API, Validation & Error Handling

Implementasi **Sesi 3** (Enterprise Java & Spring Boot Engineering, Modul 01): REST CRUD *Chart of Accounts* dengan DTO record, Bean Validation, global exception handler (ProblemDetail), correlation ID, middleware (filter & interceptor), dan dokumentasi OpenAPI.

## Stack

| Komponen | Versi |
|---|---|
| Java | 25 |
| Spring Boot | 4.2.0 (Web MVC, Data JPA, Validation, Flyway) |
| PostgreSQL | 18 (Docker Compose / Testcontainers) |
| springdoc-openapi | 3.2.1 |

## Menjalankan

Syarat: JDK 25 dan Docker.

```bash
./mvnw spring-boot:run
```

Spring Boot Docker Compose otomatis menyalakan PostgreSQL dari `compose.yaml` dan menghubungkan datasource ke container tersebut. Port host container dibuat acak agar tidak bentrok dengan PostgreSQL lokal. Cek dengan `docker compose port postgres 5432`. Flyway menjalankan migration `V1_24112026_1242__create_account.sql` (tabel `account`) dan `V2_24112026_1242__seed_root_accounts.sql` (5 akun induk: 1000–5000). Format nama migration: `Vx_DDMMYYYY_HHMM__description.sql`.

Di luar Docker Compose, datasource dibaca dari environment `DB_URL`, `DB_USERNAME`, dan `DB_PASSWORD`.

| URL | Keterangan |
|---|---|
| http://localhost:8080/swagger-ui.html | Swagger UI (redirect ke `/swagger-ui/index.html`) |
| http://localhost:8080/v3/api-docs/1-coa | Spesifikasi grup Chart of Accounts |
| http://localhost:8080/v3/api-docs.yaml | Spesifikasi lengkap (YAML) |

Profile `prod` mematikan Swagger UI dan api-docs:

```bash
java -jar target/belajar-sesi3-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Endpoint

| Operasi | Method & URI | Status sukses | Idempotent |
|---|---|---|---|
| Buat akun | `POST /api/v1/accounts` | 201 Created + `Location` | Tidak |
| Daftar akun aktif | `GET /api/v1/accounts?page=0&size=20&sort=code,asc` | 200 OK | Ya |
| Detail akun | `GET /api/v1/accounts/{id}` | 200 / 404 | Ya |
| Ubah akun | `PUT /api/v1/accounts/{id}` | 200 OK | Ya |
| Nonaktifkan akun | `PATCH /api/v1/accounts/{id}/deactivate` | 204 No Content | Ya |

Aturan validasi:
- **Bean Validation (format):** `code` wajib 4 digit, `name` wajib dan maksimal 150 karakter, `type` wajib (`ASSET`, `LIABILITY`, `EQUITY`, `REVENUE`, `EXPENSE`).
- **Service (domain):** kode akun unik (409). Akun induk harus ada, aktif, bertipe sama, dan tidak membentuk siklus (400).
- **Database (pengaman race condition):** unique constraint `uk_account_code`. Pelanggarannya juga dipetakan ke 409.
- Kode akun tidak bisa diubah lewat `PUT`. Ukuran halaman dibatasi maksimal 100.

## Error contract

Semua error memakai format [ProblemDetail (RFC 9457)](https://www.rfc-editor.org/rfc/rfc9457) dengan `Content-Type: application/problem+json`. Field `requestId` sama dengan header `X-Correlation-Id` dan ikut tercetak di setiap baris log. Field `type` tidak ditulis karena nilainya default `about:blank`.

```jsonc
// 400 Bad Request: payload tidak valid
{ "title": "Validasi gagal", "status": 400,
  "errors": ["code: must not be blank", "name: must not be blank", "type: must not be null"],
  "requestId": "a1b2c3" }

// 404 Not Found: resource tidak ada
{ "title": "Not Found", "status": 404,
  "detail": "Akun 9999 tidak ditemukan", "requestId": "d4e5f6" }

// 409 Conflict: melanggar aturan unik/bisnis
{ "title": "Conflict", "status": 409,
  "detail": "Kode akun 1101 sudah digunakan", "requestId": "g7h8i9" }
```

Error tak terduga menghasilkan 500 dengan pesan ringkas. Stack trace hanya ditulis ke log server.

## Middleware

Alur request: `CorrelationIdFilter` (semua URL, paling awal) → `AccessLogFilter` (`/api/*`) → `DispatcherServlet` → `IdempotencyKeyInterceptor` → Controller.

- **`X-Correlation-Id`:** nilai dari client dipakai ulang bila aman (`[A-Za-z0-9._-]{1,64}`). Kalau tidak dikirim atau tidak aman, dibuatkan UUID baru. Nilainya disimpan di MDC dan dikembalikan di header respons.
- **`@RequiresIdempotencyKey`:** endpoint bertanda ini wajib mengirim header `Idempotency-Key`. Tanpa header, request dihentikan dengan 400 sebelum controller dipanggil. Contohnya `POST /api/v1/journals/{id}/post`; modul jurnal masih stub dan diimplementasikan di Sesi 4–5.
- **CORS:** `/api/**` untuk `http://localhost:3000`, dengan header `X-Correlation-Id` dan `Location` diekspos.

## Uji API

- **Otomatis:** `./mvnw test` menjalankan 6 skenario API dan beberapa kasus tambahan terhadap PostgreSQL di Testcontainers, termasuk verifikasi isi tabel `account`, ditambah test middleware.
- **Manual:** `http/coa-api.http` berisi 6 skenario untuk IntelliJ HTTP Client / VS Code REST Client, dengan assertion. Jalankan berurutan.
- **Postman / Bruno:** import `docs/openapi.yaml`.

| # | Skenario | Request | Hasil |
|---|---|---|---|
| 1 | Buat akun valid | `POST /accounts {1101, Kas, ASSET}` | 201 + Location |
| 2 | Kode duplikat | `POST /accounts {1101, ...}` | 409 Conflict |
| 3 | Payload tidak valid | `POST /accounts {code: ""}` | 400 + daftar error |
| 4 | Detail tidak ada | `GET /accounts/9999` | 404 Not Found |
| 5 | Ubah nama akun | `PUT /accounts/{id}` | 200 + data baru |
| 6 | Nonaktifkan akun | `PATCH /accounts/{id}/deactivate` | 204; akun hilang dari daftar aktif |

Data di container dev tetap tersimpan antar-run, jadi skenario 1 akan menghasilkan 409 kalau koleksi dijalankan dua kali. Reset dengan `docker compose down -v`.

Verifikasi langsung di database:

```bash
docker compose exec postgres psql -U accounting -d accounting \
  -c "select id, code, name, type, parent_id, active from account order by code"
```

## Memperbarui spesifikasi OpenAPI

```bash
curl -s http://localhost:8080/v3/api-docs.yaml -o docs/openapi.yaml
```
