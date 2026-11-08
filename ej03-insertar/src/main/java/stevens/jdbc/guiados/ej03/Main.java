package stevens.jdbc.guiados.ej03;

import stevens.jdbc.guiados.ej03.comun.Esquema;

import stevens.jdbc.guiados.ej03.comun.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

/**
 * =====================================================================
 * EJERCICIO GUIADO 03 — INSERTAR CON PreparedStatement
 * =====================================================================
 *
 * OBJETIVO:
 *   Guardar varios estudiantes usando SQL parametrizado.
 *
 * PreparedStatement ESENCIAL:
 *   En vez de pegar los datos dentro del texto SQL:
 *
 *     "INSERT INTO estudiantes ... VALUES ('Ana', 'Inf', 6.2)"
 *
 *   se escribe con marcadores:
 *
 *     "INSERT INTO estudiantes ... VALUES (?, ?, ?, ?)"
 *
 *   y luego se asigna cada valor con un método setX(posición, valor).
 *
 * VENTAJAS:
 *   1. Seguridad: evita inyección SQL. El dato nunca se interpreta
 *      como código.
 *   2. Rendimiento: MySQL precompila la consulta una vez.
 *   3. Manejo de tipos: setDouble, setInt, setString dejan los
 *      tipos claros y evitan errores de comillas.
 *
 * NOTA SOBRE LOS ÍNDICES:
 *   La posición empieza en 1, NO en 0.
 *   ps.setString(1, ...) -> primer "?"
 *   ps.setString(2, ...) -> segundo "?"
 *   ...y así hasta el último.
 *
 * CLAVES GENERADAS:
 *   Como id es AUTO_INCREMENT, MySQL genera el id solo.
 *   Con Statement.RETURN_GENERATED_KEYS le decimos a JDBC:
 *   "devuélveme el id que acabas de crear".
 *   Luego lo leemos con ps.getGeneratedKeys().
 *
 * DUPLICADOS:
 *   correo es UNIQUE. Si intentas insertar dos veces el mismo correo,
 *   MySQL lanza SQLIntegrityConstraintViolationException. Aquí la
 *   capturamos fila a fila para no perder las demás inserciones.
 *
 * =====================================================================
 */
public class Main {

    public static void main(String[] args) {
        // Paso 0: nos aseguramos de que la tabla exista.
        // Esquema.crearTabla() es la misma clase que usarás en todos los
        // ejercicios. Así cada proyecto funciona por sí solo.
        Esquema.crearTabla();

        // Paso 1: definimos el SQL con 4 marcadores "?".
        // Orden de columnas: nombre, carrera, nota, correo
        // Orden de "?"               : 1      2        3        4
        String sql = "INSERT INTO estudiantes (nombre, carrera, nota, correo) VALUES (?, ?, ?, ?)";

        // Paso 2: datos de ejemplo en un arreglo.
        // Cada fila es un Object[] con: [String, String, Double, String]
        // En un proyecto real estos datos vendrían de un formulario JavaFX,
        // no de un arreglo fijo.
        Object[][] nuevos = {
                {"Ana López",     "Ingeniería en Informática", 6.2, "ana.lopez@duocuc.cl"},
                {"Luis Silva",    "Ingeniería en Informática", 5.8, "luis.silva@duocuc.cl"},
                {"María Torres",  "Administración",            6.5, "maria.torres@duocuc.cl"},
                {"Pedro Rojas",   "Administración",            4.9, "pedro.rojas@duocuc.cl"},
                {"Camila Díaz",   "Diseño Gráfico",            6.8, "camila.diaz@duocuc.cl"},
                {"Diego Fuentes", "Diseño Gráfico",            5.4, "diego.fuentes@duocuc.cl"},
                {"Valentina Soto","Ingeniería en Informática", 6.9, "valentina.soto@duocuc.cl"},
                {"Jorge Morales", "Administración",            5.1, "jorge.morales@duocuc.cl"}
        };

        // Paso 3: abrimos UNA conexión para todos los INSERT.
        // Abrir una conexión por fila sería lento e ineficiente.
        // Aquí la conexión y el PreparedStatement viven fuera del for.
        //
        // Statement.RETURN_GENERATED_KEYS le pide a MySQL que devuelva
        // el id auto-generado después de cada INSERT.
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Paso 4: recorremos los datos de ejemplo.
            for (Object[] fila : nuevos) {

                // Paso 5: asignamos cada valor a su marcador "?".
                // El número es la POSICIÓN (1, 2, 3, 4) y el tipo debe
                // coincidir con la columna: String, String, Double, String.
                ps.setString(1, (String) fila[0]);   // nombre
                ps.setString(2, (String) fila[1]);   // carrera
                ps.setDouble(3, (Double) fila[2]);   // nota
                ps.setString(4, (String) fila[3]);   // correo

                // Paso 6: ejecutamos el INSERT.
                // executeUpdate() devuelve cuántas filas se insertaron.
                // Para INSERT normalmente devuelve 1.
                try {
                    int filas = ps.executeUpdate();

                    // Paso 7: recuperamos el id generado por MySQL.
                    // getGeneratedKeys() devuelve un ResultSet con el id.
                    // clones.next() avanza a la primera (y única) fila.
                    // getInt(1) lee la primera columna de ese ResultSet.
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        int id = claves.next() ? claves.getInt(1) : -1;
                        System.out.println(filas + " fila insertada. id = " + id
                                + " | " + fila[0]);
                    }

                } catch (SQLIntegrityConstraintViolationException e) {
                    // Esto ocurre si el correo ya existe (UNIQUE).
                    // Como lo capturamos fila a fila, las demás
                    // inserciones del mismo lote NO se pierden.
                    System.out.println("Ya existe el correo " + fila[3] + ", se omite.");
                }
            }

        } catch (SQLException e) {
            // Paso 8: error general de conexión o de SQL.
            // Ej.: MySQL apagado, tabla no existe, sintaxis inválida.
            System.out.println("Error de conexión o de SQL: " + e.getMessage());
        }
    }
}
