package stevens.jdbc.guiados.ej08.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import stevens.jdbc.guiados.ej08.comun.Esquema;

import java.io.IOException;

/**
 * ===================================================================
 * Punto de entrada de la aplicación JavaFX
 * ===================================================================
 *
 * App es la clase que extiende Application.
 * Su método start(...) es el main gráfico.
 *
 * Flujo de arranque:
 *   1. Esquema.crearTabla() -> asegura que la tabla existe.
 *      Si MySQL está apagado, esto lanza excepción y la app no abre
 *      con un error claro en consola.
 *   2. FXMLLoader carga Main.fxml y crea su Controller.
 *   3. Se muestra la ventana (Stage).
 *
 * La lógica de base de datos NO está aquí. Solo preparamos el esquema.
 * El resto lo hace el Controller a través del Service y el DAO.
 * ===================================================================
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Paso 1: asegurar que la tabla estudiantes exista.
        // Así la app funciona aunque sea la primera vez que se abre.
        Esquema.crearTabla();

        // Paso 2: cargar la vista FXML.
        // El FXML está en:
        //   src/main/resources/stevens/jdbc/guiados/ej08/Main.fxml
        // getResource con "/" inicial lee desde la raíz del classpath.
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/stevens/jdbc/guiados/ej08/Main.fxml"));

        AnchorPane pane = loader.load();
        Scene scene = new Scene(pane);

        stage.setScene(scene);
        stage.setTitle("Agenda de Estudiantes - JavaFX + MySQL");
        // stage.setResizable(false); // descomenta si quieres tamaño fijo
        stage.show();
        // Al mostrar la ventana, JavaFX llama automáticamente a
        // MainController.initialize(), que carga los datos.
    }

    public static void main(String[] args) {
        launch();
    }
}
