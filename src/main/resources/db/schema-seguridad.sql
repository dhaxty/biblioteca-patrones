CREATE TABLE IF NOT EXISTS usuario_sistema (
    usuario         TEXT    PRIMARY KEY NOT NULL,
    hash_contrasena TEXT    NOT NULL,
    rol             TEXT    NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'BIBLIOTECARIO')),
    activo          INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1))
);
