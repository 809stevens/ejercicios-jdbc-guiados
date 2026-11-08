# Guía 06 — DAO

Usa `EstudianteDAO` para crear, listar, buscar y actualizar estudiantes. `Main` no contiene SQL.

## Requisitos

- MySQL iniciado en el panel de control de XAMPP (puerto 3306).
- Base de datos `agenda_estudiantes`. La URL de `db.properties` la crea sola si no existe.
- Este proyecto crea la tabla automáticamente si no existe.

## Cómo ejecutar

Esta carpeta es un **proyecto Maven independiente**:

```bash
cd guiados/ej06-dao
mvn -q exec:java
```

También puedes abrir la carpeta `ej06-dao` directamente en IntelliJ IDEA,
esperar a que Maven sincronice y ejecutar la clase `Main`.

## Qué observar

Abre `Main.java`: no hay `SELECT`, `INSERT` ni `java.sql`. Toda la persistencia quedó en el DAO.

## Archivos importantes

- `src/main/java/stevens/jdbc/guiados/ej06/Main.java` → el ejercicio
- `src/main/java/stevens/jdbc/guiados/ej06/comun/Conexion.java` → conexión centralizada
- `src/main/resources/db.properties` → URL, usuario y clave
