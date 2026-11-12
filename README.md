# Biblioteca - Módulo A (Catálogo y State)

Requisitos: JDK 25 y Maven 3.9+.

```powershell
mvn -B -ntp clean compile
mvn -B -ntp test
```

Uso mínimo (sin UI, desde cualquier clase o test):

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
