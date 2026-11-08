package stevens.jdbc.guiados.ej01;

import stevens.jdbc.guiados.ej01.comun.Conexion;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * =====================================================================
 * EJERCICIO GUIADO 01 — ABRIR UNA CONEXIÓN JDBC
 * =====================================================================
 *
 * OBJETIVO:
 *   Abrir una conexión contra el MySQL de XAMPP y comprobar que todo
 *   funciona: driver, URL, usuario, clave y puerto.
 *
 * ANTES DE EJECUTAR:
 *   1. Abre el Panel de Control de XAMPP e inicia MySQL (puerto 3306).
 *   2. Abre db.properties y verifica que host, database, user y password
 *      sean los correctos. En XAMPP por defecto: root / sin clave.
 *
 * QUÉ ES CADA CLASE QUE USA ESTE EJERCICIO:
 *
 *   Connection
 *     Representa la sesión abierta con la base de datos. Sin Connection
 *     no existe comunicación. Todo lo demás (Statement, PreparedStatement)
 *     se crea desde una Connection.
 *
 *   DatabaseMetaData
 *     Es el "ficha técnica" de la conexión: nombre del motor, versión,
 *     nombre del driver, base actual. Sirve para diagnosticar.
 *
 *   SQLException
 *     Es la excepción que lanza JDBC cuando algo sale mal: MySQL apagado,
 *     clave incorrecta, puerto equivocado, base inexistente, etc.
 *
 * FLUJO DE ESTE PROGRAMA:
 *
 *   1. Conexion.getConnection()
 *        Lee db.properties, arma la URL JDBC:
 *          jdbc:mysql://localhost:3306/agenda_estudiantes?...
 *        y llama a DriverManager.getConnection(url, user, password).
 *
 *   2. getMetaData()
 *        Pide información sobre la conexión recién abierta.
 *
 *   3. getCatalog()
 *        Devuelve el nombre de la base actual (agenda_estudiantes).
 *
 *   4. Se imprime motor, versión y driver para verificar:
 *        Motor: MySQL
 *        Versión: 5.5.5-10.11.16-MariaDB  <- XAMPP trae MariaDB
 *        Base: agenda_estudiantes
 *        Driver: MySQL Connector/J 9.3.0
 *
 *   5. El try-with-resources cierra la conexión al salir del bloque,
 *      aunque ocurra un error. Nunca se deja una Connection abierta.
 *
 * ERRORES TÍPICOS QUE VAS A VER AQUÍ:
 *   - Communications link failure  -> MySQL apagado
 *   - Access denied for user       -> usuario/clave mal en db.properties
 *   - No suitable driver found     -> falta el jar del driver (Maven no sincronizó)
 *
 * =====================================================================
 */
public class Main {

    public static void main(String[] args) {

        // Paso 0: mensaje de control. Si ves esto, el programa arrancó.
        System.out.println("Intentando conectar a MySQL...");

        // Paso 1: abrimos la conexión.
        //
        // try-with-resources significa:
        //   try ( recurso ) { ... }
        // Al salir del bloque (éxito o error) Java llama automáticamente
        // a conn.close(). Así no quedan conexiones abiertas que agoten
        // el límite del servidor ("Too many connections").
        //
        // Conexion.getConnection() NO abre nada por sí sola: devuelve una
        // Connection nueva cada vez. Por eso el try la recibe aquí.
        try (Connection conn = Conexion.getConnection()) {

            // Paso 2: pedimos la ficha técnica de la conexión.
            // DatabaseMetaData es un objeto que responde preguntas como:
            // "¿qué motor eres?", "¿qué versión?", "¿qué driver te conecta?"
            DatabaseMetaData meta = conn.getMetaData();

            // Paso 3: imprimimos diagnóstico.
            // getDatabaseProductName() puede devolver "MySQL" o, en XAMPP,
            // sigue respondiendo MySQL aunque el servidor sea MariaDB.
            System.out.println("Conexión exitosa.");
            System.out.println("Motor    : " + meta.getDatabaseProductName());

            // getDatabaseProductVersion() sirve para saber si estás en
            // MariaDB 10.11 o MySQL 8, por si un SQL no es compatible.
            System.out.println("Versión  : " + meta.getDatabaseProductVersion());

            // getCatalog() en MySQL devuelve la base actual seleccionada
            // en la URL (agenda_estudiantes). Si dice "null", la URL no
            // traía base y estarías conectado "al servidor" sin base.
            System.out.println("Base     : " + conn.getCatalog());

            // getDriverName() debe decir MySQL Connector/J. Si dice otra
            // cosa, tienes otro driver o una versión vieja.
            System.out.println("Driver   : " + meta.getDriverName() + " " + meta.getDriverVersion());

            // Al salir de este bloque, conn.close() se ejecuta solo.

        } catch (SQLException e) {
            // Paso 4: si la conexión falla, caemos aquí.
            // No es un error de compilación: es un error en tiempo de
            // ejecución. Por eso se captura y se muestra información útil.
            System.out.println("No se pudo conectar.");

            // getMessage() da el texto corto: "Access denied...", "Communications link failure"...
            System.out.println("Mensaje : " + e.getMessage());

            // getErrorCode() es el código numérico de MySQL (ej. 1045, 2003).
            // Útil para buscar en foros o en la documentación.
            System.out.println("Código  : " + e.getErrorCode());

            System.out.println("\nRevisa:");
            System.out.println("  - ¿Está MySQL iniciado en XAMPP?");
            System.out.println("  - ¿El puerto es 3306?");
            System.out.println("  - ¿Usuario/clave en db.properties?");
            // Nota: en una app real no mostrarías la clave al usuario.
            // Aquí se imprime para aprender a diagnosticar.
        }
    }
}
