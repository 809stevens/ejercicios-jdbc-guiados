package stevens.jdbc.guiados.ej08.application;

import javafx.application.Application;

/**
 * ===================================================================
 * Launcher: evita problemas de módulos en JavaFX
 * ===================================================================
 *
 * En algunos entornos con JDK moderno, lanzar App directamente
 * desde IntelliJ da errores de módulos. Launcher es una clase
 * simple sin herencia de Application que solo redirige.
 *
 * El plugin javafx-maven-plugin ejecuta estaclass en el pom.xml:
 *   stevens.jdbc.guiados.ej08.application.Launcher
 *
 * Y en IntelliJ puedes ejecutar el Launcher con el botón Run.
 * ===================================================================
 */
public class Launcher {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
