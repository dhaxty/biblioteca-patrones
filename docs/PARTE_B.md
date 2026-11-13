# Parte B · Préstamos

Responsable: B. Alcance: Lector, Prestamo, servicios, Facade y transacciones.

## Qué incluye
| Paquete | Clases |
|---|---|
| `model` | `Lector`, `TipoLector`, `Prestamo`, `PrestamoDetalle` |
| `repository` | `LectorRepository`, `PrestamoRepository` |
| `repository.jdbc` | `JdbcLectorRepository`, `JdbcPrestamoRepository` |
| `service` | `LectorService`, `PrestamoService`, `BibliotecaOperaciones`, `BibliotecaFacade` |
| `resources/db` | `schema-prestamos.sql` |

## Reglas implementadas
- Máximo 5 préstamos activos por lector; plazo de 7 días calendario (límite = inicio + 7).
- Vencido: sigue activo y hoy es posterior a la fecha límite. Un lector con vencidos no toma nuevos préstamos.
- Se puede devolver con atraso (no hay multas); al devolver, el lector queda desbloqueado.
- Préstamo + cambio de estado del ejemplar: una sola transacción (`db.enTransaccion`).
- Devolución dañada (Facade, una transacción): `ejemplar.devolverConDanio()` + `ejemplares.actualizar()` +
  cierre del préstamo + `mantenimientoService.registrarIngresoPorDanio(id, motivo)`.
- Un índice único en SQLite impide dos préstamos activos del mismo ejemplar.

## Cableado (lo hace D en `Main`)
```java
Database db = new Database("biblioteca.db");
db.inicializarEsquema("/db/schema-catalogo.sql", "/db/schema-prestamos.sql");

var lectorRepo   = new JdbcLectorRepository(db);
var prestamoRepo = new JdbcPrestamoRepository(db);
var prestamoSrv  = new PrestamoService(db, lectorRepo, ejemplarRepo, prestamoRepo, Clock.systemDefaultZone());
var lectorSrv    = new LectorService(db, lectorRepo);
BibliotecaOperaciones facade = new BibliotecaFacade(db, prestamoSrv, mantenimientoSrv, evento -> { /* Observer */ });
```
`schema-prestamos.sql` va **después** de `schema-catalogo.sql` (FK hacia `ejemplar`).

## Para otros módulos
- **D (controladores):** capturar `ReglaNegocioException` y mostrar `getMessage()`. El operador que registra el
  préstamo es el id del usuario en sesión, que se pasa a `prestar(...)`. Dashboard: `PrestamoService.contarActivos()`
  y `contarVencidos()`.
- **D (Observer):** la Facade avisa con un `Consumer<String>` después del COMMIT (`PRESTAMO_REGISTRADO`,
  `DEVOLUCION_REGISTRADA`, `MANTENIMIENTO_ACTUALIZADO`). Es provisional: al existir `event.PublicadorEventos`, se cambia.
- **C (Seguridad):** `prestamo.operador_id` guarda el id de `UsuarioSistema`. Todavía sin clave foránea porque la
  tabla de C no existe; agregarla cuando exista.

## Pendiente / decisiones
- Desactivar un lector con préstamos activos hoy **sí** está permitido (la regla no está aprobada). Si el equipo
  la quiere, se agrega en `LectorService.desactivarLector`.
- Pruebas: `PrestamoTest`, `PrestamoServiceTest`, `BibliotecaFacadeTest` (repositorios en memoria) y
  `JdbcPrestamoRepositoryTest` (SQLite en memoria con el esquema real).
