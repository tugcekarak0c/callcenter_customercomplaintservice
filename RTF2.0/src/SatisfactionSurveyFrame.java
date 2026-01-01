import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * SatisfactionSurveyFrame
 *
 * Satisfaction survey for a complaint (ComplaintID).
 *
 * Uses:
 *  - Complaints (to verify complaint exists)
 *  - ComplaintTexts (Title)
 *  - SatisfactionSurvey (insert: ComplaintID, CallID (NULL), Rating, CreatedAt)
 */
public class SatisfactionSurveyFrame extends JFrame {

    private final int complaintId;

    private JLabel lblInfo;
    private JComboBox<Integer> cmbRating;
    private JButton btnSubmit;
    private JButton btnCancel;

    public SatisfactionSurveyFrame(int complaintId) {
        this.complaintId = complaintId;

        setTitle("Satisfaction Survey - Complaint #" + complaintId);
        setSize(420, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        loadComplaintInfo();
    }

    private void initComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblInfo = new JLabel("Loading complaint info...");
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 14));

        panel.add(lblInfo);
        panel.add(Box.createVerticalStrut(18));

        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ratingPanel.add(new JLabel("Satisfaction rating (1-5):"));

        cmbRating = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        cmbRating.setSelectedIndex(4); 
        ratingPanel.add(cmbRating);

        panel.add(ratingPanel);
        panel.add(Box.createVerticalStrut(18));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnSubmit = new JButton("Submit survey");
        btnCancel = new JButton("Cancel");

        buttonPanel.add(btnSubmit);
        buttonPanel.add(btnCancel);

        panel.add(buttonPanel);

        btnSubmit.addActionListener(e -> handleSubmit());
        btnCancel.addActionListener(e -> dispose());

        setContentPane(panel);
    }

    /* Loads complaint title and checks complaint exists.*/
    private void loadComplaintInfo() {
        String sql = """
            SELECT t.Title
            FROM Complaints c
            LEFT JOIN ComplaintTexts t ON t.ComplaintID = c.ComplaintID
            WHERE c.ComplaintID = ?
            """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, complaintId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("Title");
                    if (title == null || title.isBlank()) title = "(no title)";

                    lblInfo.setText("<html>Complaint #" + complaintId +
                            "<br/>Title: " + title +
                            "<br/><br/>Please rate your satisfaction.</html>");
                    btnSubmit.setEnabled(true);

                } else {
                    lblInfo.setText("Complaint not found.");
                    btnSubmit.setEnabled(false);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            lblInfo.setText("Error while loading complaint info: " + ex.getMessage());
            btnSubmit.setEnabled(false);
        }
    }

    /*Inserts into SatisfactionSurvey by ComplaintID.
     */
    private void handleSubmit() {

    int rating = (Integer) cmbRating.getSelectedItem();

    String checkSql = "SELECT COUNT(*) AS Cnt FROM SatisfactionSurvey WHERE ComplaintID = ?";

    String insertSql = """
        INSERT INTO SatisfactionSurvey (ComplaintID, CallID, Rating, CreatedAt)
        VALUES (?, NULL, ?, SYSDATETIME())
        """;

    //find statusID by name
    String findStatusByNameSql = "SELECT ComplaintStatusID FROM ComplaintStatus WHERE Name = ?";
    // if cant find : fallback
    String fallbackStatusSql = "SELECT TOP 1 ComplaintStatusID FROM ComplaintStatus ORDER BY ComplaintStatusID ASC";

    // update complaint status
    String updateComplaintStatusSql = "UPDATE Complaints SET ComplaintStatusID = ? WHERE ComplaintID = ?";

    try (Connection conn = DbConfig.getConnection()) {

        conn.setAutoCommit(false); // transaction is succesfull

        // Already exists?
        try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setInt(1, complaintId);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt("Cnt") > 0) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this,
                            "A survey has already been recorded for this complaint.",
                            "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
        }

        // Insert survey
        try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
            psInsert.setInt(1, complaintId);
            psInsert.setInt(2, rating);
            psInsert.executeUpdate();
        }

        //  new status id 
        Integer newStatusId = null;
        String desiredStatusName = "Survey Completed"; 

        try (PreparedStatement psFind = conn.prepareStatement(findStatusByNameSql)) {
            psFind.setString(1, desiredStatusName);
            try (ResultSet rs = psFind.executeQuery()) {
                if (rs.next()) newStatusId = rs.getInt("ComplaintStatusID");
            }
        }

        if (newStatusId == null) {
            try (PreparedStatement psFb = conn.prepareStatement(fallbackStatusSql);
                 ResultSet rs = psFb.executeQuery()) {
                if (rs.next()) newStatusId = rs.getInt("ComplaintStatusID");
            }
        }

        if (newStatusId == null) {
            conn.rollback();
            JOptionPane.showMessageDialog(this,
                    "Could not determine a new complaint status to set after survey.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update complaint status
        try (PreparedStatement psUp = conn.prepareStatement(updateComplaintStatusSql)) {
            psUp.setInt(1, newStatusId);
            psUp.setInt(2, complaintId);
            psUp.executeUpdate();
        }

        conn.commit();

        JOptionPane.showMessageDialog(this,
                "Thank you! Your satisfaction survey has been recorded.",
                "Success", JOptionPane.INFORMATION_MESSAGE);

        dispose();

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Error while saving satisfaction survey:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

}
