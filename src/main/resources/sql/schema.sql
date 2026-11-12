PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS libro (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    isbn TEXT NOT NULL UNIQUE,
    titulo TEXT NOT NULL,
    autor TEXT NOT NULL,
    editorial TEXT NOT NULL,
    anio INTEGER NOT NULL,
    categoria TEXT NOT NULL,
    activo INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS ejemplar (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_unico TEXT NOT NULL UNIQUE,
    libro_id INTEGER NOT NULL,
    estado TEXT NOT NULL CHECK (estado IN ('DISPONIBLE', 'PRESTADO', 'EN_MANTENIMIENTO')),
    FOREIGN KEY (libro_id) REFERENCES libro(id)
);

CREATE TABLE IF NOT EXISTS registro_mantenimiento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ejemplar_id INTEGER NOT NULL,
    motivo TEXT NOT NULL,
    ingreso TEXT NOT NULL,
    habilitacion TEXT,
    FOREIGN KEY (ejemplar_id) REFERENCES ejemplar(id)
);
