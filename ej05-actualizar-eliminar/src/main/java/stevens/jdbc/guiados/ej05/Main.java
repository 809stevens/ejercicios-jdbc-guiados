package stevens.jdbc.guiados.ej05;

import stevens.jdbc.guiados.ej05.comun.Conexion;
import stevens.jdbc.guiados.ej05.comun.Esquema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * =====================================================================
 * EJERCICIO GUIADO 05 — UPDATE Y DELETE
 * =====================================================================
 *
 * OBJETIVO:
 *   Modificar y borrar filas. Ya sabes INSERT (ej03) y SELECT (ej04),
 *   ahora faltan las otras dos operaciones del CRUD.
 *
 * executeUpdate() EN INSERT / UPDATE / DELETE:
 *   - executeQuery()  -> devuelve ResultSet   (solo SELECT)
 *   - executeUpdate() -> devuelve int         (filas afectadas)
 *   Ese int es clave:
 *     1 = tocó 1 fila
 *     0 = no tocó ninguna fila (el WHERE no encontró nada)
 *     >1 = tocó varias filas (si no pusiste id en el WHERE)
 *
 * IMPORTANTE SOBRE EL WHERE:
 *   Sin WHERE, UPDATE y DELETE afectan TODAS las filas.
 *   Con WHERE id = ?, afectas solo la fila buscada.
 *   Aquí usamos correo como identificador porque es UNIQUE.
 *
 * EN ESTE PROYECTO:
 *   - actualizarNota(): sube la nota de Pedro de 4.9 a 5.5
 *   - eliminarEstudiante(): borra a Diego Fuentes
 *   - listar(): muestra el resultado
 *
 *   Observa que el eliminarEstudiante() está comentado en el main
 *   para que puedas ejecutar el ejercicio varias veces sin borrar
 *   datos clave. Descoméntalo para ver el DELETE en acción.
 *
 * =====================================================================
 */
public class Main {

    public static void main(String[] args) {

        // Paso 0: asegurar que la tabla exista.
        Esquema.crearTabla();

        // Paso 1: actualizamos la nota de Pedro.
        actualizarNota();

        // Paso 2: (descomenta para borrar a Diego)
        // eliminarEstudiante();

        // Paso 3: mostramos cómo quedó la tabla.
        System.out.println("\nEstado actual de la tabla:");
        listar();
    }

    private static void actualizarNota() {

        // Paso 4: SQL de actualización.
        // SET nota = ?  -> la nueva nota irá en el parámetro 1
        // WHERE correo = ? -> identifica la fila con el parámetro 2
        String update = "UPDATE estudiantes SET nota = ? WHERE correo = ?";

        // Paso 5: conexión + statement con parámetros.
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(update)) {

            // Paso 6: asignamos en orden: primero los SET, luego el WHERE.
            // Ojo: el orden es el de los "?" en el SQL, de izquierda a derecha.
            ps.setDouble(1, 5.5);                          // nuevo valor de nota
            ps.setString(2, "pedro.rojas@duocuc.cl");      // fila a modificar

            // Paso 7: ejecutamos y contamos filas tocadas.
            int filas = ps.executeUpdate();
            // Si filas == 0, el correo no existía. No es error, es información.
            System.out.println("UPDATE -> filas modificadas: " + filas);

        } catch (SQLException e) {
            System.out.println("Error al actualizar: " + e.getMessage());
        }
    }

    private static void eliminarEstudiante() {

        // Paso 8: SQL de borrado.
        // En MySQL, DELETE FROM tabla (sin TRUNCATE) borra filas y
        // deja el AUTO_INCREMENT avanzando. TRUNCATE reinicia el id a 1.
        String delete = "DELETE FROM estudiantes WHERE correo = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(delete)) {

            // Paso 9: solo hay 1 parámetro: el correo de la fila a borrar.
            ps.setString(1, "diego.fuentes@duocuc.cl");

            // Paso 10: ejecutamos y contamos filas eliminadas.
            int filas = ps.executeUpdate();
            System.out.println("DELETE -> filas eliminadas: " + filas);

        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }

    private static void listar() {

        // Paso 11: método de apoyo para ver el estado de la tabla.
        // Es el mismo SELECT del ejercicio 04, pero sin crear objetos:
        // se imprimen los valores directo del ResultSet con printf.
        String sql = "SELECT id, nombre, carrera, nota, correo FROM estudiantes ORDER BY id";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // %-4d, %-20s, etc.: formato de consola para alinear columnas.
                //   %-4d  -> entero alineado a la izquierda, ancho 4
                //   %4.1f -> decimal con 1 decimal, ancho 4
                System.out.printf("%-4d | %-20s | %-28s | %4.1f | %s%n",
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("carrera"),
                        rs.getDouble("nota"),
                        rs.getString("correo"));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }
    }
}
