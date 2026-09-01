-- PostgreSQL source-of-truth schema. Curriculum rows are administered through the backend;
-- no coefficient, subject, or exam rule is seeded in this migration.
CREATE TYPE education_level AS ENUM ('troisieme_bfem', 'terminale_bac');
CREATE TYPE curriculum_status AS ENUM ('draft', 'published', 'archived');

CREATE TABLE education_curricula (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  version VARCHAR(64) UNIQUE NOT NULL,
  level education_level NOT NULL,
  series_code VARCHAR(20),
  status curriculum_status NOT NULL DEFAULT 'draft',
  effective_from DATE NOT NULL,
  effective_to DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE TABLE education_subjects (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  curriculum_id UUID NOT NULL REFERENCES education_curricula(id) ON DELETE CASCADE,
  external_key VARCHAR(100) NOT NULL,
  name VARCHAR(100) NOT NULL,
  coefficient INTEGER NOT NULL CHECK (coefficient > 0),
  UNIQUE (curriculum_id, external_key)
);

CREATE TABLE education_chapters (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  subject_id UUID NOT NULL REFERENCES education_subjects(id) ON DELETE CASCADE,
  title VARCHAR(255) NOT NULL,
  position INTEGER NOT NULL CHECK (position >= 0),
  summary_markdown TEXT NOT NULL,
  UNIQUE (subject_id, position)
);

CREATE TABLE education_chapter_mastery (
  student_id UUID NOT NULL,
  chapter_id UUID NOT NULL REFERENCES education_chapters(id) ON DELETE CASCADE,
  mastery_score NUMERIC(5,4) NOT NULL DEFAULT 0 CHECK (mastery_score BETWEEN 0 AND 1),
  stability_days NUMERIC(8,2) NOT NULL DEFAULT 1 CHECK (stability_days > 0),
  last_studied_on DATE,
  PRIMARY KEY (student_id, chapter_id)
);

CREATE TABLE education_semester_grades (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id UUID NOT NULL,
  subject_id UUID NOT NULL REFERENCES education_subjects(id) ON DELETE CASCADE,
  semester SMALLINT NOT NULL CHECK (semester IN (1, 2)),
  homework_scores JSONB NOT NULL DEFAULT '[]'::jsonb,
  composition_score NUMERIC(4,2) NOT NULL CHECK (composition_score BETWEEN 0 AND 20),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (student_id, subject_id, semester)
);
