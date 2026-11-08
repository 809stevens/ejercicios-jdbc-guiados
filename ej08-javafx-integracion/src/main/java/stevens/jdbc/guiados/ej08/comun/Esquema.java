package stevens.jdbc.guiados.ej08.comun;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ===================================================================
 * Esquema: crea la tabla si no existe
 * ===================================================================
 *
 * Esta clase existe para que el proyecto funcione por sí solo:
 * al arrancar la aplicación, se asegura de que la tabla
 * "estudiantes" exista antes de mostrar la ventana.
 *
 * CREATE TABLE IF NOT EXISTS permite ejecutarlo varias veces
 * sin error. Si la tabla ya existe, no hace nada.
 * ===================================================================
 */
public final class Esquema {

    private Esquema() {
    }

    public static void crearTabla() {
        // El mismo CREATE TABLE de los ejercicios 02 y 06.
        String sql = """
                CREATE TABLE IF NOT EXISTS estudiantes (
                    id             INT AUTO_INCREMENT PRIMARY KEY,
                    nombre         VARCHAR(80)  NOT NULL,
                    carrera        VARCHAR(60)  NOT NULL,
                    nota           DECIMAL(3,1) NOT NULL,
                    correo         VARCHAR(120) NOT NULL UNIQUE,
                    fecha_registro TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;

        // try-with-resources cierra Connection y Statement al salir.
        try (Connection conn = Conexion.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo crear la tabla estudiantes", e);
        }
    }
}
