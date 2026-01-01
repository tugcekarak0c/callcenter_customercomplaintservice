

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

/**
 * Complaint Queue Screen (Staff view)
 *
 * Uses tables:
 *  - Complaints
 *  - ComplaintTexts
 *  - ComplaintStatus
 *  - ComplaintPriority
 *
 * Opened from StaffDashboard via:
 *   new ComplaintListFrame(staff).setVisible(true);
 */
public class ComplaintListFrame extends JFrame {

    private final StaffUser staff;

    // Swing components
    private JLabel lblFilter;
    private JComboBox<String> cmbStatusFilter;
    private JButton btnRefresh;
    private JScrollPane scrollComplaints;
    private JScrollPane jScrollPane1;
    private JTable tblComplaints;
    private JTextField txtTitle;
    private JTextField txtStatus;
    private JTextField txtPriority;
    private JTextField txtCreatedAt;
    private JTextField txtClosedAt;
    private JScrollPane scrollDescription;
    private JScrollPane jScrollPane2;
    private JTextArea txtDescription;
    private JButton btnCloseComplaint;

    public ComplaintListFrame(StaffUser staff) {
        this.staff = staff;

        setTitle("Complaint Queue - " + staff.getFullName());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        initComponents();
        loadComplaints();

        setLocationRelativeTo(null);
    }

