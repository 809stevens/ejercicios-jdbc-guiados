package stevens.jdbc.guiados.ej08.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ===================================================================
 * Modelo: representa UNA fila de la tabla estudiantes
 * ===================================================================
 *
 * En consola (ejercicios 01-07) el modelo era un POJO simple:
 * int, String, double, String.
 *
 * En JavaFX con TableView, el modelo ideal usa "Properties"
 * (IntegerProperty, StringProperty, DoubleProperty) porque:
 *
 *   - TableView puede enlazar directamente una columna a una
 *     propiedad: colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
 *   - Cuando cambia un valor, la tabla se entera sola.
 *
 * Analogía con tu trabajo con Gson:
 *   - Allá: un objeto = un registro JSON.
 *   - Aquí: un objeto = una fila SQL, pero observable por la UI.
 *
 * id:
 *   - En la BD lo genera MySQL (AUTO_INCREMENT).
 *   - En Java vale 0 hasta que se guarda. Luego el DAO lo rellena
 *     con getGeneratedKeys() y llama setId(...).
 * ===================================================================
 */
public class Estudiante {

    // ================================================================
    // Las Properties son las que lee TableView.
    // SimpleXProperty es la implementación concreta para crearlas.
    // ================================================================
    private final IntegerProperty id;
    private final StringProperty nombre;
    private final StringProperty carrera;
    private final DoubleProperty nota;
    private final StringProperty correo;

    /**
     * Constructor para crear un estudiante NUEVO (sin id todavía).
     * El id lo asignará MySQL al insertar.
     */
    public Estudiante(String nombre, String carrera, double nota, String correo) {
        this.id = new SimpleIntegerProperty(0);
        this.nombre = new SimpleStringProperty(nombre);
        this.carrera = new SimpleStringProperty(carrera);
        this.nota = new SimpleDoubleProperty(nota);
        this.correo = new SimpleStringProperty(correo);
    }

    /**
     * Constructor para crear un estudiante YA GUARDADO (con id).
     * Se usa al mapear una fila de ResultSet a objeto en el DAO.
     */
    public Estudiante(int id, String nombre, String carrera, double nota, String correo) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.carrera = new SimpleStringProperty(carrera);
        this.nota = new SimpleDoubleProperty(nota);
        this.correo = new SimpleStringProperty(correo);
    }

    // ================================================================
    // Getters de Property (los que usa PropertyValueFactory):
    //   nombreProperty(), carreraProperty(), notaProperty(), etc.
    // ================================================================

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public StringProperty carreraProperty() {
        return carrera;
    }

    public DoubleProperty notaProperty() {
        return nota;
    }

    public StringProperty correoProperty() {
        return correo;
    }

    // ================================================================
    // Getters y Setters simples (los que usa el DAO y el Service):
    //   getId(), getNombre(), setNota(...), etc.
    // ================================================================

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public String getCarrera() {
        return carrera.get();
    }

    public void setCarrera(String carrera) {
        this.carrera.set(carrera);
    }

    public double getNota() {
        return nota.get();
    }

    public void setNota(double nota) {
        this.nota.set(nota);
    }

    public String getCorreo() {
        return correo.get();
    }

    public void setCorreo(String correo) {
        this.correo.set(correo);
    }

    /**
     * Formato para consola y para depurar.
     * En ComboBox/ListView se muestra esto, en TableView no.
     */
    @Override
    public String toString() {
        return String.format("%-4d | %-20s | %-28s | %4.1f | %s",
                getId(), getNombre(), getCarrera(), getNota(), getCorreo());
    }
}
