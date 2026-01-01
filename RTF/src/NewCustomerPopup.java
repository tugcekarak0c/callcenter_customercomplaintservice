

import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * NewCustomerPopup
 *
 * Opens as a modal popup from CallProcessingFrame when a phone number
 * has no existing customer record.
 *
 * Uses tables:
 *  - Customers
 *  - CustomerContactInfo
 *  - Address
 *  - CustomerUsers
 */
public class NewCustomerPopup extends JDialog {

    // UI fields
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JComboBox<String> cmbGender;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtCountry;
    private JTextField txtCity;
    private JTextField txtAddressLine;
    private JTextField txtPostalCode;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtPasswordConfirm;

    private JButton btnSave;
    private JButton btnCancel;

    // Result values to pass back to caller (CallProcessingFrame)
    private Integer createdCustomerId = null;
    private String createdCustomerName = null;
    private String createdUsername = null;

    /**
     * @param owner       parent frame (CallProcessingFrame)
     * @param phoneNumber phone that was searched in CallProcessingFrame
     */
    public NewCustomerPopup(Frame owner, String phoneNumber) {
        super(owner, "New Customer Registration", true); // modal = true

        initComponents();

        if (phoneNumber != null && !phoneNumber.isBlank()) {
            txtPhone.setText(phoneNumber.trim());
        }

        pack();
        setLocationRelativeTo(owner);
    }

    public Integer getCreatedCustomerId() {
        return createdCustomerId;
    }

    public String getCreatedCustomerName() {
        return createdCustomerName;
    }

    public String getCreatedUsername() {
        return createdUsername;
    }

    // --------------------------------------------------
    // UI
    // --------------------------------------------------
    private void initComponents() {
        txtFirstName = new JTextField(18);
        txtLastName = new JTextField(18);
        cmbGender = new JComboBox<>(new String[]{"Female (F)", "Male (M)", "Other (O)"});
        txtEmail = new JTextField(22);
        txtPhone = new JTextField(18);
        txtCountry = new JTextField(18);
        txtCity = new JTextField(18);
        txtAddressLine = new JTextField(22);
        txtPostalCode = new JTextField(10);

        txtUsername = new JTextField(18);
        txtPassword = new JPasswordField(18);
        txtPasswordConfirm = new JPasswordField(18);

        btnSave = new JButton("Save Customer");
        btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> onCancel());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        int row = 0;

        // --- Basic customer info ---
        gc.gridx = 0; gc.gridy = row;
        formPanel.add(new JLabel("First Name:"), gc);
        gc.gridx = 1;
        formPanel.add(txtFirstName, gc);
        row++;

        gc.gridx = 0; gc.gridy = row;
        formPanel.add(new JLabel("Last Name:"), gc);
        gc.gridx = 1;
        formPanel.add(txtLastName, gc);
        row++;

        gc.gridx = 0; gc.gridy = row;
        formPanel.add(new JLabel("Gender:"), gc);
        gc.gridx = 1;
        formPanel.add(cmbGender, gc);
        row++;

        gc.gridx = 0; gc.gridy = row;
        formPanel.add(new JLabel("Email:"), gc);
        gc.gridx = 1;
        formPanel.add(txtEmail, gc);
        row++;

        gc.gridx = 0; gc.gridy = row;
        formPanel.add(new JLabel("Phone:"), gc);
        gc.gridx = 1;
        formPanel.add(txtPhone, gc);
        row++;

        // --- Address info ---
        gc.gridx = 0; gc.gridy = row;
        formPanel.add(new JLabel("Country:"), gc);
        gc.gridx = 1;
        formPanel.add(txtCountry, gc);
        row++;

        gc.gridx = 0; gc.gridy = row;
        formPanel.add(new JLabel("City:"), gc);
        gc.gridx = 1;
        formPanel.add(txtCity, gc);
        row++;

        gc.gridx = 0; gc.gridy = row;
        formPanel.add(new JLabel("Address Line:"), gc);
        gc.gridx = 1;
        formPanel.add(txtAddressLine, gc);
        row++;

        gc.gridx = 0; gc.gridy = row;
        formPanel.add(new JLabel("Postal Code:"), gc);
        gc.gridx = 1;
        formPanel.add(txtPostalCode, gc);
        row++;

        // --- Login info (CustomerUsers) ---
        gc.gridx = 0; gc.gridy = row;
        formPanel.add(new JLabel("Username:"), gc);
        gc.gridx = 1;
        formPanel.add(txtUsername, gc);
        row++;

        gc.gridx = 0; gc.gridy = row;
        formPanel.add(new JLabel("Password:"), gc);
        gc.gridx = 1;
        formPanel.add(txtPassword, gc);
        row++;

