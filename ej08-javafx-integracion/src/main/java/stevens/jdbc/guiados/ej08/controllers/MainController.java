package stevens.jdbc.guiados.ej08.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import stevens.jdbc.guiados.ej08.dao.DataAccessException;
import stevens.jdbc.guiados.ej08.dao.EstudianteDAO;
import stevens.jdbc.guiados.ej08.dao.jdbc.JdbcEstudianteDAO;
import stevens.jdbc.guiados.ej08.model.Estudiante;
import stevens.jdbc.guiados.ej08.service.EstudianteServicio;

/**
 * ===================================================================
 * Controller: conecta la Vista (FXML) con la base de datos
 * ===================================================================
 *
 * RESPONSABILIDADES DEL CONTROLLER:
 *   - Leer lo que el usuario escribe en los TextField.
 *   - Llamar al Service (nunca directo al DAO ni a JDBC).
 *   - Actualizar la TableView cuando hay cambios.
 *   - Mostrar Alertas si algo falla.
 *
 * LO QUE EL CONTROLLER NUNCA HACE:
 *   - No escribe SQL (ni SELECT ni INSERT).
 *   - No abre Connection ni PreparedStatement.
 *   - No importa java.sql.
 *   Si ves SQL aquí, el modelo está mal hecho.
 *
 * FLUJO COMPLETO DE UNA ACCIÓN (ejemplo: Guardar):
 *
 *   1. Usuario escribe en TextField y pulsa "Guardar".
 *   2. btnGuardar() lee txtNombre, txtCarrera, txtNota, txtCorreo.
 *   3. Crea un Estudiante en memoria.
 *   4. servicio.guardar(estudiante)
 *        -> valida reglas (nota 1.0-7.0, correo único)
 *        -> dao.guardar(estudiante)
 *             -> INSERT con PreparedStatement
 *             -> devuelve el id generado
 *   5. datos.add(estudiante)  // ObservableList se actualiza sola
 *   6. TableView refleja el nuevo registro sin repintar a mano.
 *
 * TableView + ObservableList:
 *   - ObservableList es la fuente de datos reactiva.
 *   - TableView está enlazada a ella con setItems(...).
 *   - Cuando agregas, cambias o borras en la lista, la tabla cambia sola.
 *   - FilteredList se usa sobre la ObservableList para buscar en vivo.
 *
 * Task para no congelar la UI:
 *   Las consultas a la BD pueden tardar. Si las haces en el hilo de
 *   JavaFX (Application Thread), la ventana se congela.
 *   Con Task, la consulta va en otro hilo y al terminar vuelve al
 *   hilo de JavaFX con setOnSucceeded.
 * ===================================================================
 */
public class MainController {

    // ================================================================
    // Componentes de la vista (vienen del FXML con fx:id)
    // ================================================================
    @FXML
    private TableView<Estudiante> tblEstudiante;

    @FXML
    private TableColumn<Estudiante, Number> colId;

    @FXML
    private TableColumn<Estudiante, String> colNombre;

    @FXML
    private TableColumn<Estudiante, String> colCarrera;

    @FXML
    private TableColumn<Estudiante, Number> colNota;

    @FXML
    private TableColumn<Estudiante, String> colCorreo;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtCarrera;

    @FXML
    private TextField txtNota;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtBuscar;

    @FXML
    private Label lblMensaje;

    // ================================================================
    // Datos y servicios
    // ================================================================

    // ObservableList: la lista que se muestra en la TableView.
    // Cuando la modificas, la UI se refresca automáticamente.
    private final ObservableList<Estudiante> datos = FXCollections.observableArrayList();

    // FilteredList: vista filtrada sobre "datos" para el buscador.
    private FilteredList<Estudiante> filtrados;

    // Service: capa intermedia con reglas de negocio.
    // El Controller SOLO habla con el Service, nunca con el DAO directo.
    private final EstudianteServicio servicio;

    // Constructor: crea la cadena Controller -> Service -> DAO (JDBC).
    // Si quieres cambiar de base, aquí cambias solo JdbcEstudianteDAO
    // por otra implementación y el resto no cambia.
    public MainController() {
        EstudianteDAO dao = new JdbcEstudianteDAO();
        this.servicio = new EstudianteServicio(dao);
    }

