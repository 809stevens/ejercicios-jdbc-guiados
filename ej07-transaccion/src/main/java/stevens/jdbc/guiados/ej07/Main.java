package stevens.jdbc.guiados.ej07;

import stevens.jdbc.guiados.ej07.comun.Esquema;

import stevens.jdbc.guiados.ej07.comun.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * =====================================================================
 * EJERCICIO GUIADO 07 — TRANSACCIONES (COMMIT / ROLLBACK)
 * =====================================================================
 *
 * OBJETIVO:
 *   Entender que varias operaciones SQL pueden agruparse como una
 *   sola unidad: o se guardan todas, o no se guarda ninguna.
 *
 * TRANSMISIÓN POR DEFECTO (AUTO-COMMIT):
 *   Por defecto, cada SQL se confirma solo apenas se ejecuta.
 *   Si haces 2 INSERT y el segundo falla, el primero QUEDA guardado.
 *   Eso puede dejar datos inconsistentes.
 *
 * TRANSACCIÓN MANUAL:
 *   conn.setAutoCommit(false);   // apagamos el guardado automático
 *   // ... varias operaciones ...
 *   conn.commit();               // todo se guarda junto
 *   // o
 *   conn.rollback();             // todo se deshace
 *   conn.setAutoCommit(true);    // volvemos al modo normal
 *
 * CÓMO LO DEMUESTRA ESTE EJERCICIO:
 *
 *   1. prepararCorreoDuplicado()
 *      Escribe una fila con correo transaccion.base@duocuc.cl
 *      para que luego podamos provocar un error de duplicado.
 *
 *   2. contar() antes
 *      Anota cuántos hay.
 *
 *   3. probarRollback()
 *      Intentamos insertar 2 filas en 1 transacción.
 *      La segunda usa el correo base (duplicado) -> falla.
 *      Al fallar, hacemos rollback().
 *      Resultado: el conteo NO aumenta. Ni siquiera la primera
 *      inserción quedó guardada.
 *
 *   4. probarCommit()
 *      Insertamos 2 filas válidas en 1 transacción y hacemos commit().
 *      Resultado: el conteo aumenta en 2.
 *
 *   5. limpiar()
 *      Borra las filas de prueba (transaccion.*). Así el ejercicio
 *      se puede re-ejecutar.
 *
 * SOLO FUNCIONA CON InnoDB:
 *   MyISAM no soporta transacciones. Por eso en el CREATE TABLE del
 *   ejercicio 02 pusimos ENGINE=InnoDB.
 *
 * =====================================================================
 */
public class Main {

    // Paso 1: SQL reutilizable para insertar un estudiante.
    // Usamos una constante para no repetir el texto en 3 métodos.
    private static final String SQL_INSERT =
            "INSERT INTO estudiantes (nombre, carrera, nota, correo) VALUES (?, ?, ?, ?)";

    public static void main(String[] args) {
        // Paso 0: asegurar que la tabla exista.
        Esquema.crearTabla();

        try {
            // Paso 2: dejamos lista la fila "base" para el error duplicado.
            prepararCorreoDuplicado();

            // Paso 3: conteo inicial para comparar antes/después.
            int antes = contar();
            System.out.println("Estudiantes antes de la transacción: " + antes);

            // Paso 4: intento que DEBE fallar y hacer rollback.
            probarRollback();
            System.out.println("Después del rollback:            " + contar()
                    + "  (debe ser igual a " + antes + ")");

            // Paso 5: intento que DEBE funcionar y hacer commit.
            probarCommit();
            System.out.println("Después del commit:              " + contar()
                    + "  (debe ser " + (antes + 2) + ")");

        } catch (SQLException e) {
            // Error general de preparación/conexión.
            System.out.println("Error general: " + e.getMessage());
        } finally {
            // Paso 6: limpiar SIEMPRE, aunque falle algo.
            // finally se ejecuta sí o sí. Así no dejas datos de prueba
            // basura en la tabla.
            limpiar();
        }
    }

