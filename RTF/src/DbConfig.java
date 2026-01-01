import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConfig {

    // Default instance: MSSQLSERVER
    private static final String SERVER_NAME = "LAPTOP-0QFOHLIA";

    private static final String DB_NAME = "RTF";

    private static final String USER = "rtf_user";
    private static final String PASSWORD = "Sena123!";

    // Portu 1433 olarak kullanacağız
    private static final String URL =
            "jdbc:sqlserver://" + SERVER_NAME + ":1433;"
            + "databaseName=" + DB_NAME + ";"
            + "encrypt=false;trustServerCertificate=true;";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("SQL Server JDBC Driver Loaded!");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not load SQL Server JDBC Driver");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
