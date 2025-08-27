-- ユーザー、投稿の最小スキーマ（タイムライン用の基本）
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(30) UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS posts (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- タイムライン用の基本インデックス（後のKeysetに備える）
CREATE INDEX IF NOT EXISTS idx_posts_user_created
  ON posts (user_id, created_at DESC, id DESC);
