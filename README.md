# Biblioteca Universitaria

Aplicación de escritorio en **Java 25 + Swing + SQLite** para administrar el catálogo, los préstamos y el mantenimiento de una biblioteca universitaria. Proyecto grupal del curso de Diseño de Patrones (UTP).

Arquitectura **MVC + Service + Repository**, con los patrones GoF: **State**, **Facade**, **Proxy**, **Observer** y **Factory**.

## Quién usa la aplicación

Inician sesión el **administrador** y el **bibliotecario**. Los estudiantes y docentes son lectores registrados y no acceden al programa.

## Funcionalidades

- Libros y ejemplares físicos (copias con código único y estado).
- Mantenimiento de ejemplares con historial de motivo e ingreso/habilitación.
- Lectores, préstamos y devoluciones.
- Cuentas del personal con roles de seguridad y contraseñas encriptadas (Hash).
- Dashboard en tiempo real: total de libros, ejemplares disponibles, préstamos activos y vencidos.

**Reglas principales:** 5 préstamos simultáneos por lector, 7 días calendario, los atrasos bloquean nuevos préstamos. Sin reservas, renovaciones ni multas. Libros y lectores se desactivan lógicamente, sin borrar su historial.

## Estado del proyecto (100% Integrado)

| Módulo | Alcance | Estado |
|---|---|---|
| **A · Catálogo / State** | Libro, Ejemplar, mantenimiento | ✅ Listo, con pruebas |
| **B · Préstamos / Facade**| Lector, Prestamo, transacciones | ✅ Listo, con pruebas |
| **C · Seguridad / Proxy** | Login, roles, Hash, cuentas | ✅ Listo, con pruebas |
| **D · Presentación / MVC**| Swing (Nimbus), Dashboard, Observer | ✅ Listo, integrado |

## Requisitos

- JDK 25
- Maven 3.9 o superior
- NetBeans (u otro IDE con soporte Maven)

## Cómo empezar

```bash
git clone https://github.com/dhaxty/biblioteca-patrones.git
cd biblioteca-patrones

# 1. Compilar y ejecutar toda la suite de pruebas (SQLite en memoria)
mvn test          

# 2. Iniciar la aplicación de escritorio (Interfaz Gráfica)
mvn exec:java -Dexec.mainClass="edu.biblioteca.Main"

# 3. Generar el ejecutable JAR
mvn -q -DskipTests package       
```

> **🔑 Credenciales de Acceso Inicial:** 
> Al ejecutar el sistema por primera vez, la base de datos se inicializa automáticamente y crea una cuenta maestra.
> - **Usuario:** `admin`
> - **Contraseña:** `Admin123*`

## Estructura de Capas

```text
src/main/java/edu/biblioteca/
├─ Main.java         Punto de entrada y cableado de dependencias
├─ model/            Entidades del dominio
│  └─ estado/        Patrón State del ejemplar
├─ view/             Pantallas Swing
├─ controller/       Controladores MVC
├─ service/          Reglas de negocio y Facade
├─ repository/       Interfaces de repositorio
│  └─ jdbc/          Implementaciones JDBC
├─ security/         Login, permisos y Proxy
├─ event/            Observer
└─ config/           Conexión SQLite y transacciones
src/main/resources/db/   Scripts SQL (catalogo, prestamos, seguridad)
src/test/java/           Pruebas JUnit 5
docs/                    Documentación técnica y diagramas
```

## Ciclo de vida de un ejemplar (Patrón State)

```text
Disponible ──prestar──▶ Prestado ──devolver──▶ Disponible
    │                      │
    │                      └─devolverConDanio─▶ En mantenimiento
    └─enviarAMantenimiento─▶ En mantenimiento ──habilitar──▶ Disponible
```

## Documentación del Equipo

- [`AGENTS.md`](AGENTS.md): Arquitectura, reglas, contratos entre módulos y convenciones (Guía rectora del proyecto).
- [`docs/`](docs/): Detalles y decisiones técnicas de cada módulo implementado (PARTE_A, PARTE_B, etc).

## Trabajo en equipo

La rama `main` se considera la versión estable y final. Las tareas se trabajaron en ramas `feature/modulo-tarea` con commits pequeños, integrándose vía Pull Request tras superar las pruebas automatizadas.

*Nota estricta del repositorio: No subir archivos `.db`, la carpeta `target/` ni credenciales en texto plano.*