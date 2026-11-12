# Biblioteca - Patrones de Diseño

Sistema de gestión de biblioteca en **Java 25** con arquitectura **MVC**, interfaz **Swing** y persistencia en **SQLite** mediante JDBC nativo. El proyecto aplica patrones de diseño GoF: **State** para el ciclo de vida de los ejemplares, y **Observer** y **Proxy** en los módulos de eventos y seguridad.

> Proyecto académico (UTP) desarrollado en equipo, dividido en cuatro módulos.

---

## Estado del proyecto

| Módulo | Alcance | Responsable | Estado |
|--------|---------|-------------|--------|
| **A** | Catálogo, patrón State y mantenimiento | @dhaxty | Completo, con 63 tests |
| **B** | Préstamos | _por asignar_ | Pendiente |
| **C** | Seguridad: autenticación, roles y Proxy | _por asignar_ | Pendiente |
| **D** | Presentación Swing y controladores | _por asignar_ | Pendiente |
| — | Observer (paquete `event`) | _por definir_ | Pendiente |

---

## Tecnologías

- **Lenguaje:** Java 25 (`maven.compiler.release` = 25)
- **Interfaz:** Java Swing (MVC puro)
- **Persistencia:** SQLite con JDBC nativo, sin ORM ni Hibernate (`sqlite-jdbc`)
- **Pruebas:** JUnit 5
- **Construcción y dependencias:** Maven

## Requisitos

- JDK 25
- Maven 3.9 o superior

Comprueba tu entorno:

```powershell
java -version
mvn -v
```

## Cómo compilar y probar

```powershell
# Compilar el proyecto completo
mvn -B -ntp clean compile

# Ejecutar las pruebas unitarias y de integración
mvn -B -ntp test
```

Los flags `-B` (modo batch) y `-ntp` (sin barras de descarga) mantienen la salida limpia y no interactiva.

## Consola de prueba del Módulo A

La clase `biblioteca.Main` es una consola de prueba temporal; la interfaz final corresponde al Módulo D.

```powershell
# Recorrido automático sobre una base en memoria (no guarda nada)
mvn -B -ntp compile exec:java "-Dexec.args=--demo"

# Menú interactivo (guarda los datos en biblioteca.db)
mvn -B -ntp compile exec:java
```

El archivo `biblioteca.db` se crea en la carpeta desde donde ejecutes el comando. Está excluido de Git. Para empezar de cero, bórralo.

---

## Arquitectura

Todo el código vive bajo el paquete raíz `biblioteca`:

```
biblioteca/
├─ model/                  Entidades de dominio (Libro, Ejemplar, RegistroMantenimiento)
│  └─ estado/              Patrón State (EstadoEjemplar y sus 3 implementaciones)
├─ repository/             Interfaces de repositorios
│  └─ jdbc/                Implementaciones SQLite con JDBC nativo
├─ service/                Reglas de negocio y transacciones
├─ controller/             Controladores MVC
├─ view/                   Formularios y paneles Swing
├─ security/               Autenticación, roles y Proxy de seguridad
├─ event/                  Patrón Observer y eventos
└─ config/                 Conexión SQLite e inicialización del esquema
```

### Patrón State: ciclo de vida del ejemplar

Un `Ejemplar` delega sus transiciones a un `EstadoEjemplar`. Toda transición no permitida lanza `IllegalStateException`.

| Estado actual | prestar | devolver | enviarAMantenimiento | habilitar |
|---------------|---------|----------|----------------------|-----------|
| **DISPONIBLE** | PRESTADO | error | EN_MANTENIMIENTO | error |
| **PRESTADO** | error | DISPONIBLE | error | error |
| **EN_MANTENIMIENTO** | error | error | error | DISPONIBLE |

### Base de datos

Esquema en `src/main/resources/sql/schema.sql`, con llaves foráneas activadas (`PRAGMA foreign_keys = ON`):

