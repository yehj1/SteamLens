CREATE TABLE IF NOT EXISTS apps (
  id UUID PRIMARY KEY,
  package_id TEXT NOT NULL,
  name TEXT,
  created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS reviews (
  id UUID PRIMARY KEY,
  app_id UUID REFERENCES apps(id) ON DELETE CASCADE,
  review_source TEXT NOT NULL,
  country TEXT,
  lang TEXT,
  rating INT,
  author TEXT,
  content TEXT NOT NULL,
  reviewed_at TIMESTAMP,
  extra JSONB,
  created_at TIMESTAMP DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_reviews_app ON reviews(app_id);
CREATE INDEX IF NOT EXISTS idx_reviews_country ON reviews(country);
CREATE INDEX IF NOT EXISTS idx_reviews_reviewed_at ON reviews(reviewed_at);

CREATE TABLE IF NOT EXISTS insights (
  id UUID PRIMARY KEY,
  app_id UUID REFERENCES apps(id) ON DELETE CASCADE,
  countries TEXT[],
  lang TEXT,
  run_at TIMESTAMP DEFAULT now(),
  model TEXT,
  summary_md TEXT,
  summary_json JSONB
);

-- Steam-specific columns and uniqueness constraints
ALTER TABLE reviews
  ADD COLUMN IF NOT EXISTS recommendation_id TEXT,
  ADD COLUMN IF NOT EXISTS playtime_forever INT,
  ADD COLUMN IF NOT EXISTS author_id TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS uq_reviews_steam_source_recid
  ON reviews (review_source, recommendation_id)
  WHERE review_source = 'steam';
