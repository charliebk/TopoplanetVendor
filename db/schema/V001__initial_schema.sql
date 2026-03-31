PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

-- Current minimal schema for the progressive build-out.
CREATE TABLE IF NOT EXISTS coreProject (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  code TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL,
  description TEXT,
  is_deleted INTEGER NOT NULL DEFAULT 0 CHECK (is_deleted IN (0, 1)),
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_coreProject_active ON coreProject(is_deleted);

COMMIT;
