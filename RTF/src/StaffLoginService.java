import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffLoginService {

    /**
     * Username + PasswordHash ile StaffLogins tablosundan
     * giriş kontrolü yapar, Staff ile join eder.
     * Başarılıysa StaffUser döner, değilse null.
     */
    public StaffUser authenticate(String username, String passwordPlain) {

        String sql = """
            SELECT s.StaffID, s.FirstName, s.LastName, s.Role
            FROM StaffLogins sl
            JOIN Staff s ON sl.StaffID = s.StaffID
            WHERE sl.Username = ?
              AND sl.PasswordHash = ?
        """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, passwordPlain);  // şimdilik hash yok, düz şifre

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int staffId = rs.getInt("StaffID");
                    String firstName = rs.getString("FirstName");
                    String lastName = rs.getString("LastName");
                    String role = rs.getString("Role");

                    return new StaffUser(staffId, firstName, lastName, role);
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
