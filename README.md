# Ejercicios guiados JDBC + MySQL (XAMPP) — copia pública

Ejercicios guiados de conectividad a base de datos con Java, Maven y MySQL/MariaDB de XAMPP.

## Requisitos

| Herramienta | Uso |
|---|---|
| XAMPP | MySQL/MariaDB + phpMyAdmin |
| JDK 17+ | Java |
| Maven | Dependencias y build |
| IntelliJ IDEA | Opcional, recomendado |

## Configuración de la base de datos (por proyecto)

Cada proyecto usa su propio archivo:

```
src/main/resources/db.properties
```

** Ese archivo NO está en GitHub ** a propósito, para no filtrar credenciales.

Para configurarlo:

1. Inicia **MySQL** en el Panel de Control de XAMPP (puerto 3306).
2. Abre el proyecto, por ejemplo `ej01-conexion`.
3. Copia `db.example.properties` como `db.properties`:

```bash
copy src\main\resources\db.example.properties src\main\resources\db.properties
```

4. Edita `db.properties` con tus datos:

```properties
db.host=localhost
db.port=3306
db.database=agenda_estudiantes
db.user=root
db.password=
```

La base `agenda_estudiantes` y la tabla `estudiantes` se crean solas al ejecutar.

## Ejecutar

En IntelliJ: abre la carpeta del ejercicio y ejecuta `Main` o `Launcher`.

Por consola:

```bash
cd ej01-conexion
mvn -q compile
mvn -q exec:java -Dexec.mainClass=stevens.jdbc.guiados.ej01.Main
```

Para el ejemplo JavaFX:

```bash
cd ej08-javafx-integracion
mvn clean javafx:run
```

## Estructura

- `ej01-conexion` — abrir conexión
- `ej02-crear-tabla` — CREATE TABLE
- `ej03-insertar` — INSERT con PreparedStatement
- `ej04-listar` — SELECT + ResultSet
- `ej05-actualizar-eliminar` — UPDATE / DELETE
- `ej06-dao` — patrón DAO
- `ej07-transaccion` — commit / rollback
- `ej08-javafx-integracion` — JavaFX completo con TableView, Service y DAO

> Nunca subas tu `db.properties` ni claves reales. Usa solo `db.example.properties`.
