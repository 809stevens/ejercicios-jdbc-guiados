package stevens.jdbc.guiados.ej02;

import stevens.jdbc.guiados.ej02.comun.Conexion;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * =====================================================================
 * EJERCICIO GUIADO 02 — CREAR LA TABLA CON SQL DDL
 * =====================================================================
 *
 * OBJETIVO:
 *   Crear la tabla "estudiantes" en MySQL usando Java.
 *
 * DDL vs DML:
 *   DDL = Data Definition Language  -> CREATE TABLE, ALTER, DROP
 *   DML = Data Manipulation Language -> INSERT, SELECT, UPDATE, DELETE
 *   Este ejercicio es DDL. Los siguientes son DML.
 *
 * Statement vs PreparedStatement:
 *   - Statement            : para SQL fijo, sin parámetros.
 *   - PreparedStatement    : para SQL con datos que cambian (los "?").
 *   Aquí el CREATE TABLE no recibe datos del usuario, por eso basta
 *   con Statement. Todo lo que venga de un TextField debe ir por
 *   PreparedStatement para evitar inyección SQL.
 *
 * QUÉ SIGNIFICA CADA PARTE DEL CREATE TABLE:
 *
 *   id INT AUTO_INCREMENT PRIMARY KEY
 *     - INT: número entero
 *     - AUTO_INCREMENT: MySQL asigna 1, 2, 3... solo
 *     - PRIMARY KEY: identificador único de cada fila
 *
 *   nombre VARCHAR(80) NOT NULL
 *     - VARCHAR(80): texto de hasta 80 caracteres
 *     - NOT NULL: no puede ir vacío
 *
 *   nota DECIMAL(3,1) NOT NULL
 *     - DECIMAL(3,1): 3 dígitos en total, 1 después de la coma.
 *       Ejemplo válido: 6.2, 7.0. No sirve para 12.5 (necesitaría 4,1).
 *
 *   correo VARCHAR(120) NOT NULL UNIQUE
 *     - UNIQUE: no puede repetirse. Dos estudiantes no pueden compartir
 *       el mismo correo. Si intentas repetirlo, MySQL lanza excepción.
 *
 *   fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 *     - MySQL rellena sola la fecha/hora de inserción.
 *
 *   ENGINE=InnoDB
 *     - Motor de almacenamiento que soporta transacciones (commit/rollback).
 *       Lo usarás en el ejercicio 07. Con MyISAM no funcionan las
 *       transacciones.
 *
 *   DEFAULT CHARSET=utf8mb4
 *     - Juego de caracteres completo para acentos y ñ.
 *
 * IF NOT EXISTS:
 *   Si ejecutas el programa 10 veces, solo crea la tabla la primera.
 *   Sin ese IF, la segunda vez fallaría con "Table already exists".
 *
 * =====================================================================
 */
public class Main {

    public static void main(String[] args) {

        // Paso 1: escribimos el SQL en un texto de varias líneas.
        // """ es un "text block" de Java moderno: permite saltos de línea
        // sin concatenar con + ni con \n. Es más legible para SQL largo.
        String sql = """
                CREATE TABLE IF NOT EXISTS estudiantes (
                    id             INT AUTO_INCREMENT PRIMARY KEY,
                    nombre         VARCHAR(80)  NOT NULL,
                    carrera        VARCHAR(60)  NOT NULL,
                    nota           DECIMAL(3,1) NOT NULL,
                    correo         VARCHAR(120) NOT NULL UNIQUE,
                    fecha_registro TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;

        // Paso 2: abrimos conexión y creamos el Statement.
        //
        // try-with-resources cierra AMBOS recursos al salir:
        //   - Connection
        //   - Statement
        // Si no los cierras, el servidor se queda con conexiones
        // ocupadas y eventualmente rechaza nuevas.
        try (Connection conn = Conexion.getConnection();
             Statement st = conn.createStatement()) {

            // Paso 3: ejecutamos el SQL.
            // st.execute(sql) sirve para cualquier SQL. En este caso,
            // como es CREATE TABLE, no esperamos filas de vuelta.
            // (Para SELECT se usa executeQuery, para INSERT/UPDATE/DELETE
            // se usa executeUpdate. Eso lo verás en los próximos ejercicios.)
            st.execute(sql);

            System.out.println("Tabla \'estudiantes\' creada (o ya existía).");

        } catch (SQLException e) {
            // Paso 4: error típico = MySQL apagado, o usuario sin permiso
            // para crear tablas.
            System.out.println("Error al crear la tabla: " + e.getMessage());
        }
    }
}
