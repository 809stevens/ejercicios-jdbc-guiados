package stevens.jdbc.guiados.ej06;

import stevens.jdbc.guiados.ej06.comun.Esquema;

import stevens.jdbc.guiados.ej06.comun.Estudiante;
import stevens.jdbc.guiados.ej06.dao.EstudianteDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * =====================================================================
 * EJERCICIO GUIADO 06 — USAR EL PATRÓN DAO
 * =====================================================================
 *
 * OBJETIVO:
 *   Dejar de escribir SQL en el programa principal y delegar toda
 *   la persistencia a una clase EstudianteDAO.
 *
 * QUÉ ES EL PATRÓN DAO (Data Access Object):
 *   Una clase cuya única responsabilidad es hablar con la base de datos.
 *   El resto de la aplicación (menú, formulario JavaFX, etc.) NO sabe
 *   SQL: solo llama métodos como dao.guardar(...) o dao.listar().
 *
 * ANALOGÍA CON Gson (lo que ya sabes):
 *
 *   Con Gson:   RepositorioGson -> guarda/recupera JSON
 *   Con JDBC:   EstudianteDAO   -> guarda/recupera filas SQL
 *
 *   El "cómo se guarda" cambia, el "quién lo hace" se mantiene.
 *
 * VENTAJAS DE USAR DAO:
 *   1. Separación de capas: Main sin SQL.
 *   2. Reutilización: cualquier pantalla usa el mismo DAO.
 *   3. Testeo: puedes reemplazar el DAO real por uno falso (mock).
 *   4. Mantenimiento: si cambia la tabla, solo cambias el DAO.
 *
 * EN ESTE EJERCICIO HACEMOS EN ORDEN:
 *   1. Crear    -> dao.guardar(nuevo)
 *   2. Leer     -> dao.listar()  y   dao.buscarPorNombre(...)
 *   3. Actualizar -> dao.actualizar(...)
 *   4. Eliminar -> dao.eliminar(id)  (comentado para no borrar datos)
 *
 * FÍJATE EN ESTE ARCHIVO: NO aparece Connection, ni PreparedStatement,
 * ni ResultSet, ni SELECT, ni INSERT. Todo eso está dentro de
 * dao/EstudianteDAO.java. Esa es la señal de que el refactor funcionó.
 *
 * =====================================================================
 */
public class Main {

    public static void main(String[] args) {
        // Paso 0: asegurar que la tabla exista.
        Esquema.crearTabla();

        // Paso 1: creamos el DAO.
        // El DAO es solo un objeto sin estado: no guarda nada en memoria.
        // Internamente, cada método abre su propia conexión, hace su SQL,
        // mapea a Estudiante(s) y cierra todo con try-with-resources.
        EstudianteDAO dao = new EstudianteDAO();

        try {
            // PASO 2: CREAR
            // ------------------------------------------------------------
            // Creamos un objeto Estudiante en memoria (todavía sin id).
            // El id lo genera MySQL al insertar.
            Estudiante nuevo = new Estudiante(
                    "Ignacio Vera", "Ingeniería en Informática", 6.1, "ignacio.vera@duocuc.cl");

            // Evitamos duplicar si ejecutas el ejercicio 2 veces:
            // buscarPorNombre devuelve una lista. Si está vacía, no existe.
            if (dao.buscarPorNombre("Ignacio Vera").isEmpty()) {

                // guardar() hace el INSERT con PreparedStatement adentro.
                // Al volver, "nuevo" ya tiene el id asignado (getGeneratedKeys).
                dao.guardar(nuevo);
                System.out.println("Estudiante guardado con id " + nuevo.getId());

            } else {
                System.out.println("Ignacio Vera ya existe, no se inserta de nuevo.");
            }

            // PASO 3: LEER (listar todo)
            // ------------------------------------------------------------
            // listar() devuelve List<Estudiante>. Cada Estudiante salió
            // de una fila del ResultSet (ver mapear() en EstudianteDAO).
            System.out.println("\nListado completo:");
            List<Estudiante> todos = dao.listar();

            // todos.forEach(System.out::println) es la forma corta de
            // "for (Estudiante e : todos) System.out.println(e);"
            todos.forEach(System.out::println);

            // PASO 4: LEER (buscar)
            // ------------------------------------------------------------
            // buscarPorNombre usa LIKE '%ana%'. Es una búsqueda parcial,
            // no necesita coincidir completa.
            System.out.println("\nBúsqueda por nombre 'ana':");
            dao.buscarPorNombre("ana").forEach(System.out::println);

            // PASO 5: ACTUALIZAR
            // ------------------------------------------------------------
            // 1. Buscamos el estudiante en la BD.
            //    get(0) toma el primero de la lista.
            Estudiante encontrado = dao.buscarPorNombre("Ignacio Vera").get(0);

            // 2. Cambiamos la nota en el objeto en memoria.
            encontrado.setNota(6.7);

            // 3. actualizar() ejecuta el UPDATE con el id del objeto.
            //    Devuelve true si tocó 1 fila, false si el id no existía.
            if (dao.actualizar(encontrado)) {
                System.out.println("\nNota actualizada para " + encontrado.getNombre());
            }

            // PASO 6: ELIMINAR (comentado a propósito)
            // ------------------------------------------------------------
            // Si lo descomentas, este estudiante se borra de la BD.
            // Lo dejamos comentado para que puedas re-ejecutar el
            // ejercicio sin perder datos de otros ejercicios.
            // boolean eliminado = dao.eliminar(encontrado.getId());
            // System.out.println("¿Eliminado? " + eliminado);

        } catch (SQLException e) {
            // El DAO lanza SQLException. Como Main no sabe de SQLException
            // en detalle, la capturamos y mostramos el mensaje.
            // En JavaFX, aquí mostrarías un Alert al usuario.
            System.out.println("Error en el DAO: " + e.getMessage());
        }
    }
}
