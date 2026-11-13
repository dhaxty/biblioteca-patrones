# AGENTS.md · Biblioteca Universitaria

Guía de arquitectura, patrones y contratos del equipo. Este documento refleja el estado final de integración del proyecto (Partes A, B, C y D).

## 1. Stack

- **Java 25**, Swing, SQLite vía JDBC (`org.xerial:sqlite-jdbc`).
- **Maven** (`pom.xml` en la raíz), NetBeans como IDE, JUnit 5 para pruebas.
- Se distribuye como aplicación de escritorio para Windows (JAR).
- Paquete base: `edu.biblioteca`. Todo el código, comentarios, mensajes al usuario y nombres de dominio van en **español** (clases, métodos y variables incluidos). Las palabras técnicas estándar (`Repository`, `Service`, `Facade`, `Proxy`, `Observer`) se mantienen en inglés.

## 2. Producto y alcance

- **Usuarios del programa:** solo el administrador y el bibliotecario inician sesión. Estudiantes y docentes son *lectores registrados* y **no** acceden al programa.
- **Funciones:** administrar libros físicos, ejemplares (copias), lectores, préstamos, devoluciones, mantenimiento, cuentas del personal y estadísticas.
- **Fuera de alcance:** reservas, renovaciones, multas, libros digitales, acceso de lectores.

## 3. Reglas de negocio (Implementadas)

- Máximo **5 préstamos simultáneos** por lector.
- Plazo de préstamo: **7 días calendario**.
- Un lector con préstamos **vencidos** no puede tomar nuevos préstamos.
- Estados del ejemplar: `Disponible`, `Prestado`, `En mantenimiento`.
- Libros y lectores se **desactivan lógicamente**; nunca se borran, para conservar el historial.
- No se puede desactivar un libro que tenga ejemplares prestados.
- No se agregan ejemplares a un libro desactivado.
- El préstamo y el cambio de estado del ejemplar se guardan en **una sola transacción**.

## 4. Arquitectura: MVC + Service + Repository

```
VIEW → CONTROLLER → SERVICE (+ Facade) → REPOSITORY (interfaz) → JDBC → SQLite
                         ↑
                  MODEL (dominio) · EVENT (Observer, solo después del COMMIT)
```

| Capa | Hace | No hace |
|---|---|---|
| `view` | Pinta formularios y tablas Swing. | No consulta SQLite ni decide reglas ni autoriza. |
| `controller` | Recibe clics, valida entradas básicas, invoca servicios/fachada, muestra errores. | No contiene reglas de negocio ni SQL. |
| `service` | Aplica límites, permisos, vencimientos y transacciones; coordina el dominio. | No depende de Swing. |
| `model` | Entidades y su comportamiento. El patrón State vive aquí. | No conoce JDBC ni la UI. |
| `repository` | Interfaces para guardar/consultar. | Las interfaces no mencionan JDBC. |
| `repository.jdbc` | Implementación JDBC sobre SQLite. | No contiene reglas de negocio. |

## 5. Estructura de paquetes (Estado: 100% Integrado)

```
src/main/java/edu/biblioteca/
├─ Main.java                      Punto de entrada, inicialización y dependencias   [D · listo]
├─ model/                         Libro, Ejemplar, RegistroMantenimiento, Lector... [A/B/C · listo]
│  └─ estado/                     EstadoEjemplar + 3 estados + TipoEstado           [A · listo]
├─ view/                          Frames y paneles Swing con Look & Feel Nimbus     [D · listo]
├─ controller/                    Controladores MVC y gestión de excepciones        [D · listo]
├─ service/                       Catalogo, Mantenimiento, Prestamo, Dashboard      [A/B/D · listo]
│                                 y BibliotecaFacade                                [B · listo]
├─ repository/                    Interfaces Lector/Libro/Ejemplar/Prestamo/Usuario [A/B/C · listo]
│  └─ jdbc/                       Jdbc* de todos los repositorios                   [A/B/C · listo]
├─ security/                      Login, permisos (Proxy) y HashContrasena          [C · listo]
├─ event/                         Observer + eventos para el Dashboard              [D · listo]
└─ config/                        Database (conexión unificada SQLite)              [A · listo]
src/main/resources/db/            schema-catalogo.sql, schema-prestamos.sql y schema-seguridad.sql
src/test/java/                    Pruebas de Integración (JUnit 5 + BD en Memoria)
```

## 6. Modelo de dominio