    /**
     * Inserta dos filas y luego provoca un error para demostrar rollback.
     *
     * La primera inserción ES válida por sí sola. El problema es que la
     * segunda viola UNIQUE (correo duplicado). Como están en la misma
     * transacción, al fallar la segunda se deshace también la primera.
     */
    private static void probarRollback() throws SQLException {

        // Abrimos UNA conexión: toda la transacción vive en esta conexión.
        try (Connection conn = Conexion.getConnection()) {

            // Apagamos el auto-commit: desde aquí, nada se guarda solo.
            conn.setAutoCommit(false); // inicia la transacción

            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

                // Primera inserción: válida, pero todavía NO confirmada.
                insertar(ps, "Transacción rollback", "Administración", 6.0,
                        "transaccion.rollback@duocuc.cl");

                // Segunda inserción: DUPLICADA -> lanza SQLException.
                // Esto provoca que caigamos al catch de abajo.
                insertar(ps, "Transacción duplicada", "Administración", 6.0,
                        "transaccion.base@duocuc.cl");

                // Si llegamos aquí, todo salió bien -> confirmamos.
                // En este caso NO llegaremos, porque la 2da falló.
                conn.commit();
                System.out.println("Esto no debería imprimirse.");

            } catch (SQLException e) {
                // La transacción falló: deshacemos TODO, incluida la
                // primera inserción que parecía buena.
                conn.rollback();
                System.out.println("Error detectado (" + e.getMessage()
                        + "). Se deshizo TODA la transacción.");
            } finally {
                // Volvemos al modo normal de auto-commit.
                // Si no lo hacemos, la conexión queda en modo manual.
                conn.setAutoCommit(true);
            }
        }
        // Al salir del try-with-resources, la conexión se cierra.
        // Si no hiciste commit ni rollback, JDBC suele hacer rollback
        // automático al cerrar, pero es mejor hacerlo explícito.
    }

    /**
     * Inserta dos filas válidas y confirma con commit.
     * Las dos quedan guardadas juntas, como una unidad.
     */
    private static void probarCommit() throws SQLException {

        try (Connection conn = Conexion.getConnection()) {

            // Iniciamos transacción manual.
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

                // Dos inserciones válidas, todavía no confirmadas.
                insertar(ps, "Transacción uno", "Diseño Gráfico", 5.7,
                        "transaccion.uno@duocuc.cl");
                insertar(ps, "Transacción dos", "Diseño Gráfico", 6.1,
                        "transaccion.dos@duocuc.cl");

                // Confirmamos: ambas quedan guardadas al mismo tiempo.
                conn.commit();
                System.out.println("Las dos filas se guardaron correctamente.");

            } catch (SQLException e) {
                // Si algo falla, deshacemos todo y re-lanzamos.
                conn.rollback();
                throw e;
            } finally {
                // Volvemos a auto-commit.
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Método auxiliar que asigna los 4 parámetros y ejecuta.
     * Evita repetir los 4 setX(...) en varios lugares.
     */
    private static void insertar(PreparedStatement ps, String nombre, String carrera,
                                 double nota, String correo) throws SQLException {
        // Recuerda: índices 1..4, en el orden de los "?" del SQL.
        ps.setString(1, nombre);
        ps.setString(2, carrera);
        ps.setDouble(3, nota);
        ps.setString(4, correo);
        ps.executeUpdate();
    }

    /**
     * Prepara la fila que luego usaremos para provocar el duplicado.
     * Primero borra si ya existe (para poder re-ejecutar), luego inserta.
     */
    private static void prepararCorreoDuplicado() throws SQLException {
        try (Connection conn = Conexion.getConnection();
             Statement st = conn.createStatement()) {

            // Limpieza previa: sin esto, la 2da ejecución fallaría al
            // tratar de insertar la base otra vez.
            st.executeUpdate("DELETE FROM estudiantes WHERE correo = 'transaccion.base@duocuc.cl'");

            // Inserta la fila base que luego duplicaremos a propósito.
            st.executeUpdate("""
                    INSERT INTO estudiantes (nombre, carrera, nota, correo)
                    VALUES ('Base para duplicar', 'Administración', 5.0,
                            'transaccion.base@duocuc.cl')
                    """);
        }
    }

    /**
     * Devuelve el total de filas.
     * Útil para demostrar que rollback no cambia el conteo y commit sí.
     */
    private static int contar() throws SQLException {
        try (Connection conn = Conexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM estudiantes")) {

            // COUNT(*) siempre devuelve 1 fila con el número total.
            // Por eso hacemos next() una sola vez.
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Borra las filas de prueba (transaccion.*) para dejar la BD limpia.
     */
    private static void limpiar() {
        try (Connection conn = Conexion.getConnection();
             Statement st = conn.createStatement()) {

            // LIKE 'transaccion.%' borra todas las de prueba de una vez.
            // % es el comodín de MySQL.
            int borradas = st.executeUpdate(
                    "DELETE FROM estudiantes WHERE correo LIKE 'transaccion.%'");
            System.out.println("Limpieza: " + borradas + " filas de prueba eliminadas.");

        } catch (SQLException e) {
            System.out.println("No se pudo limpiar: " + e.getMessage());
        }
    }
}
