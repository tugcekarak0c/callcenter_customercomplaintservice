import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDashboardFrame extends JFrame {

    private StaffUser staff;

    // UI components
    private JLabel lblWelcome;
    private JLabel lblRole;
    private JLabel lblDepartment;
    private JLabel lblEmail;
    private JLabel lblPhone;

    private JLabel lblTodayCalls;
    private JLabel lblOpenComplaints;

    private JList<String> listCriticalComplaints;
    private DefaultListModel<String> criticalListModel;

    private JButton btnCallManagement;
    private JButton btnCustomerSearch;

    public StaffDashboardFrame(StaffUser staff) {
        this.staff = staff;

        setTitle("Staff Dashboard - " + staff.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        loadStaffInfo();          // Staff + StaffContactInfo + Departments
        loadTodayCallCount();     // Calls (+ CallDetails)
        loadOpenComplaintCount(); // Complaints (IsActive = 1)
        loadCriticalComplaints(); // Complaints + ComplaintPriority (+ ComplaintTexts)

        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Top: staff info
        lblWelcome = new JLabel("Welcome, " + staff.getFullName());
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 16));

        lblRole = new JLabel("Role: " + staff.getRole());
        lblDepartment = new JLabel("Department: (loading...)");
        lblEmail = new JLabel("Email: -");
        lblPhone = new JLabel("Phone: -");

        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 2, 2));
        infoPanel.add(lblWelcome);
        infoPanel.add(lblRole);
        infoPanel.add(lblDepartment);
        infoPanel.add(lblEmail);
        infoPanel.add(lblPhone);

        // Middle: summary counters
        lblTodayCalls = new JLabel("Today's call count: 0");
        lblOpenComplaints = new JLabel("Open complaint count: 0");

        JPanel statsPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        statsPanel.add(lblTodayCalls);
        statsPanel.add(lblOpenComplaints);

        // Critical complaints list
        criticalListModel = new DefaultListModel<>();
        listCriticalComplaints = new JList<>(criticalListModel);

        JScrollPane scrollCritical = new JScrollPane(listCriticalComplaints);
        scrollCritical.setBorder(
                BorderFactory.createTitledBorder("Critical complaints (high priority, open)")
        );

        // Buttons (Complaint Queue button removed)
        btnCallManagement = new JButton("Call Management");
        btnCustomerSearch = new JButton("Customer Search");

        btnCallManagement.addActionListener(e -> {
            CallProcessingFrame cpf = new CallProcessingFrame(staff);
            cpf.setVisible(true);
        });

        btnCustomerSearch.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Customer Search screen will be implemented later.\n" +
                                "(Will use: Customers, CustomerContactInfo, Address, Calls, Complaints, etc.)")
        );

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        buttonPanel.add(btnCallManagement);
        buttonPanel.add(btnCustomerSearch);

        // Main layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel middlePanel = new JPanel(new BorderLayout(10, 10));
        middlePanel.add(statsPanel, BorderLayout.WEST);
        middlePanel.add(scrollCritical, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(middlePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        setContentPane(mainPanel);
    }

    /**
     * Tables used:
     *  - Staff
     *  - StaffContactInfo
     *  - Departments
     */
    private void loadStaffInfo() {
        String sql = """
            SELECT s.FirstName, s.LastName, s.Role,
                   d.Name AS DepartmentName,
                   sci.Email, sci.PhoneNumber
            FROM Staff s
            LEFT JOIN Departments d ON s.DepartmentID = d.DepartmentID
            LEFT JOIN StaffContactInfo sci ON s.StaffID = sci.StaffID
            WHERE s.StaffID = ?
            """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, staff.getStaffId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dept = rs.getString("DepartmentName");
                    String email = rs.getString("Email");
                    String phone = rs.getString("PhoneNumber");

                    if (dept != null) {
                        lblDepartment.setText("Department: " + dept);
                    } else {
                        lblDepartment.setText("Department: (not defined)");
                    }

                    lblEmail.setText("Email: " + (email != null ? email : "-"));
                    lblPhone.setText("Phone: " + (phone != null ? phone : "-"));
                } else {
                    lblDepartment.setText("Department: (not found)");
                    lblEmail.setText("Email: -");
                    lblPhone.setText("Phone: -");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error while loading staff info:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Tables used:
     *  - Calls
     *  - CallDetails
     *
     * Logic: today's calls for this staff (based on CallDetails.StartTime date)
     */
    private void loadTodayCallCount() {
        String sql = """
            SELECT COUNT(*) AS Cnt
            FROM Calls c
            JOIN CallDetails cd ON c.CallID = cd.CallID
            WHERE c.StaffID = ?
              AND CONVERT(date, cd.StartTime) = CONVERT(date, GETDATE())
            """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, staff.getStaffId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Cnt");
                    lblTodayCalls.setText("Today's call count: " + count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblTodayCalls.setText("Today's call count: (error)");
        }
    }

    /**
     * Tables used:
     *  - Complaints
     *
     * Logic: number of active complaints assigned to this staff.
     * If you want all open complaints, remove the AssignedStaffID filter.
     */
    private void loadOpenComplaintCount() {
        String sql = """
            SELECT COUNT(*) AS Cnt
            FROM Complaints
            WHERE IsActive = 1
              AND AssignedStaffID = ?
            """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, staff.getStaffId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Cnt");
                    lblOpenComplaints.setText("Open complaint count: " + count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblOpenComplaints.setText("Open complaint count: (error)");
        }
    }

    /**
     * Tables used:
     *  - Complaints
     *  - ComplaintPriority
     *  - ComplaintTexts (for title)
     *
     * Logic: list top 5 active complaints with high priority (Rank >= 3).
     */
    private void loadCriticalComplaints() {
        criticalListModel.clear();

        String sql = """
            SELECT TOP 5 c.ComplaintID,
                         cp.Name AS PriorityName,
                         cp.Rank,
                         ct.Title
            FROM Complaints c
            JOIN ComplaintPriority cp ON c.ComplaintPriorityID = cp.ComplaintPriorityID
            LEFT JOIN ComplaintTexts ct ON c.ComplaintID = ct.ComplaintID
            WHERE c.IsActive = 1
              AND cp.Rank >= 3   -- assumption: 3 and above is critical
            ORDER BY cp.Rank DESC, c.ComplaintID DESC
            """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<String> items = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("ComplaintID");
                String priority = rs.getString("PriorityName");
                String title = rs.getString("Title");
                int rank = rs.getInt("Rank");

                String text = "#" + id + " [" + priority + " / Rank " + rank + "] - "
                        + (title != null ? title : "(no title)");

                items.add(text);
            }

            if (items.isEmpty()) {
                criticalListModel.addElement("There are no open critical complaints.");
            } else {
                for (String s : items) {
                    criticalListModel.addElement(s);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            criticalListModel.addElement("Error while loading critical complaints: " + e.getMessage());
        }
    }
}
