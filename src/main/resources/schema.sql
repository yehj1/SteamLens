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
