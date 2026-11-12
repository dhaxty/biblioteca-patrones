# AGENTS.md - Guía de Operación para Agentes de IA

Este documento define las reglas de desarrollo, arquitectura, límites de módulos y comandos de verificación para cualquier agente de IA que trabaje en este repositorio.

---

## 1. Stack Tecnológico y Comandos de Build

* **Lenguaje:** Java 25 (usando `maven.compiler.release` igual a `25`).
* **UI:** Java Swing (MVC puro).
* **Persistencia:** SQLite vía JDBC nativo (sin ORM ni Hibernate).
* **Pruebas:** JUnit 5.
* **Gestor de Dependencias:** Maven.
* **Entorno del desarrollador:** Windows 11 + PowerShell. Maven y JDK 25 ya están instalados en la máquina del usuario.

### Comandos de Verificación Obligatorios
Antes de dar por completada cualquier tarea o solicitar aprobación, ejecuta e inspecciona los resultados de:

```bash
# Compilar el proyecto completo
mvn -B -ntp clean compile

# Ejecutar la suite de pruebas unitarias e integración
mvn -B -ntp test
```

Los flags `-B` (modo batch, no interactivo) y `-ntp` (sin barras de progreso de descarga) son obligatorios para evitar salidas infinitas o prompts que bloqueen el comando.

---

## 2. Estructura de Paquetes (`biblioteca`)

Toda la aplicación debe organizarse bajo el paquete raíz `biblioteca`:

```
biblioteca/
├─ model/                  # Entidades de dominio (Libro, Ejemplar, RegistroMantenimiento, etc.)
│  └─ estado/             # Patrón GoF State (EstadoEjemplar y sus 3 implementaciones)
├─ repository/            # Interfaces de repositorios
│  └─ jdbc/              # Implementaciones SQLite con JDBC nativo
├─ service/               # Servicios de reglas de negocio y transacciones
├─ controller/            # Controladores MVC
├─ view/                  # Formularios y paneles Swing
├─ security/              # Autenticación, roles y Proxy de seguridad
├─ event/                 # Patrón Observer y eventos
└─ config/                # Conexión SQLite y scripts de inicialización
```

---

## 3. Alcance Específico del Módulo A (Catálogo / State)

Como agente operando en la rama `feature/catalogo-state`, **tus responsabilidades principales incluyen**:

1. **Dominio Catálogo & Mantenimiento:**
   * `Libro`: Datos de libro, actualización y desactivación lógica (`activo = false`).
   * `Ejemplar`: Código único, referencia al `Libro`, delegación de transiciones al patrón **State**.
   * `RegistroMantenimiento`: Registro de ingreso y finalización (fecha de habilitación) para historial.

2. **Patrón GoF State (`biblioteca.model.estado`):**
   * Interfaz `EstadoEjemplar`: Define operaciones (`prestar`, `devolver`, `enviarAMantenimiento`, `habilitar`).
   * Implementaciones: `EstadoDisponible`, `EstadoPrestado`, `EstadoEnMantenimiento`.
   * **Invariante:** Cualquier transición de estado no permitida **debe** lanzar `IllegalStateException`.

3. **Repositorios JDBC (`biblioteca.repository.jdbc`):**
   * Implementar `LibroRepository`, `EjemplarRepository` y `MantenimientoRepository`.
   * Inyectar `java.sql.Connection` por constructor para permitir la reutilización de conexiones dentro de transacciones SQLite.

4. **Servicios (`biblioteca.service`):**
   * `CatalogoService`: CRUD lógico de libros y ejemplares.
   * `MantenimientoService`: Retiro e ingreso a mantenimiento gestionado con transacciones explícitas (`setAutoCommit(false)`, `commit()`, `rollback()`).

---

## 4. Fronteras y Contratos entre Desarrolladores

Para preservar la integración con los desarrolladores B, C y D:

* **Módulo B (Préstamos):** Depende de `EjemplarRepository` y del método `ejemplar.prestar()`. No modifiques las firmas publicas de `EjemplarRepository` sin previo aviso.
* **Módulo C (Seguridad):** Gestiona `UsuarioSistema` y autenticación. No dupliques ni alteres la seguridad en los servicios de catálogo.
* **Módulo D (Presentación / Swing):** La vista lee los repositorios y servicios a través de controladores. **Nunca agregues lógica de base de datos (SQL) ni decisiones de negocio dentro de clases del paquete `view` o `controller`.**

---

## 5. Reglas Invariantes y Buenas Prácticas

1. **Borrado Lógico:** Los registros de `Libro` nunca se eliminan mediante `DELETE`. Se usa actualización de la columna `activo = 0`.
2. **Manejo de Excepciones:**
   * `IllegalArgumentException`: Para datos nulos, vacíos, o violaciones de formato/duplicidad.
   * `IllegalStateException`: Para intentos de transiciones de estado inválidas o violaciones de invariantes de negocio.
3. **Manejo de SQL / JDBC:**
   * Usa siempre `PreparedStatement` para evitar inyección SQL.
   * Mapea los tipos `LocalDateTime` de Java como cadenas ISO-8601 (`String`) en SQLite, y los booleanos como `INTEGER` (`1` / `0`).
