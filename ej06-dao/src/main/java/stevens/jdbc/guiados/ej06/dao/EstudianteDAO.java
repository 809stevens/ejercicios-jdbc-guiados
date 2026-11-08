package stevens.jdbc.guiados.ej06.dao;

import stevens.jdbc.guiados.ej06.comun.Conexion;
import stevens.jdbc.guiados.ej06.comun.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * ===================================================================
 * EstudianteDAO: acceso a datos de la tabla estudiantes
 * ===================================================================
 *
 * DAO = Data Access Object.
 *
 * Esta clase es la ÚNICA que sabe SQL de la tabla estudiantes.
 * El Main, un formulario JavaFX o cualquier controller NO escribe SQL:
 * solo llama dao.guardar(...), dao.listar(), etc.
 *
 * Analogía con tu trabajo anterior con Gson:
 *   - Allá tenías una clase que guardaba/recuperaba JSON.
 *   - Aquí tienes una clase que guarda/recupera filas SQL.
 *   El concepto de encapsular la persistencia es el mismo.
 *
 * REGLAS DE ESTE DAO:
 *   1. Cada método abre su propia conexión con Conexion.getConnection().
 *   2. Usa PreparedStatement con "?" (nunca concatenar datos).
 *   3. Cierra todo con try-with-resources.
 *   4. Devuelve objetos Estudiante o listas, nunca ResultSet.
 *   5. Lanza SQLException y deja que la capa superior decida qué
 *      mostrar al usuario.
 *
 * SQL COMO CONSTANTES:
 *   Guardar el SQL en constantes evita repetir texto y hace más
 *   fácil cambiar una consulta en un solo lugar.
 * ===================================================================
 */
public class EstudianteDAO {

    // SQL de cada operación. Los "?" son marcadores de parámetros.
    private static final String SQL_INSERTAR =
            "INSERT INTO estudiantes (nombre, carrera, nota, correo) VALUES (?, ?, ?, ?)";

    private static final String SQL_LISTAR =
            "SELECT id, nombre, carrera, nota, correo FROM estudiantes ORDER BY id";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id, nombre, carrera, nota, correo FROM estudiantes WHERE id = ?";

    private static final String SQL_BUSCAR_POR_NOMBRE =
            // LIKE '%texto%': búsqueda parcial (contiene el texto).
            // No distingue mayúsculas en utf8mb4_general_ci.
            "SELECT id, nombre, carrera, nota, correo FROM estudiantes WHERE nombre LIKE ? ORDER BY nombre";

    private static final String SQL_ACTUALIZAR =
            // Los 4 primeros ? son los SET, el 5to es el WHERE.
            "UPDATE estudiantes SET nombre = ?, carrera = ?, nota = ?, correo = ? WHERE id = ?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM estudiantes WHERE id = ?";

    /**
     * Guarda un estudiante nuevo y le asigna el id generado por MySQL.
     *
     * Pasos internos:
     *   1. Abre conexión.
     *   2. Prepara INSERT con RETURN_GENERATED_KEYS.
     *   3. Asigna los 4 valores con setString/setDouble.
     *   4. executeUpdate() inserta la fila.
     *   5. getGeneratedKeys() recupera el id y lo deja en el objeto.
     */
    public void guardar(Estudiante estudiante) throws SQLException {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            // Orden de los "?" en el SQL: nombre, carrera, nota, correo.
            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getCarrera());
            ps.setDouble(3, estudiante.getNota());
            ps.setString(4, estudiante.getCorreo());

            // Ejecuta el INSERT. Devuelve 1 si insertó 1 fila.
            ps.executeUpdate();

            // Recupera el id que generó MySQL (AUTO_INCREMENT).
            // Esto permite que el objeto en memoria quede completo,
            // sin tener que hacer otro SELECT.
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    estudiante.setId(claves.getInt(1));
                }
            }
        }
    }

    /**
     * Devuelve todos los estudiantes ordenados por id.
     * Si la tabla está vacía, devuelve una lista vacía (no null).
     */
    public List<Estudiante> listar() throws SQLException {
        List<Estudiante> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos el ResultSet y mapeamos cada fila a objeto.
            // Al salir, try-with-resources cierra rs, ps y conn.
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Busca por clave primaria.
     * Devuelve el estudiante o null si no existe.
     * En Java moderno a veces se devuelve Optional, aquí usamos null
     * por ser un curso introductorio.
     */
    public Estudiante buscarPorId(int id) throws SQLException {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_POR_ID)) {

            // Solo hay 1 "?", con el id buscado.
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                // Como id es PRIMARY KEY, hay como máximo 1 fila.
                // rs.next() dice si existe; si existe, mapeamos.
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Búsqueda parcial por nombre.
     * Ejemplo: buscarPorNombre("ana") encuentra "Ana López",
     * "Mariana", "Fernanda", etc.
     */
    public List<Estudiante> buscarPorNombre(String texto) throws SQLException {
        List<Estudiante> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {

            // Los % son comodines dentro del VALOR, no del SQL.
            // El SQL tiene un solo "?", y le pasamos "%texto%".
            ps.setString(1, "%" + texto + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Actualiza un estudiante existente.
     * @return true si tocó 1 fila, false si el id no existía.
     */
    public boolean actualizar(Estudiante estudiante) throws SQLException {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_ACTUALIZAR)) {

            // Orden: los 4 SET primero, luego el WHERE.
            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getCarrera());
            ps.setDouble(3, estudiante.getNota());
            ps.setString(4, estudiante.getCorreo());
            ps.setInt(5, estudiante.getId());

            // executeUpdate() > 0 significa que encontró y actualizó la fila.
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina por id.
     * @return true si eliminó 1 fila, false si el id no existía.
     */
    public boolean eliminar(int id) throws SQLException {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_ELIMINAR)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Estudiante.
     *
     * Este método existe porque la conversión se repite en todos los
     * SELECT (listar, buscarPorId, buscarPorNombre...).
     * Centralizarlo evita copiar el mismo new Estudiante(...) 5 veces.
     *
     * OJO: el ResultSet debe estar posicionado en una fila válida
     * (ya llamaste rs.next()). Este método no llama next() por sí solo.
     */
    private Estudiante mapear(ResultSet rs) throws SQLException {
        return new Estudiante(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("carrera"),
                rs.getDouble("nota"),
                rs.getString("correo")
        );
    }
}
