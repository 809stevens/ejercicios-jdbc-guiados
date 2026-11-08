package stevens.jdbc.guiados.ej04.comun;

/**
 * ===================================================================
 * Modelo: representa UNA fila de la tabla estudiantes
 * ===================================================================
 *
 * Es el mismo concepto que usabas con Gson:
 *   - Un objeto Java por registro.
 *   - Se llena desde la base de datos (ResultSet) o desde un formulario.
 *
 * id:
 *   - En la BD lo genera MySQL (AUTO_INCREMENT).
 *   - En Java vale 0 hasta que se guarda. Luego el DAO lo rellena
 *     con getGeneratedKeys().
 *
 * nota:
 *   - double en Java, DECIMAL(3,1) en MySQL. Son compatibles.
 *
 * toString():
 *   - Formato legible para consola.
 *   - En JavaFX se usa para mostrar en ComboBox/ListView,
 *     pero para TableView se usan PropertyValueFactory o
 *     SimpleStringProperty.
 * ===================================================================
 */
public class Estudiante {

    private int id;
    private String nombre;
    private String carrera;
    private double nota;
    private String correo;

    /**
     * Constructor para crear un estudiante nuevo (sin id todavía).
     * El id lo asignará MySQL al insertar.
     */
    public Estudiante(String nombre, String carrera, double nota, String correo) {
        this.nombre = nombre;
        this.carrera = carrera;
        this.nota = nota;
        this.correo = correo;
    }

    /**
     * Constructor para crear un estudiante ya guardado (con id).
     * Se usa al mapear una fila de ResultSet a objeto.
     */
    public Estudiante(int id, String nombre, String carrera, double nota, String correo) {
        this(nombre, carrera, nota, correo);
        this.id = id;
    }

    // ================================================================
    // Getters: solo lectura. JavaFX y JDBC los usan para leer campos.
    // Setters: solo escritura. El DAO usa setId() tras el INSERT.
    // ================================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public double getNota() {
        return nota;
    }

    public void setNota(double nota) {
        this.nota = nota;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * Formato para consola y para depurar.
     * Ejemplo: 1 | Ana López | Ingeniería ... | 6.2 | ana.lopez@...
     */
    @Override
    public String toString() {
        return String.format("%-4d | %-20s | %-28s | %4.1f | %s",
                id, nombre, carrera, nota, correo);
    }
}
