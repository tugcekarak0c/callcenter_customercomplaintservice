import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
/**
 * ComplaintListFrame
 *
 * Displays a list of complaints for staff users.
 * Allows viewing, filtering, and selecting complaints
 * to see their details or perform further actions.
 *
 * Tables used:
 *  - Complaints (main complaint records)
 *  - ComplaintTexts (titles and descriptions)
 *  - ComplaintStatus (current complaint status)
 *  - ComplaintPriority (priority information)
 *  - Customers (customer information related to complaints)
 *
 * Used by staff to manage and track complaints efficiently.
 */



public class ComplaintListFrame extends JFrame {

    private final StaffUser staff;
    private static final int CLOSED_STATUS_ID = 3;

    private final Color PRIMARY_DARK = new Color(62, 39, 35);    
    private final Color ACCENT_COLOR = new Color(191, 54, 12);   
    private final Color BG_COLOR = new Color(245, 245, 245);    
    private final Color TEXT_COLOR = new Color(33, 33, 33);     
    private final Color TABLE_HEADER_TXT = Color.WHITE;          

    // UI
    private JComboBox<String> cmbStatusFilter;
    private JButton btnRefresh;
    private JButton btnLogout;
    private JTable tblComplaints;

    private JTextField txtTitle;
    private JTextField txtStatus;
    private JTextField txtPriority;
    private JTextField txtCreatedAt;
    private JTextField txtClosedAt;

    private JTextField txtCustomerId;
    private JTextField txtProductId;
    private JTextField txtCategory;

    private JTextArea txtDescription;
    private JButton btnCloseComplaint;

    private Integer selectedComplaintId = null;
    private boolean selectedComplaintIsActive = true;

    public ComplaintListFrame(StaffUser staff) {
        this.staff = staff;
        setTitle("Farm Management | Staff Desk - " + staff.getFullName());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        getContentPane().setBackground(BG_COLOR);

        initComponents();
        loadComplaints();

        pack();
        setLocationRelativeTo(null);
        setSize(1100, 700); 
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PRIMARY_DARK);
        topPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblFilter = new JLabel("Status Filter:");
        lblFilter.setForeground(Color.WHITE);
        lblFilter.setFont(new Font("Arial", Font.BOLD, 14));

        cmbStatusFilter = new JComboBox<>(new String[]{"All", "Active", "Close"});
        cmbStatusFilter.setBackground(Color.WHITE);
        cmbStatusFilter.setForeground(TEXT_COLOR);

        btnRefresh = createHeaderButton("Refresh List");
        btnRefresh.addActionListener(e -> loadComplaints());

        btnLogout = createHeaderButton("Log out");
        btnLogout.setBackground(new Color(93, 64, 55)); 
        btnLogout.addActionListener(e -> {
            new RoleSelectionFrame().setVisible(true);
            dispose();
        });

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftTop.setOpaque(false); 
        leftTop.add(lblFilter);
        leftTop.add(cmbStatusFilter);
        leftTop.add(btnRefresh);

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightTop.setOpaque(false);
        rightTop.add(btnLogout);

        topPanel.add(leftTop, BorderLayout.WEST);
        topPanel.add(rightTop, BorderLayout.EAST);

