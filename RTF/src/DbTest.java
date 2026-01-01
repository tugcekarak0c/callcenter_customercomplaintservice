import java.sql.Connection;

public class DbTest {
    public static void main(String[] args) {
        try (Connection conn = DbConfig.getConnection()) {
            System.out.println("Connected to SQL Server successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
