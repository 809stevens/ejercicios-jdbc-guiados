# Guía 03 — Insertar

Inserta 8 estudiantes de ejemplo usando `PreparedStatement` con parámetros `?`.

## Requisitos

- MySQL iniciado en el panel de control de XAMPP (puerto 3306).
- Base de datos `agenda_estudiantes`. La URL de `db.properties` la crea sola si no existe.
- Este proyecto crea la tabla automáticamente si no existe.

## Cómo ejecutar

Esta carpeta es un **proyecto Maven independiente**:

```bash
cd guiados/ej03-insertar
mvn -q exec:java
```

También puedes abrir la carpeta `ej03-insertar` directamente en IntelliJ IDEA,
esperar a que Maven sincronice y ejecutar la clase `Main`.

## Qué observar

Si lo ejecutas dos veces, los correos repetidos se omiten porque la columna `correo` es `UNIQUE`.

## Archivos importantes

- `src/main/java/stevens/jdbc/guiados/ej03/Main.java` → el ejercicio
- `src/main/java/stevens/jdbc/guiados/ej03/comun/Conexion.java` → conexión centralizada
- `src/main/resources/db.properties` → URL, usuario y clave
