# Guía 05 — Actualizar y eliminar

Actualiza la nota de Pedro Rojas y elimina a Diego Fuentes. Muestra cuántas filas afectó cada operación.

## Requisitos

- MySQL iniciado en el panel de control de XAMPP (puerto 3306).
- Base de datos `agenda_estudiantes`. La URL de `db.properties` la crea sola si no existe.
- Este proyecto crea la tabla automáticamente si no existe.

## Cómo ejecutar

Esta carpeta es un **proyecto Maven independiente**:

```bash
cd guiados/ej05-actualizar-eliminar
mvn -q exec:java
```

También puedes abrir la carpeta `ej05-actualizar-eliminar` directamente en IntelliJ IDEA,
esperar a que Maven sincronice y ejecutar la clase `Main`.

## Qué observar

`executeUpdate()` devuelve 0 si el `WHERE` no encontró filas. No lanza error por eso.

## Archivos importantes

- `src/main/java/stevens/jdbc/guiados/ej05/Main.java` → el ejercicio
- `src/main/java/stevens/jdbc/guiados/ej05/comun/Conexion.java` → conexión centralizada
- `src/main/resources/db.properties` → URL, usuario y clave
