package stevens.jdbc.guiados.ej04;

import stevens.jdbc.guiados.ej04.comun.Esquema;

import stevens.jdbc.guiados.ej04.comun.Conexion;
import stevens.jdbc.guiados.ej04.comun.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * =====================================================================
 * EJERCICIO GUIADO 04 — LEER CON SELECT Y ResultSet
 * =====================================================================
 *
 * OBJETIVO:
 *   Traer todas las filas de la tabla y convertirlas en objetos Java.
 *
 * QUÉ ES ResultSet:
 *   Es la "tabla temporal" que devuelve MySQL cuando haces un SELECT.
 *   Imagínala como un cursor que se mueve fila por fila:
 *
 *     Cursor antes de la primera fila  ^
 *     Fila 1  <- rs.next() llega aquí
 *     Fila 2  <- rs.next() llega aquí
 *     ...
 *     Fin: rs.next() devuelve false
 *
 * PROTOCOLO SIEMPRE PARA LEER:
 *   1. Executa el SELECT con executeQuery().
 *   2. while (rs.next()) { ... }  // mientras haya filas
 *   3. Dentro del while, lee cada columna con getInt, getString, etc.
 *
 * LEER POR NOMBRE O POR ÍNDICE:
 *   rs.getString("nombre")  -> legible, se rompe si cambias el SELECT
 *   rs.getInt(1)            -> rápido, pero depende del orden del SELECT
 *   En este curso usamos el nombre de la columna para que el código
 *   sea más claro.
 *
 * MAPEAR:
 *   Convertir una fila de la tabla en un objeto Estudiante se llama
 *   "mapear". Aquí lo hacemos directo en el while. En el ejercicio 06
 *   lo centralizarás en un método mapear(ResultSet) dentro del DAO.
 *
 * CONNECTION / PREPARED / RESULTSET JUNTOS:
 *   En el try-with-resources aparecen los tres. Al salir del bloque se
 *   cierran los tres automáticamente, en orden inverso:
 *   primero ResultSet, luego PreparedStatement, luego Connection.
 *
 * =====================================================================
 */
public class Main {

    public static void main(String[] args) {
        // Paso 0: asegurar que la tabla exista.
        Esquema.crearTabla();

        // Paso 1: el SQL de lectura.
        // SELECT id, nombre, ... lista SOLO las columnas que quieres.
        // ORDER BY id hace que se lean de menor a mayor id.
        // En este caso no hay parámetros, pero igual usamos
        // PreparedStatement por consistencia (podría ser Statement).
        String sql = "SELECT id, nombre, carrera, nota, correo FROM estudiantes ORDER BY id";

        // Paso 2: abrimos conexión, preparamos y ejecutamos.
        // executeQuery() es SOLO para SELECT. Devuelve el ResultSet.
        // Si lo usas para INSERT, se interpreta mal.
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Paso 3: cabecera decorativa de la consola.
            System.out.println("ID   | NOMBRE               | CARRERA                      | NOTA | CORREO");
            System.out.println("-----+----------------------+------------------------------+------+-------------------------");

            // Paso 4: contador para mostrar el total al final.
            int contador = 0;

            // Paso 5: recorremos el ResultSet fila por fila.
            // rs.next() devuelve true si hay una fila más, false si
            // llegó al final. Por eso nunca debes leer sin next() antes.
            while (rs.next()) {

                // Paso 6: "mapear" la fila actual a un objeto Estudiante.
                // Cada getX lee la columna de la fila donde está el cursor.
                //
                // getInt("id")       -> columna id, tipo entero
                // getString("nombre")-> columna nombre, tipo texto
                // getDouble("nota")  -> columna nota, tipo decimal
                //
                // Si el nombre de la columna no existe, lanza SQLException.
                Estudiante e = new Estudiante(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("carrera"),
                        rs.getDouble("nota"),
                        rs.getString("correo")
                );

                // Paso 7: mostrar el objeto.
                // El toString() de Estudiante formatea la fila.
                System.out.println(e);
                contador++;
            }

            // Paso 8: resumen.
            System.out.println("--------------------------------------------------------------");
            System.out.println("Total de estudiantes: " + contador);
            // En JavaFX, aquí no imprimirías: pondrías esos objetos en un
            // ObservableList y con tabla.setItems(lista) la UI se refresca.

        } catch (SQLException e) {
            // Errores típicos: tabla no existe, columna mal escrita,
            // MySQL apagado.
            System.out.println("Error al listar: " + e.getMessage());
        }
    }
}
