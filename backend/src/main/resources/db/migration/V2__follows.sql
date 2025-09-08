CREATE TABLE IF NOT EXISTS follows (
  follower_id BIGINT NOT NULL,     -- フォローする側（= 自分）
  followed_id BIGINT NOT NULL,     -- フォローされる側（= 相手）
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_follows_followed FOREIGN KEY (followed_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT uq_follows UNIQUE (follower_id, followed_id),
  CONSTRAINT ck_self_follow CHECK (follower_id <> followed_id)
);

-- クエリ最適化用インデックス
-- 「自分が誰をフォローしているか」を引く用途が最重要
CREATE INDEX IF NOT EXISTS idx_follows_follower ON follows (follower_id, followed_id);

-- 「このユーザのフォロワー一覧」取得用
CREATE INDEX IF NOT EXISTS idx_follows_followed ON follows (followed_id, follower_id);

-- 作成日の降順タイムラインJOINに効くケース
CREATE INDEX IF NOT EXISTS idx_follows_created_at ON follows (created_at DESC);