package stevens.jdbc.guiados.ej08.dao;

import stevens.jdbc.guiados.ej08.model.Estudiante;

import java.util.List;
import java.util.Optional;

/**
 * ===================================================================
 * Contrato DAO (interfaz)
 * ===================================================================
 *
 * Esta interfaz define QUÉ operaciones se pueden hacer con la tabla
 * estudiantes, sin decir CÓMO se implementan.
 *
 * - La interfaz describe el negocio.
 * - La clase JdbcEstudianteDAO describe el detalle técnico (JDBC, SQL).
 *
 * Ventajas (de la presentación "Refactorizando capa DAO"):
 *   1. Intercambiabilidad: puedes cambiar de SQLite a MySQL creando
 *      otra clase, sin tocar el Controller.
 *   2. Testing: puedes crear un DAO falso en memoria para probar el
 *      Service sin base de datos.
 *   3. Responsabilidad única: el Controller solo conoce esta interfaz.
 *
 * NOTA: Los métodos NO lanzan SQLException.
 *   El DAO traduce SQLException a DataAccessException (no revisada).
 *   Así la vista no importa java.sql ni sabe de JDBC.
 * ===================================================================
 */
public interface EstudianteDAO {

    /**
     * Guarda un estudiante nuevo.
     * Al volver, el objeto ya trae el id generado por MySQL.
     */
    void guardar(Estudiante estudiante);

    /** Devuelve todos, ordenados por id. */
    List<Estudiante> listar();

    /** Busca por clave primaria. Vacío si no existe. */
    Optional<Estudiante> buscarPorId(int id);

    /** Búsqueda parcial por nombre (LIKE %texto%). */
    List<Estudiante> buscarPorNombre(String texto);

    /** Busca por correo exacto (útil para validar duplicados). */
    Optional<Estudiante> buscarPorCorreo(String correo);

    /**
     * Actualiza un estudiante existente.
     * @return true si tocó 1 fila, false si el id no existía.
     */
    boolean actualizar(Estudiante estudiante);

    /**
     * Elimina por id.
     * @return true si eliminó 1 fila, false si el id no existía.
     */
    boolean eliminar(int id);
}
