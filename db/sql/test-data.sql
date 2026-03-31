INSERT INTO coreProject (code, name, description)
VALUES
  ('TP-CORE', 'Vendor Evaluation Core', 'Base del modelo coreProject y arranque incremental del dominio.'),
  ('ERP-2026', 'ERP Selection 2026', 'Evaluacion comparativa para shortlist de soluciones corporativas.'),
  ('GIS-LATAM', 'GIS Platform LATAM', 'Analisis multi-vendor con foco en capacidades geoespaciales.')
ON CONFLICT(code) DO UPDATE SET
  name = excluded.name,
  description = excluded.description,
  is_deleted = 0,
  updated_at = CURRENT_TIMESTAMP;