    /**
     * Se ejecuta automáticamente al cargar el FXML.
     * Aquí se enlazan las columnas, se carga la tabla y se configura
     * el buscador.
     */
    @FXML
    public void initialize() {
        // ================================================================
        // 1. Enlazar columnas de la TableView con el modelo Estudiante.
        //
        // PropertyValueFactory("nombre") lee nombreProperty() del modelo.
        // Para Number (id, nota) usamos la propiedad numérica, para
        // String la de texto.
        // ================================================================
        colId.setCellValueFactory(celda -> celda.getValue().idProperty());
        colNombre.setCellValueFactory(celda -> celda.getValue().nombreProperty());
        colCarrera.setCellValueFactory(celda -> celda.getValue().carreraProperty());
        colNota.setCellValueFactory(celda -> celda.getValue().notaProperty());
        colCorreo.setCellValueFactory(celda -> celda.getValue().correoProperty());

        // ================================================================
        // 2. Enlazar la TableView con la lista observable.
        //
        // No hacemos tblEstudiante.setItems(servicio.listar()) directo:
        // usamos "datos" (ObservableList) y "filtrados" (FilteredList)
        // para poder buscar sin perder los datos originales.
        // ================================================================
        filtrados = new FilteredList<>(datos, e -> true);
        tblEstudiante.setItems(filtrados);

        // ================================================================
        // 3. Buscador en vivo: al escribir en txtBuscar, la tabla filtra.
        // ================================================================
        txtBuscar.textProperty().addListener((obs, anterior, valor) -> {
            String filtro = valor == null ? "" : valor.trim().toLowerCase();
            filtrados.setPredicate(e ->
                    filtro.isEmpty()
                            || e.getNombre().toLowerCase().contains(filtro)
                            || e.getCarrera().toLowerCase().contains(filtro)
                            || e.getCorreo().toLowerCase().contains(filtro));
        });

        // ================================================================
        // 4. Cargar datos al abrir la ventana (en segundo plano).
        // ================================================================
        cargarDatos();
    }

    /**
     * Carga los estudiantes desde la BD sin congelar la ventana.
     *
     * Usamos Task porque listar() hace una consulta SQL que podría
     * tardar. Si la hicieramos en el hilo de JavaFX, la ventana se
     * quedaría congelada hasta que MySQL responda.
     */
    private void cargarDatos() {
        Task<java.util.List<Estudiante>> tarea = new Task<>() {
            @Override
            protected java.util.List<Estudiante> call() {
                // Esto se ejecuta en OTRO hilo (no en JavaFX).
                return servicio.listar();
            }
        };

        tarea.setOnSucceeded(e -> {
            // Esto vuelve al hilo de JavaFX. Ahí sí puedes tocar la UI.
            datos.setAll(tarea.getValue());
            mensaje("Datos cargados: " + datos.size() + " estudiantes");
        });

        tarea.setOnFailed(e -> {
            mostrarError("No se pudo cargar la lista", tarea.getException());
        });

        // Arranca la tarea en un hilo nuevo.
        new Thread(tarea).start();
    }

    // ================================================================
    // BOTONES
    // ================================================================

    /**
     * Botón "Guardar": crea un estudiante nuevo desde el formulario.
     */
    @FXML
    private void btnGuardar() {
        // Paso 1: leer el formulario y crear el objeto en memoria.
        // Todavía NO está en la BD. El id vale 0.
        Estudiante nuevo = new Estudiante(
                txtNombre.getText().trim(),
                txtCarrera.getText().trim(),
                parseNota(txtNota.getText()),
                txtCorreo.getText().trim()
        );

        try {
            // Paso 2: Service valida y DAO inserta (INSERT).
            // Al volver, "nuevo" ya tiene el id generado por MySQL.
            servicio.guardar(nuevo);

            // Paso 3: agregar a la lista observable.
            // La TableView se actualiza sola, sin repintar a mano.
            datos.add(nuevo);

            mensaje("Guardado: " + nuevo.getNombre());
            limpiarFormulario();

        } catch (IllegalArgumentException ex) {
            // Error de validación del Service (nota fuera de rango, etc.)
            mostrarError("Datos inválidos", ex);
        } catch (DataAccessException ex) {
            // Error de BD (MySQL apagado, duplicado UNIQUE, etc.)
            mostrarError("Error al guardar", ex);
        }
    }

