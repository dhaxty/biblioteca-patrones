CREATE TABLE IF NOT EXISTS libro (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    isbn      TEXT    NOT NULL UNIQUE,
    titulo    TEXT    NOT NULL,
    autor     TEXT    NOT NULL,
    editorial TEXT    NOT NULL DEFAULT '',
    anio      INTEGER NOT NULL,
    categoria TEXT    NOT NULL DEFAULT '',
    activo    INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS ejemplar (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo   TEXT    NOT NULL UNIQUE,
    libro_id INTEGER NOT NULL REFERENCES libro(id),
    estado   TEXT    NOT NULL DEFAULT 'DISPONIBLE'
             CHECK (estado IN ('DISPONIBLE', 'PRESTADO', 'EN_MANTENIMIENTO'))
);

CREATE TABLE IF NOT EXISTS registro_mantenimiento (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    ejemplar_id  INTEGER NOT NULL REFERENCES ejemplar(id),
    motivo       TEXT    NOT NULL,
    ingreso      TEXT    NOT NULL,
    habilitacion TEXT
);

CREATE INDEX IF NOT EXISTS idx_ejemplar_libro ON ejemplar(libro_id);
CREATE INDEX IF NOT EXISTS idx_ejemplar_estado ON ejemplar(estado);
CREATE INDEX IF NOT EXISTS idx_mant_ejemplar ON registro_mantenimiento(ejemplar_id);