4. **Git Hygiene:** No incluyas en los commits archivos `.db`, carpetas `target/` ni configuraciones personales del IDE.
5. **Pruebas con SQLite:** Los tests de repositorio deben usar una base en memoria (`jdbc:sqlite::memory:`) o un archivo temporal, nunca el `.db` real. Activa `PRAGMA foreign_keys = ON` en **cada** conexión nueva. Los tests no deben compartir estado entre sí.

---

## 6. Esquema SQL (`src/main/resources/sql/schema.sql`)

El agente debe garantizar que el script de base de datos mantenga las llaves foráneas habilitadas:

```sql
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
```

---

## 7. Control de Bloqueos, Entorno y Reporte de Fallos

Esta sección existe para evitar sesiones largas que no avanzan. Tiene prioridad sobre cualquier instrucción del prompt que pida "seguir intentando hasta que funcione".

### 7.1 Prerrequisitos de entorno (una sola vez, al inicio)
* Antes de escribir código, ejecuta únicamente:
  * `java -version`
  * `mvn -v`
* Si Java es menor a 25 o `mvn` no responde: **DETENTE y avisa al usuario**.
* **No busques** `mvn.cmd` ni JDKs por el disco, no instales ni descargues JDK/Maven, y no cambies `maven.compiler.release` para "hacerlo funcionar".
* Si tu terminal/sandbox no ve `mvn` pero el usuario dice que sí lo tiene, repórtalo como problema de PATH del sandbox y pide al usuario que ejecute el comando él mismo.

### 7.2 Regla de los 3 intentos
* Si un mismo comando o la misma corrección falla **3 veces seguidas** con el mismo error (o uno equivalente), **DETENTE**. No pruebes una cuarta variante.
* Al detenerte, escribe un reporte con este formato y espera instrucciones:
  1. Comando exacto ejecutado
  2. Error completo (últimas 30 líneas)
  3. Qué intentaste en cada intento
  4. Tu hipótesis de la causa
  5. Qué necesitas del usuario para continuar

### 7.3 Comandos acotados y sin esperas
* Usa siempre `mvn -B -ntp ...` (ver sección 1).
* Nunca lances comandos que puedan quedarse esperando entrada o que no terminan: prompts interactivos, servidores, ni la aplicación Swing (`main`). La UI se verifica **solo compilando**.
* **Timeout de comandos:** si un comando no produce salida nueva en ~2 minutos, o lleva más de 3 minutos en total, cancélalo y repórtalo. No lo relances sin cambiar algo concreto y justificado.
* No lances un comando nuevo mientras otro sigue en ejecución o en segundo plano. Espera a que termine o cancélalo antes.
* No dejes tareas en segundo plano (Background Tasks) abiertas. Si quedó alguna colgada, cancélala y menciónalo en el reporte.

### 7.4 Dependencias y modo offline
* La primera ejecución de Maven puede descargar dependencias (sqlite-jdbc, JUnit 5, plugins de Surefire). Si esa descarga falla o se queda sin salida, **no reintentes en bucle**: aplica la regla 7.2.
* Si el usuario indica que las dependencias ya están descargadas, usa modo offline: `mvn -B -ntp -o test`. Si falla por dependencias no encontradas, detente y avisa; no intentes descargar nada.
* El `pom.xml` debe fijar versiones explícitas de `maven-compiler-plugin`, `maven-surefire-plugin` (3.x, necesario para JUnit 5), `junit-jupiter` y `sqlite-jdbc`.

### 7.5 Prohibido "arreglar" saltándose el problema
* No bajes la versión de Java, no desactives tests, no uses `-DskipTests`, y no borres ni comentes pruebas que fallan para lograr un build verde.
* Un test que falla se corrige en el código o se reporta; no se elimina.
* No reescribas archivos que ya existen y compilan. Ante un error, modifica solo lo mínimo necesario (usa edición puntual, no regeneración completa).

### 7.6 Trabajo por fases con checkpoints
Trabaja en fases y al terminar cada una detente y resume (máximo 10 líneas):
1. `pom.xml` + `schema.sql` → `mvn -B -ntp clean compile`
2. `model` y `model.estado` + sus tests → `mvn -B -ntp test`
3. Repositorios JDBC + sus tests → `mvn -B -ntp test`
4. Servicios (`CatalogoService`, `MantenimientoService`) + sus tests → `mvn -B -ntp test`

No avances a la siguiente fase si la verificación de la actual no pasó. Si la conexión o la sesión se interrumpe, al retomar **revisa primero qué archivos ya existen** y continúa desde ahí sin regenerarlos.

### 7.7 Bitácora de progreso
* Mantén un archivo `PROGRESS.md` en la raíz (no lo incluyas en commits salvo que se pida) con: fase actual, últimos comandos ejecutados y su resultado (OK / FALLÓ), y qué falta.
* Actualízalo después de cada verificación. Permite al usuario ver de un vistazo si el agente avanza o da vueltas.

### 7.8 Reporte final
Al terminar (o al detenerte), entrega siempre:
* Resultado literal del último `mvn -B -ntp clean compile` y `mvn -B -ntp test` (líneas `Tests run: X, Failures: Y, Errors: Z`).
* Lista de archivos creados o modificados en la sesión.
* Cualquier comando que hayas cancelado o que quedó sin verificar.
