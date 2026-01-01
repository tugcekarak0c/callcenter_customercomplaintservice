import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;   
import java.sql.*;

/**
 * NewComplaintScreen
 *
 * Provides the interface for customers to create and submit
 * a new complaint. Allows entering a title and description,
 * selecting a complaint category, and optionally linking
 * the complaint to a product.
 *
 * On submission, the complaint is saved with default status
 * and priority values and is automatically assigned to
 * an available staff member.
 *
 * Tables used:
 *  - Complaints (main complaint record)
 *  - ComplaintTexts (complaint title and description)
 *  - ComplaintCategory (complaint categories)
 *  - ComplaintPriority (default complaint priority)
 *  - Products (optional product lookup)
 *  - Staff (random staff assignment)
 */


public class NewComplaintScreen extends JFrame {

    private final int customerId;

    private final Color PRIMARY_DARK = new Color(62, 39, 35);    
    private final Color ACCENT_COLOR = new Color(191, 54, 12);   
    private final Color BG_COLOR = new Color(245, 245, 245);     
    private final Color TEXT_COLOR = new Color(33, 33, 33);      

    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JComboBox<ComboItem> cmbCategory;
    private JTextField txtProductId;   

    private Integer midPriorityId = null;

    private static final int OPEN_STATUS_ID = 1;
    private static final int DEFAULT_SOURCE_ID = 1;

    private static class ComboItem {
        final int id;
        final String text;
        ComboItem(int id, String text) {
            this.id = id;
            this.text = text;
        }
        @Override
        public String toString() { return text; }
    }

    public NewComplaintScreen(int customerId) {
        this.customerId = customerId;

        setTitle("Create New Complaint");
        getContentPane().setBackground(BG_COLOR);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        loadComboData();
        
        pack(); // Resize to fit components
        setLocationRelativeTo(null);
        setSize(550, 600); 
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        //  HEADER
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        headerPanel.setBackground(PRIMARY_DARK);
        
        JLabel header = new JLabel("SUBMIT A COMPLAINT");
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setForeground(Color.WHITE);
        headerPanel.add(header);
        
        add(headerPanel, BorderLayout.NORTH);

        // FORM PANEL 
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        
        int row = 0;

        // Title
        gbc.gridy = row++;
        formPanel.add(createStyledLabel("Complaint Title:"), gbc);
        
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 15, 0); 
        txtTitle = createStyledTextField();
        formPanel.add(txtTitle, gbc);

        // Category
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(createStyledLabel("Category:"), gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 15, 0);
        cmbCategory = new JComboBox<>();
        cmbCategory.setBackground(Color.WHITE);
        cmbCategory.setFont(new Font("Arial", Font.PLAIN, 14));
        ((JComponent) cmbCategory.getRenderer()).setBorder(new EmptyBorder(5,5,5,5));
        formPanel.add(cmbCategory, gbc);

