package connector;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by gali on 9/15/17.
 */
public class Connector {

    public static String connectionUrl = "jdbc:postgresql://localhost:5432/experts";
    public static String username = "postgres";
    public static String password = "postgres";

    public static java.sql.Connection getConnection() throws SQLException {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return DriverManager.getConnection(connectionUrl, username, password);
    }

    public static void closeConn(java.sql.Connection connection) throws SQLException {
        connection.close();
    }
}