| Clase | Datos esenciales | Métodos |
|---|---|---|
| `Libro` | ISBN, título, autor, editorial, año, categoría, activo | `actualizarDatos()`, `desactivar()` |
| `Ejemplar` | código único, libro, estado | `prestar()`, `devolver()`, `devolverConDanio()`, `enviarAMantenimiento()`, `habilitar()` |
| `RegistroMantenimiento` | ejemplar, motivo, ingreso, habilitación | `finalizar()`, `estaAbierto()` |
| `Lector` | código universitario, nombre, correo, tipo, activo | `actualizarDatos()`, `desactivar()` |
| `Prestamo` | lector, ejemplar, operador, inicio, límite, devolución | `estaVencido(fecha)`, `cerrar()` |
| `UsuarioSistema` | usuario, hash de contraseña, rol, activo | `tieneRol()`, `estaActivo()` |

Relaciones: Libro 1:N Ejemplar; Ejemplar 1:N Préstamo (histórico); Lector 1:N Préstamo; el personal registra préstamos; Ejemplar 1:N RegistroMantenimiento.

## 7. Patrones GoF Implementados

| Patrón | Participantes | Estado |
|---|---|---|
| **State** | `EstadoEjemplar` + `EstadoDisponible`, `EstadoPrestado`, `EstadoEnMantenimiento` | Implementado (A) |
| **Facade** | `BibliotecaOperaciones` + `BibliotecaFacade`: coordina préstamos, devoluciones y mantenimiento. | Implementado (B) |
| **Proxy** | `GestionCuentas`, `GestionCuentasReal`, `GestionCuentasProxy`: autoriza tareas de administrador. | Implementado (C) |
| **Observer** | `ObservadorEvento`, `PublicadorEventos`, `ObservadorDashboard`: actualiza el tablero numérico. | Implementado (D) |
| **Factory** | `GestionCuentasFactory`: aísla la creación del proxy de seguridad. | Implementado (C) |

### State: transiciones permitidas

| Operación | Desde | Hacia |
|---|---|---|
| `prestar()` | Disponible | Prestado |
| `devolver()` | Prestado | Disponible |
| `devolverConDanio()` | Prestado | En mantenimiento |
| `enviarAMantenimiento()` | Disponible | En mantenimiento |
| `habilitar()` | En mantenimiento | Disponible |

## 8. Contratos de Integración Final

**Base de datos y transacciones** (`config.Database`)
- Conexión compartida. Los repositorios JDBC reciben `Database` por constructor y usan `db.conexion()`.
- Operaciones atómicas: `db.enTransaccion(() -> { ... })`. Es **reentrante**; si algo lanza excepción, ocurre un rollback general.
- Inicialización en `Main.java` carga los 3 scripts SQL unificados de `src/main/resources/db/`.
- Se usa `db.ultimoIdInsertado()` (`last_insert_rowid()`) para llaves autogeneradas.

**Manejo de Errores en Capas**
- Reglas incumplidas → `service.ReglaNegocioException`. 
- Los controladores MVC capturan explícitamente `ReglaNegocioException` (nunca `RuntimeException` genérico) y muestran `getMessage()` en los diálogos Swing.
- Fallos de datos → `repository.RepositorioException` (envuelve `SQLException`).

**Fachada y Eventos**
- Prestar y Devolver: Operaciones atómicas delegadas en la fachada que gestionan los repositorios de préstamos, el cambio de estado del ejemplar y el mantenimiento simultáneamente.
- El `DashboardService` calcula sus indicadores reutilizando los conteos de los repositorios originales.
- Las notificaciones `Observer` se publican únicamente tras un *COMMIT* exitoso para refrescar la vista.

## 9. Convenciones de código establecidas

- Cero SQL fuera de `repository.jdbc`. Uso exclusivo de `PreparedStatement`.
- Swing y los controladores ignoran por completo los repositorios; su acceso es a través de servicios o la fachada.
- Contraseñas protegidas mediante Hash (PBKDF2), con creación automática de cuenta `admin` por defecto si la BD es nueva (`admin` / `Admin123*`).
- Pruebas unitarias de integración apuntan siempre a SQLite en memoria (`Database.enMemoria()`).

## 10. Comandos y Distribución

```bash
mvn test                 # compila y ejecuta toda la suite de pruebas A, B, C y D
mvn exec:java -Dexec.mainClass="edu.biblioteca.Main" # ejecuta la interfaz Swing
mvn -q -DskipTests package # genera el JAR de escritorio en target/
```