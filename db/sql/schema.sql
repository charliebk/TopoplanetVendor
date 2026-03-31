CREATE TABLE IF NOT EXISTS coreProject (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  code TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL,
  description TEXT,
  is_deleted INTEGER NOT NULL DEFAULT 0 CHECK (is_deleted IN (0, 1)),
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coreTypeLog (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  code TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL,
  description TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coreLog (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  core_project_id INTEGER NOT NULL,
  core_type_log_id INTEGER NOT NULL,
  title TEXT NOT NULL,
  message TEXT NOT NULL,
  comment TEXT,
  happened_at_utc TEXT NOT NULL DEFAULT (STRFTIME('%Y-%m-%dT%H:%M:%fZ', 'now')),
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (core_project_id) REFERENCES coreProject(id),
  FOREIGN KEY (core_type_log_id) REFERENCES coreTypeLog(id)
);

CREATE INDEX IF NOT EXISTS idx_coreProject_active ON coreProject(is_deleted);
CREATE INDEX IF NOT EXISTS idx_coreLog_coreProject ON coreLog(core_project_id);
CREATE INDEX IF NOT EXISTS idx_coreLog_coreTypeLog ON coreLog(core_type_log_id);
CREATE INDEX IF NOT EXISTS idx_coreLog_project_happenedAt ON coreLog(core_project_id, happened_at_utc);

INSERT INTO coreTypeLog (code, name, description)
VALUES
  ('info', 'Info', 'Informational event for the project timeline.'),
  ('warning', 'Warning', 'Event that requires attention but does not stop the flow.'),
  ('error', 'Error', 'Event that represents a failure or blocked operation.')
ON CONFLICT(code) DO UPDATE SET
  name = excluded.name,
  description = excluded.description,
  updated_at = CURRENT_TIMESTAMP;