    /**
     * Botón "Editar": actualiza el estudiante seleccionado en la tabla.
     * Al seleccionar una fila, sus datos se cargan en el formulario.
     */
    @FXML
    private void btnEditar() {
        Estudiante seleccionado = tblEstudiante.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mensaje("Selecciona un estudiante de la tabla para editar");
            return;
        }

        try {
            // Paso 1: leer cambios del formulario y aplicarlos al objeto.
            seleccionado.setNombre(txtNombre.getText().trim());
            seleccionado.setCarrera(txtCarrera.getText().trim());
            seleccionado.setNota(parseNota(txtNota.getText()));
            seleccionado.setCorreo(txtCorreo.getText().trim());

            // Paso 2: Service valida y DAO hace UPDATE.
            boolean ok = servicio.actualizar(seleccionado);

            if (ok) {
                // Paso 3: como el modelo usa Properties, la tabla se
                // actualiza sola al cambiar el objeto.
                // refresh() es por si acaso, pero no es obligatorio.
                tblEstudiante.refresh();
                mensaje("Actualizado: " + seleccionado.getNombre());
            } else {
                mensaje("No se encontró el estudiante en la base");
            }

        } catch (RuntimeException ex) {
            mostrarError("Error al actualizar", ex);
        }
    }

    /**
     * Botón "Eliminar": borra el seleccionado de la BD y de la tabla.
     */
    @FXML
    private void btnEliminar() {
        Estudiante seleccionado = tblEstudiante.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mensaje("Selecciona un estudiante de la tabla para eliminar");
            return;
        }

        try {
            // Paso 1: DAO hace DELETE por id.
            boolean ok = servicio.eliminar(seleccionado.getId());

            if (ok) {
                // Paso 2: quitar de la ObservableList.
                // La TableView se actualiza sola.
                datos.remove(seleccionado);
                mensaje("Eliminado: " + seleccionado.getNombre());
                limpiarFormulario();
            } else {
                mensaje("No se encontró el estudiante en la base");
            }

        } catch (DataAccessException ex) {
            mostrarError("Error al eliminar", ex);
        }
    }

    /**
     * Botón "Limpiar": vacía el formulario y quita la selección.
     */
    @FXML
    private void btnLimpiar() {
        limpiarFormulario();
    }

    /**
     * Botón "Actualizar": vuelve a leer todo desde la BD.
     * Útil si otro programa modificó la tabla.
     */
    @FXML
    private void btnActualizar() {
        cargarDatos();
    }

    // ================================================================
    // Métodos de apoyo
    // ================================================================

    /**
     * Al hacer clic en una fila, carga sus datos en el formulario
     * para poder editarlos. Se enlaza desde el FXML con onMouseClicked.
     */
    @FXML
    private void filaSeleccionada() {
        Estudiante e = tblEstudiante.getSelectionModel().getSelectedItem();
        if (e != null) {
            txtNombre.setText(e.getNombre());
            txtCarrera.setText(e.getCarrera());
            txtNota.setText(String.valueOf(e.getNota()));
            txtCorreo.setText(e.getCorreo());
        }
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtCarrera.clear();
        txtNota.clear();
        txtCorreo.clear();
        tblEstudiante.getSelectionModel().clearSelection();
    }

    private double parseNota(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("La nota debe ser un número (ej: 6.5)");
        }
    }

    private void mensaje(String texto) {
        if (lblMensaje != null) {
            lblMensaje.setText(texto);
        }
    }

    private void mostrarError(String titulo, Throwable causa) {
        String detalle = causa != null && causa.getMessage() != null
                ? causa.getMessage()
                : "Error desconocido";

        // Si el error viene de MySQL (SQLException envuelta), mostramos
        // su causa para que veas el mensaje real de la BD.
        if (causa != null && causa.getCause() != null
                && causa.getCause().getMessage() != null) {
            detalle += "  (" + causa.getCause().getMessage() + ")";
        }

        mensaje(titulo + ": " + detalle);

        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(titulo);
        alerta.setContentText(detalle);
        alerta.showAndWait();
    }
}