    /**
     * Loads complaints assigned to this staff into the table.
     *
     * Uses:
     *  - Complaints
     *  - ComplaintTexts
     *  - ComplaintStatus
     *  - ComplaintPriority
     *
     * Logic:
     *  - Shows complaints where AssignedStaffID = staff.getStaffId()
     *  - Filter by IsActive according to combo:
     *      "All"   -> all statuses
     *      "Active"-> IsActive = 1
     *      "Close" -> IsActive = 0
     */
    private void loadComplaints() {
        try (Connection conn = DbConfig.getConnection()) {

            String filter = cmbStatusFilter.getSelectedItem().toString();

            StringBuilder sql = new StringBuilder(
                    "SELECT c.ComplaintID, t.Title, s.Name AS StatusName, " +
                    "       p.Name AS PriorityName, c.CreatedAt " +
                    "FROM Complaints c " +
                    "JOIN ComplaintTexts t ON t.ComplaintID = c.ComplaintID " +
                    "JOIN ComplaintStatus s ON s.ComplaintStatusID = c.ComplaintStatusID " +
                    "JOIN ComplaintPriority p ON p.ComplaintPriorityID = c.ComplaintPriorityID " +
                    "WHERE c.AssignedStaffID = ? "
            );

            if (filter.equals("Active")) {
                sql.append("AND c.IsActive = 1 ");
            } else if (filter.equals("Close")) {
                sql.append("AND c.IsActive = 0 ");
            }

            sql.append("ORDER BY c.CreatedAt DESC");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setInt(1, staff.getStaffId());

                try (ResultSet rs = ps.executeQuery()) {
                    DefaultTableModel model = (DefaultTableModel) tblComplaints.getModel();
                    model.setRowCount(0);

                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("ComplaintID"),
                                rs.getString("Title"),
                                rs.getString("StatusName"),
                                rs.getString("PriorityName"),
                                rs.getTimestamp("CreatedAt")
                        });
                    }
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error while loading complaints:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads detailed info for a selected complaint.
     *
     * Uses:
     *  - Complaints
     *  - ComplaintTexts
     *  - ComplaintStatus
     *  - ComplaintPriority
     */
    private void loadComplaintDetail(int complaintId) {
        String sql = """
            SELECT t.Title, t.Description,
                   s.Name AS StatusName,
                   p.Name AS PriorityName,
                   c.CreatedAt, c.ClosedAt, c.IsActive
            FROM Complaints c
            JOIN ComplaintTexts t ON c.ComplaintID = t.ComplaintID
            JOIN ComplaintStatus s ON s.ComplaintStatusID = c.ComplaintStatusID
            JOIN ComplaintPriority p ON p.ComplaintPriorityID = c.ComplaintPriorityID
            WHERE c.ComplaintID = ?
            """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, complaintId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtTitle.setText(rs.getString("Title"));
                    txtDescription.setText(rs.getString("Description"));
                    txtStatus.setText(rs.getString("StatusName"));
                    txtPriority.setText(rs.getString("PriorityName"));
                    txtCreatedAt.setText(String.valueOf(rs.getTimestamp("CreatedAt")));

                    Timestamp closed = rs.getTimestamp("ClosedAt");
                    txtClosedAt.setText(closed == null ? "" : closed.toString());

                    boolean isActive = rs.getBoolean("IsActive");
                    btnCloseComplaint.setEnabled(isActive);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error while loading complaint detail:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Marks a complaint as closed (IsActive=0, ClosedAt = now).
     *
     * Uses:
     *  - Complaints
     */
    private void closeSelectedComplaint() {
        int row = tblComplaints.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a complaint from the list.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int complaintId = (int) tblComplaints.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to close this complaint?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = """
            UPDATE Complaints
            SET IsActive = 0,
                ClosedAt = SYSDATETIME()
            WHERE ComplaintID = ?
            """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, complaintId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Complaint has been closed.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);

            loadComplaints();
            loadComplaintDetail(complaintId);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error while closing complaint:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- UI initialization (NetBeans form kodunun sadeleştirilmiş hali) ----------

    private void initComponents() {

        lblFilter = new JLabel();
        cmbStatusFilter = new JComboBox<>();
        btnRefresh = new JButton();
        scrollComplaints = new JScrollPane();
        jScrollPane1 = new JScrollPane();
        tblComplaints = new JTable();
        txtTitle = new JTextField();
        txtStatus = new JTextField();
        txtPriority = new JTextField();
        txtCreatedAt = new JTextField();
        txtClosedAt = new JTextField();
        scrollDescription = new JScrollPane();
        jScrollPane2 = new JScrollPane();
        txtDescription = new JTextArea();
        btnCloseComplaint = new JButton();

        lblFilter.setText("Condition Filter:");

        cmbStatusFilter.setModel(new DefaultComboBoxModel<>(new String[] { "All", "Active", "Close" }));

        btnRefresh.setText("Refresh list");
        btnRefresh.addActionListener(evt -> loadComplaints());

        tblComplaints.setModel(new DefaultTableModel(
                new Object [][] {
                        {null, null, null, null, null},
                        {null, null, null, null, null},
                        {null, null, null, null, null},
                        {null, null, null, null, null}
                },
                new String [] {
                        "ComplaintID", "Title", "Status", "Priority", "CreatedAt"
                }
        ));
        tblComplaints.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tblComplaints.getSelectedRow();
                if (row >= 0) {
                    int complaintId = (int) tblComplaints.getValueAt(row, 0);
                    loadComplaintDetail(complaintId);
                }
            }
        });
        jScrollPane1.setViewportView(tblComplaints);
        scrollComplaints.setViewportView(jScrollPane1);

        txtTitle.setEditable(false);
        txtStatus.setEditable(false);
        txtPriority.setEditable(false);
        txtCreatedAt.setEditable(false);
        txtClosedAt.setEditable(false);

        txtDescription.setEditable(false);
        txtDescription.setColumns(20);
        txtDescription.setRows(5);
        jScrollPane2.setViewportView(txtDescription);
        scrollDescription.setViewportView(jScrollPane2);

        btnCloseComplaint.setText("Close the complaint");
        btnCloseComplaint.addActionListener(evt -> closeSelectedComplaint());

        // Layout (GroupLayout) – NetBeans formundan alınmış yapı
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(scrollComplaints, GroupLayout.PREFERRED_SIZE, 485, GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(162, 162, 162)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(txtTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtStatus, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtPriority, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtCreatedAt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtClosedAt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(114, 114, 114)
                                                .addComponent(scrollDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                .addGap(72, 72, 72))
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(46, 46, 46)
                                                .addComponent(lblFilter)
                                                .addGap(73, 73, 73)
                                                .addComponent(cmbStatusFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(83, 83, 83)
                                                .addComponent(btnRefresh))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(190, 190, 190)
                                                .addComponent(btnCloseComplaint)))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblFilter)
                                        .addComponent(cmbStatusFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnRefresh))
                                .addGap(43, 43, 43)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollComplaints, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(txtTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(30, 30, 30)
                                                .addComponent(txtStatus, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(28, 28, 28)
                                                .addComponent(txtPriority, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(42, 42, 42)
                                                .addComponent(txtCreatedAt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(31, 31, 31)
                                                .addComponent(txtClosedAt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(44, 44, 44)
                                                .addComponent(scrollDescription, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                                .addComponent(btnCloseComplaint)
                                .addGap(17, 17, 17))
        );

        pack();
    }
}
