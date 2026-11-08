# Guía 07 — Transacción

Demuestra una transacción: un `rollback` cuando una inserción falla y un `commit` cuando todo sale bien.

## Requisitos

- MySQL iniciado en el panel de control de XAMPP (puerto 3306).
- Base de datos `agenda_estudiantes`. La URL de `db.properties` la crea sola si no existe.
- Este proyecto crea la tabla automáticamente si no existe.

## Cómo ejecutar

Esta carpeta es un **proyecto Maven independiente**:

```bash
cd guiados/ej07-transaccion
mvn -q exec:java
```

También puedes abrir la carpeta `ej07-transaccion` directamente en IntelliJ IDEA,
esperar a que Maven sincronice y ejecutar la clase `Main`.

## Qué observar

El conteo no cambia después del `rollback` y sube en 2 después del `commit`.

## Archivos importantes

- `src/main/java/stevens/jdbc/guiados/ej07/Main.java` → el ejercicio
- `src/main/java/stevens/jdbc/guiados/ej07/comun/Conexion.java` → conexión centralizada
- `src/main/resources/db.properties` → URL, usuario y clave
