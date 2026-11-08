# Guía 04 — Listar

Ejecuta un `SELECT`, recorre el `ResultSet` con `rs.next()` y convierte cada fila en un objeto `Estudiante`.

## Requisitos

- MySQL iniciado en el panel de control de XAMPP (puerto 3306).
- Base de datos `agenda_estudiantes`. La URL de `db.properties` la crea sola si no existe.
- Este proyecto crea la tabla automáticamente si no existe.

## Cómo ejecutar

Esta carpeta es un **proyecto Maven independiente**:

```bash
cd guiados/ej04-listar
mvn -q exec:java
```

También puedes abrir la carpeta `ej04-listar` directamente en IntelliJ IDEA,
esperar a que Maven sincronice y ejecutar la clase `Main`.

## Qué observar

Al final del `while`, `rs.next()` devuelve `false`. Ese es el final de la tabla temporal.

## Archivos importantes

- `src/main/java/stevens/jdbc/guiados/ej04/Main.java` → el ejercicio
- `src/main/java/stevens/jdbc/guiados/ej04/comun/Conexion.java` → conexión centralizada
- `src/main/resources/db.properties` → URL, usuario y clave
