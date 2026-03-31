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

INSERT INTO category (core_project_id, code, name, description, factor)
VALUES (
  (SELECT id FROM coreProject WHERE code = 'TP-CORE'),
  'functional',
  'Functional',
  'Capacidades funcionales del producto evaluado.',
  1.35
)
ON CONFLICT(core_project_id, code) DO UPDATE SET
  name = excluded.name,
  description = excluded.description,
  factor = excluded.factor,
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO category (core_project_id, code, name, description, factor)
VALUES (
  (SELECT id FROM coreProject WHERE code = 'TP-CORE'),
  'operations',
  'Operations',
  'Operacion diaria, soporte y mantenibilidad.',
  1.0
)
ON CONFLICT(core_project_id, code) DO UPDATE SET
  name = excluded.name,
  description = excluded.description,
  factor = excluded.factor,
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO category (core_project_id, code, name, description, factor)
VALUES (
  (SELECT id FROM coreProject WHERE code = 'ERP-2026'),
  'security',
  'Security',
  'Seguridad, cumplimiento y control de accesos.',
  1.4
)
ON CONFLICT(core_project_id, code) DO UPDATE SET
  name = excluded.name,
  description = excluded.description,
  factor = excluded.factor,
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO category (core_project_id, code, name, description, factor)
VALUES (
  (SELECT id FROM coreProject WHERE code = 'ERP-2026'),
  'functional',
  'Functional',
  'Cobertura funcional para procesos corporativos.',
  1.25
)
ON CONFLICT(core_project_id, code) DO UPDATE SET
  name = excluded.name,
  description = excluded.description,
  factor = excluded.factor,
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO category (core_project_id, code, name, description, factor)
VALUES (
  (SELECT id FROM coreProject WHERE code = 'GIS-LATAM'),
  'operations',
  'Operations',
  'Despliegue, rendimiento y soporte regional.',
  1.15
)
ON CONFLICT(core_project_id, code) DO UPDATE SET
  name = excluded.name,
  description = excluded.description,
  factor = excluded.factor,
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO category (core_project_id, code, name, description, factor)
VALUES (
  (SELECT id FROM coreProject WHERE code = 'GIS-LATAM'),
  'security',
  'Security',
  'Proteccion de datos y trazabilidad del acceso.',
  1.3
)
ON CONFLICT(core_project_id, code) DO UPDATE SET
  name = excluded.name,
  description = excluded.description,
  factor = excluded.factor,
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO requirementLevel (core_project_id, code, name, description, factor)
VALUES (
  (SELECT id FROM coreProject WHERE code = 'TP-CORE'),
  'mandatory',
  'Mandatory',
  'Requirement that must be satisfied to continue the evaluation.',
  1.0
), (
  (SELECT id FROM coreProject WHERE code = 'ERP-2026'),
  'mandatory',
  'Mandatory',
  'Requirement that must be satisfied to continue the evaluation.',
  1.0
), (
  (SELECT id FROM coreProject WHERE code = 'GIS-LATAM'),
  'mandatory',
  'Mandatory',
  'Requirement that must be satisfied to continue the evaluation.',
  1.0
)
ON CONFLICT(core_project_id, code) DO UPDATE SET
  name = excluded.name,
  description = excluded.description,
  factor = excluded.factor,
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO requirementLevel (core_project_id, code, name, description, factor)
VALUES (
  (SELECT id FROM coreProject WHERE code = 'TP-CORE'),
  'important',
  'Important',
  'Requirement with high impact on the final evaluation score.',
  1.35
), (
  (SELECT id FROM coreProject WHERE code = 'ERP-2026'),
  'important',
  'Important',
  'Requirement with high impact on the final evaluation score.',
  1.5
), (
  (SELECT id FROM coreProject WHERE code = 'GIS-LATAM'),
  'important',
  'Important',
  'Requirement with high impact on the final evaluation score.',
  1.4
)
ON CONFLICT(core_project_id, code) DO UPDATE SET
  name = excluded.name,
  description = excluded.description,
  factor = excluded.factor,
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO requirementLevel (core_project_id, code, name, description, factor)
VALUES (
  (SELECT id FROM coreProject WHERE code = 'TP-CORE'),
  'optional',
  'Optional',
  'Requirement that adds value but has lower impact on the final score.',
  0.75
), (
  (SELECT id FROM coreProject WHERE code = 'ERP-2026'),
  'optional',
  'Optional',
  'Requirement that adds value but has lower impact on the final score.',
  0.65
), (
  (SELECT id FROM coreProject WHERE code = 'GIS-LATAM'),
  'optional',
  'Optional',
  'Requirement that adds value but has lower impact on the final score.',
  0.8
)
ON CONFLICT(core_project_id, code) DO UPDATE SET
  name = excluded.name,
  description = excluded.description,
  factor = excluded.factor,
  updated_at = CURRENT_TIMESTAMP;