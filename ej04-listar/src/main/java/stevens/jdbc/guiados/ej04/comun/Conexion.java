package stevens.jdbc.guiados.ej04.comun;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * ===================================================================
 * Conexión centralizada a MySQL
 * ===================================================================
 *
 * Esta es la ÚNICA clase que sabe:
 *   - host, puerto y base de datos
 *   - usuario y clave
 *   - cómo armar la URL JDBC
 *
 * Ningún DAO ni Main escribe la URL a mano. Todos dicen:
 *   Connection conn = Conexion.getConnection();
 * y esta clase se encarga del resto.
 *
 * - db.properties vive en src/main/resources/db.properties
 * - En tiempo de ejecución Maven lo copia a target/classes/,
 *   por eso se lee con getResourceAsStream("/db.properties")
 *   (el "/" inicial significa "desde la raíz del classpath").
 *
 * Los parámetros técnicos del driver van fijos en construirUrl():
 *   createDatabaseIfNotExist, useSSL, allowPublicKeyRetrieval,
 *   serverTimezone, characterEncoding.
 * No los pongas en db.properties: no cambian por ejercicio.
 * ===================================================================
 */
public final class Conexion {

    // Paso 1: cargamos db.properties una sola vez, al cargar la clase.
    // static {} se ejecuta cuando Java carga Conexion por primera vez.
    private static final Properties CONFIG = new Properties();

    static {
        // getResourceAsStream devuelve null si no encuentra el archivo.
        try (InputStream entrada = Conexion.class.getResourceAsStream("/db.properties")) {
            if (entrada == null) {
                throw new IllegalStateException(
                        "No se encontró db.properties. Revisa src/main/resources.");
            }
            // CONFIG ahora contiene: db.host, db.port, db.database,
            // db.user, db.password
            CONFIG.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer db.properties", e);
        }
    }

    // Constructor privado: nadie hace "new Conexion()".
    // La conexión se pide con el método estático getConnection().
    private Conexion() {
    }

    /**
     * Arma la URL JDBC a partir de db.properties.
     *
     * Formato:
     *   jdbc:mysql://host:puerto/basedatos?parametro=valor&parametro=valor
     *
     * Ejemplo con XAMPP:
     *   jdbc:mysql://localhost:3306/agenda_estudiantes?createDatabaseIfNotExist=true...
     */
    private static String construirUrl() {
        // getProperty(clave, valorPorDefecto): si la clave no está en el
        // archivo, usa el valor por defecto. Así host/port tienen respaldo.
        String host = CONFIG.getProperty("db.host", "localhost");
        String port = CONFIG.getProperty("db.port", "3306");
        String database = CONFIG.getProperty("db.database");

        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?createDatabaseIfNotExist=true"
                + "&useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC"
                + "&characterEncoding=UTF-8";
    }

    /**
     * Devuelve una Connection nueva y lista para usar.
     *
     * IMPORTANTE:
     *   - Cada llamada crea una conexión nueva.
     *   - Quien la llama debe cerrarla, idealmente con try-with-resources:
     *       try (Connection conn = Conexion.getConnection()) { ... }
     *   - Si no la cierras, el servidor se queda sin conexiones libres.
     */
    public static Connection getConnection() throws SQLException {
        // DriverManager es la clase de JDBC que, según la URL, busca el
        // driver adecuado (mysql-connector-j) y abre la sesión.
        return DriverManager.getConnection(
                construirUrl(),
                CONFIG.getProperty("db.user", "root"),
                CONFIG.getProperty("db.password", "")
        );
    }
}
