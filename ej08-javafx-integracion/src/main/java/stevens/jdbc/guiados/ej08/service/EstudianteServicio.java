package stevens.jdbc.guiados.ej08.service;

import stevens.jdbc.guiados.ej08.dao.EstudianteDAO;
import stevens.jdbc.guiados.ej08.model.Estudiante;

import java.util.List;
import java.util.Optional;

/**
 * ===================================================================
 * Capa Service: reglas de negocio
 * ===================================================================
 *
 * El Service está entre el Controller y el DAO.
 *
 * - Controller: lee el formulario, llama al Service, muestra el resultado.
 * - Service:     valida reglas (nota 1.0 a 7.0, campos no vacíos,
 *               correo único) y delega al DAO.
 * - DAO:        solo sabe SQL.
 *
 * Reglas que se aplican aquí (no en el DAO):
 *   - nombre y carrera no vacíos
 *   - nota entre 1.0 y 7.0
 *   - correo con formato válido y no duplicado
 *
 * Si en el futuro cambias la base de datos, el Service no cambia.
 * Si en el futuro agregas una regla nueva, la agregas aquí sin
 * tocar SQL ni la vista.
 * ===================================================================
 */
public class EstudianteServicio {

    // Inyección por constructor: el Service recibe el DAO desde afuera.
    // Así puedes pasar un DAO real (JDBC) o uno falso para pruebas.
    private final EstudianteDAO dao;

    public EstudianteServicio(EstudianteDAO dao) {
        this.dao = dao;
    }

    /**
     * Valida y guarda un estudiante nuevo.
     *
     * @throws IllegalArgumentException si los datos no cumplen las reglas.
     *         El Controller captura esto y muestra un Alert.
     */
    public Estudiante guardar(Estudiante estudiante) {
        validar(estudiante);

        // Regla: el correo debe ser único.
        // El DAO lo busca; si existe, lanzamos error de negocio.
        Optional<Estudiante> existente = dao.buscarPorCorreo(estudiante.getCorreo());
        if (existente.isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un estudiante con el correo " + estudiante.getCorreo());
        }

        // Sin errores, delegamos al DAO (SQL).
        dao.guardar(estudiante);
        return estudiante;
    }

    /** Valida y actualiza. */
    public boolean actualizar(Estudiante estudiante) {
        validar(estudiante);

        // Si otro estudiante tiene ese correo, no permitir.
        Optional<Estudiante> otro = dao.buscarPorCorreo(estudiante.getCorreo());
        if (otro.isPresent() && otro.get().getId() != estudiante.getId()) {
            throw new IllegalArgumentException(
                    "Otro estudiante ya usa el correo " + estudiante.getCorreo());
        }

        return dao.actualizar(estudiante);
    }

    /** Elimina por id. */
    public boolean eliminar(int id) {
        return dao.eliminar(id);
    }

    /** Lista todos. */
    public List<Estudiante> listar() {
        return dao.listar();
    }

    /** Busca por nombre (parcial). */
    public List<Estudiante> buscarPorNombre(String texto) {
        return dao.buscarPorNombre(texto);
    }

    // ================================================================
    // Validaciones. Si fallan, lanzan IllegalArgumentException.
    // ================================================================
    private void validar(Estudiante e) {
        if (e.getNombre() == null || e.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (e.getCarrera() == null || e.getCarrera().isBlank()) {
            throw new IllegalArgumentException("La carrera es obligatoria");
        }
        if (e.getCorreo() == null || !e.getCorreo().contains("@")) {
            throw new IllegalArgumentException("El correo debe ser válido (debe contener @)");
        }
        if (e.getNota() < 1.0 || e.getNota() > 7.0) {
            throw new IllegalArgumentException("La nota debe estar entre 1.0 y 7.0");
        }
    }
}
