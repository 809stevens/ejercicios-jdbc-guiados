package stevens.jdbc.guiados.ej08.dao;

/**
 * ===================================================================
 * Excepción de dominio para errores de persistencia
 * ===================================================================
 *
 * Por qué existe:
 *   JDBC lanza SQLException (revisada, checked). Si el Controller
 *   tuviera que capturar SQLException en cada botón, la vista
 *   quedaría acoplada a JDBC.
 *
 *   Esta excepción NO revisada (extends RuntimeException)
 *   permite al DAO traducir el error técnico a un error del negocio:
 *
 *     catch (SQLException e) {
 *         throw new DataAccessException("No se pudo guardar", e);
 *     }
 *
 *   Y el Controller captura DataAccessException y muestra un Alert.
 *
 * El mensaje incluye el contexto ("No se pudo guardar") en vez de
 * exponer el mensaje crudo de MySQL al usuario.
 * ===================================================================
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