- `libro`: datos del libro y bandera `activo`.
- `ejemplar`: código único, referencia al libro y estado (`DISPONIBLE`, `PRESTADO`, `EN_MANTENIMIENTO`).
- `registro_mantenimiento`: motivo, fecha de ingreso y fecha de habilitación (vacía mientras siga abierto).

Convenciones: las fechas (`LocalDateTime`) se guardan como texto ISO-8601 y los booleanos como `INTEGER` (1 / 0).

---

## Módulo A: qué incluye

- **Catálogo:** registrar, actualizar y desactivar libros (borrado lógico), y registrar ejemplares con código único.
- **Mantenimiento:** enviar un ejemplar a mantenimiento y habilitarlo, con transacciones explícitas (`setAutoCommit(false)`, `commit`, `rollback`) e historial por ejemplar.
- **Repositorios JDBC:** `LibroRepositoryJdbc`, `EjemplarRepositoryJdbc` y `MantenimientoRepositoryJdbc`, que reciben la `Connection` por constructor para compartirla dentro de una transacción.
- **Servicios:** `CatalogoService` y `MantenimientoService`.

## Cómo usar los servicios

```java
try (Connection c = ConexionSqlite.abrirArchivo("biblioteca.db")) {
    ConexionSqlite.inicializarEsquema(c);

    var libros = new LibroRepositoryJdbc(c);
    var ejemplares = new EjemplarRepositoryJdbc(c);
    var mantenimientos = new MantenimientoRepositoryJdbc(c);

    var catalogo = new CatalogoService(libros, ejemplares);
    var mantenimiento = new MantenimientoService(c, ejemplares, mantenimientos);

    var libro = catalogo.registrarLibro("978-0-306-40615-7", "Título", "Autor", "Editorial", 2000, "Ciencia");
    var ejemplar = catalogo.registrarEjemplar("EJ-001", libro.getId());

    mantenimiento.enviarAMantenimiento(ejemplar.getId(), "Tapa rota");
    mantenimiento.habilitar(ejemplar.getId());
}
```

---

## Contratos entre módulos

- **Módulo B (Préstamos):** depende de `EjemplarRepository` y de `ejemplar.prestar()`. No se modifican las firmas públicas de `EjemplarRepository` sin avisar.
- **Módulo C (Seguridad):** gestiona `UsuarioSistema` y la autenticación. Los servicios de catálogo no duplican ni alteran la seguridad.
- **Módulo D (Presentación):** la vista accede a repositorios y servicios solo a través de controladores. **Nunca** hay SQL ni decisiones de negocio en los paquetes `view` o `controller`.

## Reglas del proyecto

1. **Borrado lógico:** los libros nunca se eliminan con `DELETE`; se actualiza `activo = 0`.
2. **Excepciones:** `IllegalArgumentException` para datos nulos, vacíos, de formato inválido o duplicados; `IllegalStateException` para transiciones de estado inválidas o invariantes de negocio violadas.
3. **JDBC:** siempre `PreparedStatement`, nunca concatenación de SQL.
4. **Pruebas con SQLite:** usan una base en memoria o un archivo temporal, nunca el `.db` real.
5. **Git:** no se versionan archivos `.db`, la carpeta `target/` ni configuraciones personales del IDE.

El documento [`AGENTS.md`](AGENTS.md) detalla estas reglas, junto con las pautas para agentes de IA que trabajen en el repositorio.

---

## Flujo de trabajo con Git

`main` debe contener siempre código que compila y pasa los tests. Para cada cambio nuevo:

```powershell
git checkout -b feature/nombre-del-cambio
# ...cambios y commits...
git push -u origin feature/nombre-del-cambio
```

Luego se abre un Pull Request hacia `main`. Antes de pedir revisión, ejecuta:

```powershell
mvn -B -ntp clean test
```

## Clonar el repositorio

```powershell
git clone https://github.com/dhaxty/biblioteca-patrones.git
cd biblioteca-patrones
mvn -B -ntp clean test
```