        tblComplaints = new JTable(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Title", "Status", "Priority", "Created At"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });

        styleTable(tblComplaints); 

        tblComplaints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblComplaints.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblComplaints.getSelectedRow();
                if (row >= 0) {
                    int complaintId = (int) tblComplaints.getValueAt(row, 0);
                    loadComplaintDetail(complaintId);
                }
            }
        });
        JScrollPane scrollTable = new JScrollPane(tblComplaints);
        scrollTable.getViewport().setBackground(Color.WHITE);
        scrollTable.setBorder(BorderFactory.createEmptyBorder()); 

        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBackground(BG_COLOR);
        detailPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY), 
                new EmptyBorder(20, 20, 20, 20)
        ));

        Dimension fieldSize = new Dimension(220, 30);

        txtTitle = createFixedField(fieldSize);
        txtStatus = createFixedField(fieldSize);
        txtPriority = createFixedField(fieldSize);
        txtCreatedAt = createFixedField(fieldSize);
        txtClosedAt = createFixedField(fieldSize);
        txtCustomerId = createFixedField(fieldSize);
        txtProductId = createFixedField(fieldSize);
        txtCategory = createFixedField(fieldSize);

        txtDescription = new JTextArea(8, 25);
        txtDescription.setEditable(false);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setFont(new Font("Arial", Font.PLAIN, 13));
        txtDescription.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        JScrollPane scrollDescription = new JScrollPane(txtDescription);
        scrollDescription.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollDescription.setPreferredSize(new Dimension(300, 140));

        // Action Button 
        btnCloseComplaint = new JButton("CLOSE COMPLAINT");
        btnCloseComplaint.setFont(new Font("Arial", Font.BOLD, 14));
        btnCloseComplaint.setBackground(ACCENT_COLOR);
        btnCloseComplaint.setForeground(Color.WHITE);
        btnCloseComplaint.setFocusPainted(false);
        btnCloseComplaint.setBorder(new EmptyBorder(12, 20, 12, 20));
        btnCloseComplaint.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCloseComplaint.addActionListener(e -> closeSelectedComplaint());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5); 
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        JLabel lblDetailHeader = new JLabel("COMPLAINT DETAILS");
        lblDetailHeader.setFont(new Font("Arial", Font.BOLD, 18));
        lblDetailHeader.setForeground(PRIMARY_DARK);
        
        GridBagConstraints headerGbc = new GridBagConstraints();
        headerGbc.gridx = 0; headerGbc.gridy = 0; 
        headerGbc.gridwidth = 4; 
        headerGbc.insets = new Insets(0, 0, 20, 0);
        headerGbc.anchor = GridBagConstraints.CENTER;
        detailPanel.add(lblDetailHeader, headerGbc);

        int row = 1;

        addLabeledField2Col(detailPanel, gbc, row++, "Title:", txtTitle, "Closed At:", txtClosedAt);
        addLabeledField2Col(detailPanel, gbc, row++, "Status:", txtStatus, "Customer ID:", txtCustomerId);
        addLabeledField2Col(detailPanel, gbc, row++, "Priority:", txtPriority, "Product ID:", txtProductId);
        addLabeledField2Col(detailPanel, gbc, row++, "Created At:", txtCreatedAt, "Category:", txtCategory);

        // Description Label
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblDesc = new JLabel("Description:");
        lblDesc.setFont(new Font("Arial", Font.BOLD, 12));
        detailPanel.add(lblDesc, gbc);

        // Description Area
        gbc.gridx = 1; gbc.gridy = row;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0; // Give space to text area
        detailPanel.add(scrollDescription, gbc);
        row++;

        // Close Button area
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 0, 0);
        detailPanel.add(btnCloseComplaint, gbc);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTable, detailPanel);
        splitPane.setResizeWeight(0.55); // Table takes 55% width
        splitPane.setDividerSize(4);
        splitPane.setBorder(null);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
    }


    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(PRIMARY_DARK);
        header.setForeground(TABLE_HEADER_TXT);
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_COLOR));
        
        table.setSelectionBackground(new Color(255, 224, 178)); 
        table.setSelectionForeground(Color.BLACK);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Status
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Priority
    }

    private JButton createHeaderButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(new Color(255, 255, 255));
        btn.setForeground(PRIMARY_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(5, 15, 5, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextField createFixedField(Dimension size) {
        JTextField f = new JTextField();
        f.setEditable(false);
        f.setPreferredSize(size);
        f.setMinimumSize(size);
        f.setMaximumSize(size);
        f.setBackground(Color.WHITE);
        f.setForeground(TEXT_COLOR);
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        // Elegant flat border
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return f;
    }

    private void addLabeledField2Col(JPanel panel, GridBagConstraints gbc, int row,
                                     String leftLabel, JComponent leftField,
                                     String rightLabel, JComponent rightField) {

        gbc.weighty = 0.0;
        
        // Left Label
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel l1 = new JLabel(leftLabel);
        l1.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(l1, gbc);

        // Left Field
        gbc.gridx = 1; gbc.gridy = row;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(leftField, gbc);

        // Right Label
        gbc.gridx = 2; gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel l2 = new JLabel(rightLabel);
        l2.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(l2, gbc);

        // Right Field
        gbc.gridx = 3; gbc.gridy = row;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(rightField, gbc);
    }


    private void loadComplaints() {
        try (Connection conn = DbConfig.getConnection()) {

            String filter = (String) cmbStatusFilter.getSelectedItem();

            StringBuilder sql = new StringBuilder(
                    "SELECT c.ComplaintID, t.Title, s.Name AS StatusName, p.Name AS PriorityName, t.CreatedAt AS CreatedAt " +
                    "FROM Complaints c " +
                    "JOIN ComplaintTexts t ON t.ComplaintID = c.ComplaintID " +
                    "JOIN ComplaintStatus s ON s.ComplaintStatusID = c.ComplaintStatusID " +
                    "JOIN ComplaintPriority p ON p.ComplaintPriorityID = c.ComplaintPriorityID " +
                    "WHERE c.AssignedStaffID = ? "
            );

            if ("Active".equals(filter)) sql.append("AND c.IsActive = 1 ");
            else if ("Close".equals(filter)) sql.append("AND c.IsActive = 0 ");

            sql.append("ORDER BY t.CreatedAt DESC");

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

            selectedComplaintId = null;
            btnCloseComplaint.setEnabled(false);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error while loading complaints:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadComplaintDetail(int complaintId) {
        String sql = """
            SELECT 
                t.Title, t.Description,
                s.Name AS StatusName,
                p.Name AS PriorityName,
                t.CreatedAt, t.ClosedAt,
                c.IsActive,
                c.CustomerID,
                c.ProductID,
                cat.Name AS CategoryName
            FROM Complaints c
            JOIN ComplaintTexts t ON c.ComplaintID = t.ComplaintID
            JOIN ComplaintStatus s ON s.ComplaintStatusID = c.ComplaintStatusID
            JOIN ComplaintPriority p ON p.ComplaintPriorityID = c.ComplaintPriorityID
            LEFT JOIN ComplaintCategory cat ON cat.ComplaintCategoryID = c.ComplaintCategoryID
            WHERE c.ComplaintID = ?
            """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, complaintId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    selectedComplaintId = complaintId;
                    selectedComplaintIsActive = rs.getBoolean("IsActive");

                    txtTitle.setText(rs.getString("Title"));
                    txtDescription.setText(rs.getString("Description"));
                    txtStatus.setText(rs.getString("StatusName"));
                    txtPriority.setText(rs.getString("PriorityName"));

                    Timestamp created = rs.getTimestamp("CreatedAt");
                    txtCreatedAt.setText(created == null ? "" : created.toString());

                    Timestamp closed = rs.getTimestamp("ClosedAt");
                    txtClosedAt.setText(closed == null ? "" : closed.toString());

                    int custId = rs.getInt("CustomerID");
                    txtCustomerId.setText(rs.wasNull() ? "" : String.valueOf(custId));

                    int prodId = rs.getInt("ProductID");
                    txtProductId.setText(rs.wasNull() ? "" : String.valueOf(prodId));

                    txtCategory.setText(rs.getString("CategoryName"));

                    btnCloseComplaint.setEnabled(selectedComplaintIsActive);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error while loading complaint detail:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

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

        String selectOldStatusSql = "SELECT ComplaintStatusID FROM Complaints WHERE ComplaintID = ?";

        String updateComplaintSql = """
            UPDATE Complaints
            SET IsActive = 0,
                ComplaintStatusID = ?
            WHERE ComplaintID = ?
            """;

        String updateTextSql = """
            UPDATE ComplaintTexts
            SET ClosedAt = SYSDATETIME(),
                LastUpdatedAt = SYSDATETIME()
            WHERE ComplaintID = ?
            """;

        String insertActionSql = """
            INSERT INTO ComplaintActions
                (ComplaintID, OldStatusID, NewStatusID, PerformedByID, ActionType, ActionDate)
            VALUES
                (?, ?, ?, ?, ?, SYSDATETIME())
            """;

        try (Connection conn = DbConfig.getConnection()) {
            try {
                conn.setAutoCommit(false);

                int oldStatusId;

                try (PreparedStatement psSelect = conn.prepareStatement(selectOldStatusSql)) {
                    psSelect.setInt(1, complaintId);
                    try (ResultSet rs = psSelect.executeQuery()) {
                        if (!rs.next()) {
                            JOptionPane.showMessageDialog(this,
                                    "Complaint not found.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            conn.rollback();
                            return;
                        }
                        oldStatusId = rs.getInt("ComplaintStatusID");
                    }
                }

                try (PreparedStatement psUpdateComplaints = conn.prepareStatement(updateComplaintSql)) {
                    psUpdateComplaints.setInt(1, CLOSED_STATUS_ID);
                    psUpdateComplaints.setInt(2, complaintId);
                    psUpdateComplaints.executeUpdate();
                }

                try (PreparedStatement psUpdateText = conn.prepareStatement(updateTextSql)) {
                    psUpdateText.setInt(1, complaintId);
                    psUpdateText.executeUpdate();
                }

                try (PreparedStatement psInsert = conn.prepareStatement(insertActionSql)) {
                    psInsert.setInt(1, complaintId);
                    psInsert.setInt(2, oldStatusId);
                    psInsert.setInt(3, CLOSED_STATUS_ID);
                    psInsert.setInt(4, staff.getStaffId());
                    psInsert.setString(5, "Close");
                    psInsert.executeUpdate();
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Complaint has been closed.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);

                loadComplaints();
                loadComplaintDetail(complaintId);

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error while closing complaint:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}