        // Product ID
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(createStyledLabel("Product Code (6 digits, optional):"), gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 15, 0);
        txtProductId = createStyledTextField();
        txtProductId.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) { e.consume(); return; }
                if (txtProductId.getText().length() >= 6) e.consume();
            }
        });
        formPanel.add(txtProductId, gbc);

        // Description
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(createStyledLabel("Description / Details:"), gbc);

        gbc.gridy = row++;
        gbc.weighty = 1.0; 
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 15, 0);
        
        txtDescription = new JTextArea(8, 20);
        txtDescription.setFont(new Font("Arial", Font.PLAIN, 14));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JScrollPane scroll = new JScrollPane(txtDescription);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        formPanel.add(scroll, gbc);

        add(formPanel, BorderLayout.CENTER);

        //  FOOTER BUTTONS
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JButton cancelButton = createStyledButton("Cancel", Color.LIGHT_GRAY, Color.BLACK);
        JButton submitButton = createStyledButton("SUBMIT COMPLAINT", ACCENT_COLOR, Color.WHITE);

        submitButton.addActionListener(e -> handleSubmit());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    //  UI 

    private JLabel createStyledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(TEXT_COLOR);
        return lbl;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Arial", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 8, 8, 8) // Padding inside text field
        ));
        return tf;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }


    private void loadComboData() {
        loadCategories();
        loadMidPriorityId();
    }

    private void loadCategories() {
        String sql = "SELECT ComplaintCategoryID, Name FROM ComplaintCategory ORDER BY Name";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            cmbCategory.removeAllItems();
            boolean hasAny = false;
            while (rs.next()) {
                hasAny = true;
                cmbCategory.addItem(new ComboItem(
                        rs.getInt("ComplaintCategoryID"),
                        rs.getString("Name")
                ));
            }
            if (!hasAny) {
                cmbCategory.addItem(new ComboItem(-1, "(no categories)"));
                cmbCategory.setEnabled(false);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error while loading categories:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMidPriorityId() {
        String sqlByName = "SELECT ComplaintPriorityID FROM ComplaintPriority WHERE Name = 'Mid'";
        String sqlFallback = "SELECT TOP 1 ComplaintPriorityID FROM ComplaintPriority ORDER BY Rank ASC";

        try (Connection conn = DbConfig.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(sqlByName);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    midPriorityId = rs.getInt("ComplaintPriorityID");
                    return;
                }
            }

            try (PreparedStatement ps2 = conn.prepareStatement(sqlFallback);
                 ResultSet rs2 = ps2.executeQuery()) {
                midPriorityId = rs2.next() ? rs2.getInt("ComplaintPriorityID") : null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            midPriorityId = null;
        }
    }

    // Pick random staff id 
    private Integer pickRandomStaffId(Connection conn) throws SQLException {
        String sql = "SELECT TOP 1 StaffID FROM Staff ORDER BY NEWID()";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("StaffID");
        }
        return null; //if staff= null ;
    }

    private void handleSubmit() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        String productCode = txtProductId.getText().trim();

        if (title.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!cmbCategory.isEnabled()) {
            JOptionPane.showMessageDialog(this,
                    "Complaint categories are not configured.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ComboItem catItem = (ComboItem) cmbCategory.getSelectedItem();
        if (catItem == null || catItem.id <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a valid complaint category.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (midPriorityId == null) {
            JOptionPane.showMessageDialog(this,
                    "No 'Mid' priority found in ComplaintPriority table.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ProductCode -> ProductID lookup
        Integer productIdToInsert = null;

        if (!productCode.isEmpty()) {
            if (productCode.length() != 6) {
                JOptionPane.showMessageDialog(this,
                        "Product Code must be exactly 6 digits.",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String lookupSql = "SELECT ProductID FROM Products WHERE ProductCode = ?";
            try (Connection conn = DbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(lookupSql)) {

                ps.setString(1, productCode);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        productIdToInsert = rs.getInt("ProductID");
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No product found with this product code.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error while verifying product code:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String insertComplaintSql = """
            INSERT INTO Complaints
                (CustomerID, ProductID, ComplaintCategoryID, ComplaintSourceID,
                 CallID, ComplaintStatusID, ComplaintPriorityID, AssignedStaffID, IsActive)
            VALUES
                (?, ?, ?, ?, NULL, ?, ?, ?, 1)
            """;

        String insertTextSql = """
            INSERT INTO ComplaintTexts
                (ComplaintID, Title, Description, CreatedAt, ClosedAt, LastUpdatedAt)
            VALUES
                (?, ?, ?, SYSDATETIME(), NULL, SYSDATETIME())
            """;

        try (Connection conn = DbConfig.getConnection()) {

            conn.setAutoCommit(false);

            Integer assignedStaffId = pickRandomStaffId(conn);
            if (assignedStaffId == null) {
                conn.rollback();
                JOptionPane.showMessageDialog(this,
                        "No staff found to assign this complaint.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int newComplaintId;

            //  Complaints insert
            try (PreparedStatement ps = conn.prepareStatement(
                    insertComplaintSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, customerId);

                if (productIdToInsert != null) ps.setInt(2, productIdToInsert);
                else ps.setNull(2, Types.INTEGER);

                ps.setInt(3, catItem.id);
                ps.setInt(4, DEFAULT_SOURCE_ID);
                ps.setInt(5, OPEN_STATUS_ID);
                ps.setInt(6, midPriorityId);

                // AssignedStaffID
                ps.setInt(7, assignedStaffId);

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) newComplaintId = keys.getInt(1);
                    else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this,
                                "Could not retrieve new ComplaintID.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            // ComplaintTexts insert
            try (PreparedStatement psText = conn.prepareStatement(insertTextSql)) {
                psText.setInt(1, newComplaintId);
                psText.setString(2, title);
                psText.setString(3, description);
                psText.executeUpdate();
            }

            conn.commit();

            JOptionPane.showMessageDialog(this,
                    "Complaint submitted successfully!\nComplaintID: " + newComplaintId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error while submitting complaint:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        NewComplaintScreen nc = new NewComplaintScreen(1);
        nc.setVisible(true);
    }
}