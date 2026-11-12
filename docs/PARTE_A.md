# Parte A · Catálogo / State

Rama sugerida: `feature/catalogo-state`. Paquete base: `edu.biblioteca`.

## Qué incluye

| Paquete | Clases |
|---|---|
| `model` | `Libro`, `Ejemplar`, `RegistroMantenimiento` |
| `model.estado` | `EstadoEjemplar` (interfaz), `EstadoDisponible`, `EstadoPrestado`, `EstadoEnMantenimiento`, `TipoEstado`, `TransicionInvalidaException` |
| `repository` | `LibroRepository`, `EjemplarRepository`, `MantenimientoRepository`, `RepositorioException` |
| `repository.jdbc` | `JdbcLibroRepository`, `JdbcEjemplarRepository`, `JdbcMantenimientoRepository` |
| `service` | `CatalogoService`, `MantenimientoService`, `ReglaNegocioException` |
| `config` | `Database` (conexión SQLite + transacciones) |
| `resources/db` | `schema-catalogo.sql` (tablas `libro`, `ejemplar`, `registro_mantenimiento`) |
| `test` | `EstadoEjemplarTest`, `CatalogoMantenimientoIntegracionTest` |

## State: transiciones

- Disponible → Prestado: `ejemplar.prestar()`
- Prestado → Disponible: `ejemplar.devolver()`
- Prestado → En mantenimiento: `ejemplar.devolverConDanio()`
- Disponible → En mantenimiento: `ejemplar.enviarAMantenimiento()`
- En mantenimiento → Disponible: `ejemplar.habilitar()`

Cualquier otra combinación lanza `TransicionInvalidaException`.

## Contratos para el resto del equipo

**B (Préstamos)**
- Para prestar: `ejemplar.prestar()` y luego `ejemplarRepository.actualizar(ejemplar)`, junto con el INSERT del préstamo, dentro de `db.enTransaccion(...)`.
- Devolución dañada: `ejemplar.devolverConDanio()`, `ejemplarRepository.actualizar(...)` y `mantenimientoService.registrarIngresoPorDanio(id, motivo)`, todo en la misma transacción. Las transacciones son reentrantes.
- `Database` solo tiene mis tablas: B y C agregan sus propios `schema-*.sql` y los pasan a `db.inicializarEsquema("/db/schema-catalogo.sql", "/db/schema-prestamos.sql", ...)`. El orden importa si hay claves foráneas hacia `ejemplar`.

**D (Presentación / Dashboard)**
- Buscar: `catalogoService.buscarLibros(texto)` (título, autor, ISBN o código de ejemplar).
- Dashboard: `libroRepository.contarActivos()` y `ejemplarRepository.contarPorEstado(TipoEstado.DISPONIBLE)`.
- Los servicios lanzan `ReglaNegocioException` con un mensaje listo para mostrar en un diálogo.
- Observer: los servicios de A no publican eventos todavía; D o el Facade pueden publicarlos después del commit.

**C (Seguridad)**: sin dependencias directas. El Proxy puede envolver `CatalogoService` y `MantenimientoService` si se decide restringir operaciones por rol.

## Supuestos míos (revisar con el equipo)

1. No se puede desactivar un libro con ejemplares prestados.
2. No se agregan ejemplares a un libro desactivado.
3. El año de publicación debe estar entre 1000 y el año actual.
4. Una sola conexión SQLite compartida (app de escritorio).
5. Las fechas de mantenimiento se guardan como texto ISO-8601.
