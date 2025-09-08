# miniSNSアプリ MVP仕様書

## 1) 目的

-   **最小機能で"縦に1本"動くSNS**：サインアップ→ログイン（JWT）→投稿作成→タイムライン表示
-   将来の拡張（フォロー、通知、メディア等）を見据え、**スキーマ/API/セキュリティは堅実に**。

## 2) 技術スタック

-   **Backend**: Spring Boot 3.5 / Java 21, Spring Web, Spring Security,
    Spring Data JPA, HikariCP
-   **DB**: PostgreSQL 16（Docker Compose）
-   **Migration**: Flyway（`db/migration`）
-   **Auth**: JWT (JJWT 0.12.x, HS256)
-   **Build**: Gradle Groovy
-   **Profile**: `application.yml`（共通）,
    `application-local.yml`（ローカル開発）

## 3) MVPスコープ / 非スコープ

### スコープ

-   `/health`, `/health/db`（監視・疎通）
-   `/auth/signup`, `/auth/login`（bcryptハッシュ）
-   `/mypage`（JWT動作確認）
-   `/posts`（作成）
-   `/timeline`（**自分の投稿のみ**を取得）

### 非スコープ（次フェーズ）

-   フォロー関係（`follows`）
- keysetページング
-   画像/動画アップロード、いいね/返信、検索、通知、外部ID連携、管理画面

## 4) ドメイン & データモデル

### ER（MVP）

-   `users(id, username[uniq 3..30], password_hash, created_at)`
-   `posts(id, user_id(FK→users.id ON DELETE CASCADE), content(TEXT ≤280), created_at)`
-   Flyway V1 で作成。**`created_at` は不変**（INSERT時のみ）。

### インデックス

``` sql
-- タイムライン/作成者別の降順キーセット用
CREATE INDEX idx_posts_user_created
  ON posts (user_id, created_at DESC, id DESC);
```

## 5) セキュリティ設計

-   `SecurityFilterChain`（**ベースパッケージ直下に配置**）
    -   `permitAll`: `/health/**`, `/auth/**`
    -   `authenticated`: 上記以外（`/mypage`, `/posts`, `/timeline`
        など）
    -   `httpBasic().disable()`, `formLogin().disable()`, `STATELESS`
-   `JwtAuthFilter` を `UsernamePasswordAuthenticationFilter`
    の**前**に追加
-   `PasswordEncoder`: `BCryptPasswordEncoder`
-   **環境変数**:
    `JWT_SECRET`（32バイト以上推奨）。`app.jwt.secret: ${JWT_SECRET:dev-secret...}`

## 6) カーソル（Keyset）設計

-   並び順: `created_at DESC, id DESC`
-   条件:
    `created_at < :createdAt OR (created_at = :createdAt AND id < :id)`（**重複なし**）
-   カーソル: `Base64URL( "<sec>:<nano>:<id>" )`（UTC, パディングなし）
-   無効カーソルは**1ページ目フォールバック**。`limit` は 1..50
    に制限、`limit+1` 取得で厳密に hasNext 判定も可。

## 7) API設計（MVP）

### 公開

-   `GET /health` → `200 {"status":"ok"}`
-   `GET /health/db` → `200 {"status":"ok","db":"up","check":1}` or
    `503 {...}`

### 認証不要（permitAll）

-   `POST /auth/signup`
    -   Req: `{"username":"[3..30]","password":"[8..128]"}`
    -   Res: `{"id":1,"username":"kaito"}`
    -   409/400: ユーザー名重複/バリデーションエラー
-   `POST /auth/login`
    -   Req: `{"username":"kaito","password":"password1234"}`
    -   Res: `{"token":"<JWT>"}`（HS256, `sub=username`, `uid`
        クレーム、`exp` 24h）
    -   401: 認証失敗

### 認証必須（Bearer）

-   `GET /mypage` → `{"username":"kaito"}`
-   `POST /posts`
    -   Req: `{"content":"text up to 280"}`
    -   Res:
        `{"id":10,"userId":1,"username":"kaito","content":"...","createdAt":"..."}`
    -   400: 空文字/上限超過
-   `GET /timeline?limit=20&cursor=<opaque>`
    -   Res:

        ``` json
        {
          "items":[
            {"id":10,"userId":1,"username":"alice","content":"...", "createdAt":"..."}
          ],
          "nextCursor":"<opaque|null>"
        }
        ```

### 共通エラー

-   `401 {"error":"unauthorized"}`（未認証）
-   `403 {"error":"forbidden"}`（認可不許可／将来）
-   `400/409 {"message":"..."}`
-   例外は `ResponseStatusException` で明示 or 共通ハンドラ追加予定

## 8) 設定 & 環境変数

``` yaml
# application.yml（共通）
spring:
  jpa:
    hibernate.ddl-auto: none
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration
server:
  error:
    include-message: always
    include-binding-errors: always

# application-local.yml（ローカル上書き）
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:sns_db}
    username: ${DB_USER:sns}
    password: ${DB_PASSWORD:sns_password_123}
  jpa:
    properties.hibernate.format_sql: true
logging.level:
  org.springframework.security: INFO
app.jwt:
  secret: ${JWT_SECRET:dev-secret-change-me-please-32bytes-minimum-aaaaaaaa}
  expires-in-sec: 86400
```

-   起動:
    `SPRING_PROFILES_ACTIVE=local JWT_SECRET="$(openssl rand -base64 48)" ./gradlew bootRun`

## 9) ディレクトリ構成（抜粋）

    src/main/java/com/example/backend/
    ├─ BackendApplication.java
    ├─ SecurityConfig.java
    ├─ boot/ (任意の起動ログ補助)
    ├─ config/ (将来の設定)
    ├─ entity/ {User, Post}
    ├─ repository/ {UserRepository, PostRepository}
    ├─ security/ {JwtService, JwtAuthFilter}
    ├─ service/ {UserService, PostService}
    ├─ util/ {CursorUtil}
    └─ web/
       ├─ HealthController, AuthController, PostController, MeController
       └─ dto/ {AuthDtos, PostDtos}
    resources/
    └─ db/migration/V1__init.sql

## 10) 起動順序（ローカル）

1.  `docker compose up -d db`（Postgres healthy）
2.  `./gradlew clean bootRun -Dspring.profiles.active=local`
3.  `POST /auth/signup` → `POST /auth/login`（JWT取得）
4.  `POST /posts` → `GET /timeline`（`limit`・`cursor`）

## 11) 受け入れ基準（MVP Doneの定義）

-   `/health`, `/health/db` が 200 を返す（DB停止時は `/health/db` が
    503）
-   新規ユーザー登録→ログインで JWT が返る
-   JWT を付けて `/posts` 作成が成功し、`/timeline`
    で順序正しく取得できる
-   主要異常系（重複ユーザー名、空投稿、未認証アクセス）で適切なHTTPステータス

## 12) 次の増分（推奨ロードマップ）

-   **V2 Migration**:
    `follows(follower_id, followee_id, created_at, PK(follower_id, followee_id))` +
    インデックス
-   タイムラインを「自分 + フォロー先」へ拡張（`IN (:ids)` or
    `JOIN follows`）
-   `limit`/`cursor` によるページング
-   **共通エラーハンドラ**（`@ControllerAdvice`）
-   DTO投影 or `@EntityGraph` で N+1 回避
-   Actuator 導入（本番は `/actuator/health` を利用）
-   E2E/統合テスト（Testcontainers + RestAssured）
