# miniSNSアプリ MVP仕様書

## 1) 目的

-   **最小機能で"縦に1本"動くSNS**：サインアップ→ログイン（JWT）→フォロー→投稿作成→タイムライン表示
-   将来の拡張（通知、メディア等）を見据え、**スキーマ/API/セキュリティは堅実に**。

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
-   `/follows/{targetId}`（フォロー/フォロー解除）
-   `/timeline`（自分 + フォロー先の投稿を取得）

### 非スコープ（次フェーズ）

-   フォロー一覧/フォロワー一覧の取得
-   画像/動画アップロード、いいね/返信、検索、通知、外部ID連携、管理画面

## 4) ドメイン & データモデル

### ER（MVP）

-   `users(id, username[uniq 3..30], password_hash, created_at)`
-   `posts(id, user_id(FK→users.id ON DELETE CASCADE), content(TEXT ≤280), created_at)`
-   `follows(follower_id, followed_id, created_at, PRIMARY KEY(follower_id, followed_id))`
-   Flyway V1〜V3 で作成。**`created_at` は不変**（INSERT時のみ）。

### インデックス

``` sql
-- 投稿タイムライン用の複合インデックス
CREATE INDEX idx_posts_user_created_id
  ON posts (user_id, created_at DESC, id DESC);

-- フォロー関係の取得高速化
CREATE INDEX idx_follows_follower_followed
  ON follows (follower_id, followed_id);
CREATE INDEX idx_follows_created_at
  ON follows (created_at DESC);
```

## 5) セキュリティ設計

-   `SecurityFilterChain`（`config.SecurityConfig`）
    -   `permitAll`: `/health/**`, `/auth/**`
    -   `authenticated`: 上記以外（`/mypage`, `/posts`, `/timeline`, `/follows/**`
        など）
    -   `httpBasic().disable()`, `formLogin().disable()`, `SessionCreationPolicy.STATELESS`
    -   `oauth2ResourceServer().jwt()` で HS256 JWT を検証
-   `PasswordEncoder`: `BCryptPasswordEncoder`
-   `JwtService` が `user_id`/`username`/`roles` を含むトークンを発行
-   **環境変数**:
    `JWT_SECRET`（32バイト以上推奨）。`auth.jwt.secret: ${JWT_SECRET:dev-secret...}`

## 6) カーソル（Keyset）設計

-   並び順: `created_at DESC, id DESC`
-   条件:
    `created_at < :createdAt OR (created_at = :createdAt AND id < :id)`（**重複なし**）
-   カーソル: `Base64URL( {"at":"<ISO-8601>","id":<Long>} )`（JSONをエンコード）
-   無効カーソルは**1ページ目フォールバック**。`size` は 1..100
    にクリップ、`size+1` 取得で厳密に hasNext 判定も可。

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
-   `POST /follows/{targetId}` → `204 No Content`
-   `DELETE /follows/{targetId}` → `204 No Content`
-   `GET /timeline?size=20&cursor=<opaque>`
    -   Res:

        ``` json
        {
          "items":[
            {"id":10,"userId":1,"content":"...","createdAt":"..."}
          ],
          "nextCursor":"<opaque|null>"
        }
        ```

### 共通エラー

-   `401 {"error":"unauthorized"}`（未認証）
-   `403 {"error":"forbidden"}`（認可不許可）
-   その他のエラーは `{"error":"<code>","message":"...","path":"/...","status":400,"timestamp":"..."}` 形式（`GlobalExceptionHandler`）

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
    url: jdbc:postgresql://localhost:${POSTGRES_PORT:5432}/${POSTGRES_DB:sns_db}
    username: ${POSTGRES_USER:sns_user}
    password: ${POSTGRES_PASSWORD:sns_pass}
    hikari:
      maximum-pool-size: 5
  jpa:
    show-sql: false
    properties.hibernate.format_sql: true
logging:
  level:
    org.springframework.security: DEBUG
auth:
  jwt:
    secret: ${JWT_SECRET:dev-secret-change-me-please-32bytes-minimum-aaaaaaaa}
    expires-in-sec: 86400
```

-   起動:
    `SPRING_PROFILES_ACTIVE=local JWT_SECRET="$(openssl rand -base64 48)" ./gradlew bootRun`

## 9) ディレクトリ構成（抜粋）

    src/main/java/com/example/backend/
    ├─ BackendApplication.java
    ├─ config/ {SecurityConfig}
    ├─ entity/ {User, Post, Follow, FollowId}
    ├─ health/ {HealthController}
    ├─ pagination/ {Cursor, CursorCodec}
    ├─ repository/ {UserRepository, PostRepository, FollowRepository}
    ├─ security/ {AuthUser, JwtService}
    ├─ service/ {UserService, PostService, FollowService, TimelineService}
    ├─ util/ {CursorUtil}
    └─ web/
       ├─ AuthController, FollowController, MyPageController, PostController, TimelineController
       ├─ dto/ {AuthDtos, PostDtos, TimelineDtos}
       ├─ error/ {ErrorResponse, GlobalExceptionHandler}
       └─ log/ {RequestIdFilter, AccessLogFilter}
    resources/
    └─ db/migration/{V1__init.sql, V2__follows.sql, V3__add_timeline_indexes.sql}

## 10) 起動順序（ローカル）

1.  `docker compose up -d db`（Postgres healthy）
2.  `./gradlew clean bootRun -Dspring.profiles.active=local`
3.  `POST /auth/signup` → `POST /auth/login`（JWT取得）
4.  `POST /follows/{targetId}`（任意）→ `POST /posts` → `GET /timeline`（`size`・`cursor`）

## 11) 受け入れ基準（MVP Doneの定義）

-   `/health`, `/health/db` が 200 を返す（DB停止時は `/health/db` が
    503）
-   新規ユーザー登録→ログインで JWT が返る
-   JWT を付けて `/posts` 作成が成功し、`/timeline`
    で自分とフォロー先の投稿が順序正しく取得できる
-   `/follows/{targetId}` でフォロー/フォロー解除ができる
-   主要異常系（重複ユーザー名、空投稿、未認証アクセス）で適切なHTTPステータス

## 12) 次の増分（推奨ロードマップ）

-   フォロー一覧/フォロワー一覧 API
-   タイムラインにユーザー名を含めるためのDTO投影や `@EntityGraph` による N+1 回避
-   画像/動画アップロード、いいね/返信、検索、通知、外部ID連携、管理画面
-   Actuator 導入（本番は `/actuator/health` を利用）
-   E2E/統合テスト（Testcontainers + RestAssured）
