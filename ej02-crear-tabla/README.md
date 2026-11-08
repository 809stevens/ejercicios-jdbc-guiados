# Guía 02 — Crear tabla

Crea la tabla `estudiantes` con `CREATE TABLE IF NOT EXISTS`. Es el punto de partida de los demás ejercicios.

## Requisitos

- MySQL iniciado en el panel de control de XAMPP (puerto 3306).
- Base de datos `agenda_estudiantes`. La URL de `db.properties` la crea sola si no existe.
- Haber iniciado MySQL en XAMPP.

## Cómo ejecutar

Esta carpeta es un **proyecto Maven independiente**:

```bash
cd guiados/ej02-crear-tabla
mvn -q exec:java
```

También puedes abrir la carpeta `ej02-crear-tabla` directamente en IntelliJ IDEA,
esperar a que Maven sincronice y ejecutar la clase `Main`.

## Qué observar

Puedes ejecutarlo varias veces: si la tabla ya existe, no falla ni borra datos.

## Archivos importantes

- `src/main/java/stevens/jdbc/guiados/ej02/Main.java` → el ejercicio
- `src/main/java/stevens/jdbc/guiados/ej02/comun/Conexion.java` → conexión centralizada
- `src/main/resources/db.properties` → URL, usuario y clave