        gc.gridx = 0; gc.gridy = row;
        formPanel.add(new JLabel("Password (again):"), gc);
        gc.gridx = 1;
        formPanel.add(txtPasswordConfirm, gc);
        row++;

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);

        getContentPane().setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
        getContentPane().add(formPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    // --------------------------------------------------
    // Validation
    // --------------------------------------------------
    private boolean validateInputs() {
        String firstName = txtFirstName.getText().trim();
        String lastName  = txtLastName.getText().trim();
        String email     = txtEmail.getText().trim();
        String phone     = txtPhone.getText().trim();
        String username  = txtUsername.getText().trim();
        String password  = new String(txtPassword.getPassword());
        String password2 = new String(txtPasswordConfirm.getPassword());

        if (firstName.isEmpty() || lastName.isEmpty() ||
                email.isEmpty() || phone.isEmpty() ||
                username.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "First name, last name, email, phone, username and password are required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Simple email validation
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Phone format – same style as CallProcessingFrame
        String phonePattern = "^0\\(\\d{3}\\) \\d{3} \\d{2} \\d{2}$";
        if (!phone.matches(phonePattern)) {
            JOptionPane.showMessageDialog(this,
                    "Phone format is invalid.\nRequired: 0(***) *** ** **\nExample: 0(555) 123 45 67",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Password match
        if (!password.equals(password2)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Username length basic check
        if (username.length() < 4) {
            JOptionPane.showMessageDialog(this,
                    "Username must be at least 4 characters.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Check username uniqueness in CustomerUsers
        if (!isUsernameAvailable(username)) {
            JOptionPane.showMessageDialog(this,
                    "This username is already in use. Please choose another one.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean isUsernameAvailable(String username) {
        String sql = "SELECT COUNT(*) AS Cnt FROM CustomerUsers WHERE Username = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cnt = rs.getInt("Cnt");
                    return cnt == 0;
                }
            }
        } catch (SQLException ex) {
            // Eğer hata olursa, username'i kullanılabilir kabul etmek riskli; burada false döndürmek daha güvenli.
            ex.printStackTrace();
            return false;
        }
        return false;
    }

    // --------------------------------------------------
    // Save logic (DB inserts)
    // --------------------------------------------------
    /**
     * Transaction:
     *  1) INSERT INTO Customers
     *  2) INSERT INTO CustomerContactInfo
     *  3) INSERT INTO Address
     *  4) INSERT INTO CustomerUsers (username + password)
     */
    private void onSave() {
        if (!validateInputs()) {
            return;
        }

        String firstName = txtFirstName.getText().trim();
        String lastName  = txtLastName.getText().trim();
        String email     = txtEmail.getText().trim();
        String phone     = txtPhone.getText().trim();
        String country   = txtCountry.getText().trim();
        String city      = txtCity.getText().trim();
        String address   = txtAddressLine.getText().trim();
        String postal    = txtPostalCode.getText().trim();
        String username  = txtUsername.getText().trim();
        String password  = new String(txtPassword.getPassword());

        // Gender mapping to CHAR(1) in Customers
        String genderStr = (String) cmbGender.getSelectedItem();
        char gender = 'O';
        if (genderStr != null) {
            if (genderStr.contains("(F)")) gender = 'F';
            else if (genderStr.contains("(M)")) gender = 'M';
            else gender = 'O';
        }

        Connection conn = null;
        PreparedStatement psCustomer = null;
        PreparedStatement psContact  = null;
        PreparedStatement psAddress  = null;
        PreparedStatement psUser     = null;
        ResultSet generatedKeys = null;

        try {
            conn = DbConfig.getConnection();
            conn.setAutoCommit(false);

            // 1) Customers
            String sqlCustomer = """
                INSERT INTO Customers (FirstName, LastName, Gender, CreatedAt)
                VALUES (?, ?, ?, SYSDATETIME())
                """;
            psCustomer = conn.prepareStatement(sqlCustomer, Statement.RETURN_GENERATED_KEYS);
            psCustomer.setString(1, firstName);
            psCustomer.setString(2, lastName);
            psCustomer.setString(3, String.valueOf(gender));
            psCustomer.executeUpdate();

            generatedKeys = psCustomer.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Could not obtain generated CustomerID.");
            }
            int customerId = generatedKeys.getInt(1);

            // 2) CustomerContactInfo
            String sqlContact = """
                INSERT INTO CustomerContactInfo (CustomerID, Email, PhoneNumber)
                VALUES (?, ?, ?)
                """;
            psContact = conn.prepareStatement(sqlContact);
            psContact.setInt(1, customerId);
            psContact.setString(2, email);
            psContact.setString(3, phone);
            psContact.executeUpdate();

            // 3) Address
            String sqlAddress = """
                INSERT INTO Address (CustomerID, City, Country, AddressLine, PostalCode)
                VALUES (?, ?, ?, ?, ?)
                """;
            psAddress = conn.prepareStatement(sqlAddress);
            psAddress.setInt(1, customerId);
            psAddress.setString(2, city.isEmpty() ? null : city);
            psAddress.setString(3, country.isEmpty() ? null : country);
            psAddress.setString(4, address.isEmpty() ? null : address);
            psAddress.setString(5, postal.isEmpty() ? null : postal);
            psAddress.executeUpdate();

            // 4) CustomerUsers (username + password)
            String sqlUser = """
                INSERT INTO CustomerUsers (CustomerID, Username, PasswordHash, Email)
                VALUES (?, ?, ?, ?)
                """;
            psUser = conn.prepareStatement(sqlUser);
            psUser.setInt(1, customerId);
            psUser.setString(2, username);
            // Şimdilik basit: PasswordHash kolonuna düz metin yazıyoruz.
            // İstersen daha sonra SHA-256 vs. ekleyebilirsin.
            psUser.setString(3, password);
            psUser.setString(4, email);
            psUser.executeUpdate();

            conn.commit();

            createdCustomerId = customerId;
            createdCustomerName = firstName + " " + lastName;
            createdUsername = username;

            JOptionPane.showMessageDialog(this,
                    "Customer created successfully.\n" +
                    "CustomerID: " + customerId + "\nUsername: " + username,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error while creating customer:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (generatedKeys != null) generatedKeys.close(); } catch (Exception ignore) {}
            try { if (psUser != null) psUser.close(); } catch (Exception ignore) {}
            try { if (psAddress != null) psAddress.close(); } catch (Exception ignore) {}
            try { if (psContact != null) psContact.close(); } catch (Exception ignore) {}
            try { if (psCustomer != null) psCustomer.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    private void onCancel() {
        createdCustomerId = null;
        createdCustomerName = null;
        createdUsername = null;
        dispose();
    }
}
