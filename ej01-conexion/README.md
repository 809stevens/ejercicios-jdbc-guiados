# Guía 01 — Conexión

Abre una conexión JDBC contra el MySQL de XAMPP y muestra el motor, la versión, la base de datos y el driver que se está usando.

## Requisitos

- MySQL iniciado en el panel de control de XAMPP (puerto 3306).
- Base de datos `agenda_estudiantes`. La URL de `db.properties` la crea sola si no existe.
- No necesita ninguna tabla creada.

## Cómo ejecutar

Esta carpeta es un **proyecto Maven independiente**:

```bash
cd guiados/ej01-conexion
mvn -q exec:java
```

También puedes abrir la carpeta `ej01-conexion` directamente en IntelliJ IDEA,
esperar a que Maven sincronice y ejecutar la clase `Main`.

## Qué observar

Si aparece `MariaDB 10.11` en lugar de `MySQL`, está correcto: XAMPP usa MariaDB. Lo importante es que la conexión sea exitosa.

## Archivos importantes

- `src/main/java/stevens/jdbc/guiados/ej01/Main.java` → el ejercicio
- `src/main/java/stevens/jdbc/guiados/ej01/comun/Conexion.java` → conexión centralizada
- `src/main/resources/db.properties` → URL, usuario y clave
