-- 投稿×タイムライン用
CREATE INDEX IF NOT EXISTS idx_posts_user_created_id
  ON posts (user_id, created_at DESC, id DESC);

-- フォロー集合の取得を高速化
CREATE INDEX IF NOT EXISTS idx_follows_follower_followed
  ON follows (follower_id, followed_id);