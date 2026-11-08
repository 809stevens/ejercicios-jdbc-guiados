# Ejercicio 08 — JavaFX + MySQL (integración completa)

Ejemplo completo y funcional que une todo lo de la unidad: **JDBC, `PreparedStatement`, `ResultSet`, CRUD, patrón DAO, capa Service y `TableView` de JavaFX**.

Cada carpeta sigue siendo un **proyecto Maven independiente**.

---

## Cómo ejecutar

### IntelliJ IDEA

1. Abre el **Panel de Control de XAMPP** e inicia **MySQL**.
2. Abre en IntelliJ la carpeta `ej08-javafx-integracion`.
3. Espera a que Maven sincronice.
4. Ejecuta la clase `stevens.jdbc.guiados.ej08.application.Launcher` con el botón Run.

### Consola

```bash
cd E:\ejercicios-jdbc\guiados\ej08-javafx-integracion
mvn clean javafx:run
```

La primera vez se crea la base de datos `agenda_estudiantes` y la tabla `estudiantes` automáticamente.

---

## Flujo de datos

```
Formulario (TextField)
      ↓
Controller: btnGuardar() lee los campos
      ↓
Service: EstudianteServicio valida (nota 1.0-7.0, correo único)
      ↓
DAO: JdbcEstudianteDAO guarda con PreparedStatement (INSERT ? ? ? ?)
      ↓
MySQL: INSERT INTO estudiantes ... VALUES (?, ?, ?, ?)
      ↓
ResultSet con el id generado -> Estudiante -> ObservableList -> TableView
```

Y a la vuelta:

```
SELECT id, nombre, carrera, nota, correo FROM estudiantes
      ↓ ResultSet (fila por fila)
      ↓ mapear(rs) -> new Estudiante(...)
      ↓ List<Estudiante>
      ↓ ObservableList.setAll(lista)
      ↓ TableView se refresca sola
```

---

## Estructura por capas

```
src/main/java/stevens/jdbc/guiados/ej08/
├── application/
│   ├── App.java          → Punto de entrada JavaFX
│   └── Launcher.java     → Evita problemas de módulos
├── controllers/
│   └── MainController.java → Vista <-> Service (SIN SQL)
├── service/
│   └── EstudianteServicio.java → Reglas de negocio
├── dao/
│   ├── EstudianteDAO.java      → Interfaz (contrato)
│   ├── DataAccessException.java
│   └── jdbc/
│       └── JdbcEstudianteDAO.java → SQL con JDBC (único con SQL)
├── model/
│   └── Estudiante.java   → Modelo con Properties JavaFX
└── comun/
    ├── Conexion.java     → Connection centralizada (db.properties)
    └── Esquema.java      → CREATE TABLE IF NOT EXISTS
```

---

## Conceptos que se muestran

| Concepto | Dónde verlo |
|---|---|
| `Connection` y `DriverManager` | `comun/Conexion.java` |
| `PreparedStatement` con `?` | `dao/jdbc/JdbcEstudianteDAO.java` |
| `executeUpdate()` para INSERT/UPDATE/DELETE | `dao/jdbc/JdbcEstudianteDAO.java` |
| `executeQuery()` y `ResultSet` para SELECT | `dao/jdbc/JdbcEstudianteDAO.java` |
| `getGeneratedKeys()` para el id | `dao/jdbc/JdbcEstudianteDAO.java` |
| `try-with-resources` | Todos los métodos JDBC |
| Patrón DAO con interfaz | `dao/EstudianteDAO.java` + `dao/jdbc/` |
| Capa Service sin SQL | `service/EstudianteServicio.java` |
| Controller sin SQL | `controllers/MainController.java` |
| `TableView` + `ObservableList` | `controllers/MainController.java` |
| `FilteredList` para buscador en vivo | `controllers/MainController.java` |
| `Task` para no congelar la UI | `controllers/MainController.java` |
| `Alert` para errores | `controllers/MainController.java` |
| Modelo con `StringProperty`, etc. | `model/Estudiante.java` |

---

## Qué hace cada botón

| Botón | Qué hace |
|---|---|
| **Guardar** | Valida, inserta en MySQL y agrega a la tabla |
| **Editar** | Actualiza el estudiante seleccionado con los datos del formulario |
| **Eliminar** | Borra el seleccionado de la base y de la tabla |
| **Limpiar** | Vacía el formulario y quita la selección |
| **Actualizar** | Vuelve a leer todo desde la base |
| **Buscar** | Filtra en vivo por nombre, carrera o correo |

Al hacer clic en una fila, sus datos se cargan en el formulario para editarlos.

---

## Configuración

`src/main/resources/db.properties`:

```properties
db.host=localhost
db.port=3306
db.database=agenda_estudiantes
db.user=root
db.password=
```

Todas las consultas usan `PreparedStatement`. Nunca se concatena SQL.

---

## Errores comunes

| Síntoma | Causa | Solución |
|---|---|---|
| `Communications link failure` | MySQL apagado | Inicia MySQL en XAMPP |
| `Access denied` | Usuario/clave mal | Revisa `db.properties` |
| Tabla vacía al abrir | Datos borrados | Ejecuta `sql/02_reiniciar_datos.sql` o guarda uno nuevo |
| Ventana no abre | Error en `App.start()` | Revisa la consola de IntelliJ |
| `No suitable driver` | Maven no sincronizó | Recarga Maven y espera la descarga de `mysql-connector-j` |

## Reiniciar los datos

```bash
cmd /c '"C:\xampp\mysql\bin\mysql.exe" -u root --default-character-set=utf8mb4 < "E:\ejercicios-jdbc\sql\02_reiniciar_datos.sql"'
```
