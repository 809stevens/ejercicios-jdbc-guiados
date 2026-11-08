package stevens.jdbc.guiados.ej08.dao.jdbc;

import stevens.jdbc.guiados.ej08.comun.Conexion;
import stevens.jdbc.guiados.ej08.dao.DataAccessException;
import stevens.jdbc.guiados.ej08.dao.EstudianteDAO;
import stevens.jdbc.guiados.ej08.model.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ===================================================================
 * Implementación JDBC del DAO
 * ===================================================================
 *
 * Esta es la ÚNICA clase que sabe SQL de la tabla estudiantes.
 * El Controller, el Service y la vista NO saben de JDBC.
 *
 * REGLAS DE ESTA CLASE:
 *   1. Cada método abre su propia conexión con Conexion.getConnection().
 *   2. Usa PreparedStatement con "?" (nunca concatenar datos).
 *   3. Cierra todo con try-with-resources.
 *   4. Devuelve objetos Estudiante o listas, nunca ResultSet.
 *   5. Traduce SQLException a DataAccessException.
 *
 * MAPEAR:
 *   mapear(ResultSet) convierte la fila actual en un Estudiante.
 *   Se repite en todos los SELECT, por eso está centralizado.
 *
 * FLUJO COMPLETO DE UNA LECTURA:
 *   Controller -> Service -> dao.listar()
 *     -> Conexion.getConnection()     (abre sesión)
 *     -> conn.prepareStatement(SQL)   (prepara consulta)
 *     -> ps.executeQuery()            (MySQL devuelve ResultSet)
 *     -> while (rs.next()) mapear()   (fila -> objeto)
 *     -> return List<Estudiante>
 *   <- ObservableList.setAll(lista)   (actualiza TableView sola)
 *
 * FLUJO DE UNA ESCRITURA (INSERT/UPDATE/DELETE):
 *   Controller -> Service -> dao.guardar(...)
 *     -> ps.setString(1, ...), setDouble(2, ...)
 *     -> ps.executeUpdate()           (devuelve filas tocadas)
 *     -> getGeneratedKeys()           (id para INSERT)
 * ===================================================================
 */
public class JdbcEstudianteDAO implements EstudianteDAO {

    // SQL como constantes: si cambia la tabla, cambias en un solo lugar.
    private static final String SQL_INSERTAR =
            "INSERT INTO estudiantes (nombre, carrera, nota, correo) VALUES (?, ?, ?, ?)";

    private static final String SQL_LISTAR =
            "SELECT id, nombre, carrera, nota, correo FROM estudiantes ORDER BY id";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id, nombre, carrera, nota, correo FROM estudiantes WHERE id = ?";

    private static final String SQL_BUSCAR_POR_NOMBRE =
            // LIKE '%texto%': búsqueda parcial, no necesita coincidir completa.
            "SELECT id, nombre, carrera, nota, correo FROM estudiantes WHERE nombre LIKE ? ORDER BY nombre";

    private static final String SQL_BUSCAR_POR_CORREO =
            "SELECT id, nombre, carrera, nota, correo FROM estudiantes WHERE correo = ?";

    private static final String SQL_ACTUALIZAR =
            // Los 4 primeros ? son los SET, el 5to es el WHERE.
            "UPDATE estudiantes SET nombre = ?, carrera = ?, nota = ?, correo = ? WHERE id = ?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM estudiantes WHERE id = ?";

    @Override
    public void guardar(Estudiante estudiante) {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            // Orden de los "?": nombre, carrera, nota, correo.
            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getCarrera());
            ps.setDouble(3, estudiante.getNota());
            ps.setString(4, estudiante.getCorreo());

            // Ejecuta el INSERT. Devuelve 1 si insertó 1 fila.
            ps.executeUpdate();

            // Recupera el id AUTO_INCREMENT que generó MySQL.
            // Así el objeto en memoria queda completo sin otro SELECT.
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    estudiante.setId(claves.getInt(1));
                }
            }

        } catch (SQLException e) {
            // Traducimos el error técnico a error de negocio.
            // El Controller capturará DataAccessException, no SQLException.
            throw new DataAccessException("No se pudo guardar el estudiante", e);
        }
    }

    @Override
    public List<Estudiante> listar() {
        List<Estudiante> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos el ResultSet fila por fila y mapeamos a objetos.
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DataAccessException("No se pudieron listar los estudiantes", e);
        }
    }

    @Override
    public Optional<Estudiante> buscarPorId(int id) {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                // PRIMARY KEY: como máximo 1 fila.
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("No se pudo buscar por id", e);
        }
    }

    @Override
    public List<Estudiante> buscarPorNombre(String texto) {
        List<Estudiante> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {

            // Los % van DENTRO del valor, no en el SQL.
            ps.setString(1, "%" + texto + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("No se pudo buscar por nombre", e);
        }
        return lista;
    }

    @Override
    public Optional<Estudiante> buscarPorCorreo(String correo) {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_POR_CORREO)) {

            ps.setString(1, correo);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("No se pudo buscar por correo", e);
        }
    }

    @Override
    public boolean actualizar(Estudiante estudiante) {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_ACTUALIZAR)) {

            // Orden: los 4 SET primero, luego el WHERE.
            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getCarrera());
            ps.setDouble(3, estudiante.getNota());
            ps.setString(4, estudiante.getCorreo());
            ps.setInt(5, estudiante.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("No se pudo actualizar el estudiante", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_ELIMINAR)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("No se pudo eliminar el estudiante", e);
        }
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Estudiante.
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
