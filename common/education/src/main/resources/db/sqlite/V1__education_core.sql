-- SQLite offline cache. Sync IDs reference the PostgreSQL UUIDs as TEXT.
CREATE TABLE education_curricula (
  id TEXT PRIMARY KEY NOT NULL, version TEXT NOT NULL UNIQUE, level TEXT NOT NULL,
  series_code TEXT, status TEXT NOT NULL, effective_from TEXT NOT NULL, effective_to TEXT,
  CHECK (level IN ('troisieme_bfem', 'terminale_bac')),
  CHECK (status IN ('draft', 'published', 'archived')),
  CHECK (effective_to IS NULL OR effective_to >= effective_from)
);
CREATE TABLE education_subjects (
  id TEXT PRIMARY KEY NOT NULL, curriculum_id TEXT NOT NULL REFERENCES education_curricula(id) ON DELETE CASCADE,
  external_key TEXT NOT NULL, name TEXT NOT NULL, coefficient INTEGER NOT NULL CHECK (coefficient > 0),
  UNIQUE (curriculum_id, external_key)
);
CREATE TABLE education_chapters (
  id TEXT PRIMARY KEY NOT NULL, subject_id TEXT NOT NULL REFERENCES education_subjects(id) ON DELETE CASCADE,
  title TEXT NOT NULL, position INTEGER NOT NULL CHECK (position >= 0), summary_markdown TEXT NOT NULL,
  UNIQUE (subject_id, position)
);
CREATE TABLE education_chapter_mastery (
  student_id TEXT NOT NULL, chapter_id TEXT NOT NULL REFERENCES education_chapters(id) ON DELETE CASCADE,
  mastery_score REAL NOT NULL DEFAULT 0 CHECK (mastery_score BETWEEN 0 AND 1),
  stability_days REAL NOT NULL DEFAULT 1 CHECK (stability_days > 0), last_studied_on TEXT,
  PRIMARY KEY (student_id, chapter_id)
);
