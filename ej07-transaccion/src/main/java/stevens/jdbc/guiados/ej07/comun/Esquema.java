package stevens.jdbc.guiados.ej07.comun;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ===================================================================
 * Esquema: crea la tabla si no existe
 * ===================================================================
 *
 * Esta clase existe para que cada proyecto de ejercicio funcione
 * por sí solo, sin depender del ejercicio anterior.
 *
 * - Si abres ej04-listar sin haber ejecutado ej02-crear-tabla,
 *   la tabla no existe y el SELECT fallaría.
 * - Con Esquema.crearTabla() al inicio del main, cada proyecto
 *   se asegura de que la tabla existe antes de trabajar.
 *
 * CREATE TABLE IF NOT EXISTS permite ejecutarlo varias veces sin error.
 * ===================================================================
 */
public final class Esquema {

    private Esquema() {
    }

    public static void crearTabla() {
        // El mismo CREATE TABLE del ejercicio 02, centralizado aquí.
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

        // Abrimos conexión y Statement, ejecutamos y dejamos que
        // try-with-resources cierre ambos.
        try (Connection conn = Conexion.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            // No mostramos el error aquí: lo convertimos en
            // IllegalStateException para que el main lo reporte.
            throw new IllegalStateException("No se pudo crear la tabla estudiantes", e);
        }
    }
}
