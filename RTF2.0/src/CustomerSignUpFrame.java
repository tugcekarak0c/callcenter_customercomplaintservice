
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

/**
 * CustomerSignUpFrame
 *
 * Provides the user interface for customer registration.
 * Allows new customers to create an account by entering
 * required personal and login information.
 *
 * Tables used:
 *  - CustomerUsers (customer account and login information)
 *  - Customers (customer basic profile information)
 */



public class CustomerSignUpFrame extends JFrame {

    // UI 
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

    private JButton btnRegister;
    private JButton btnCancel;

    // THEME 
    private final Color COLOR_BG_CREAM = new Color(255, 248, 225); 
    private final Color COLOR_TEXT_BROWN = new Color(93, 64, 55); 
    private final Color COLOR_BTN_WOOD = new Color(121, 85, 72);   
    private final Color COLOR_BTN_GREEN = new Color(56, 142, 60);  
    private final Color COLOR_INPUT_BG = Color.WHITE;

    public CustomerSignUpFrame() {
        setTitle("Customer Sign Up");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 650); 
        setLocationRelativeTo(null);

        getContentPane().setBackground(COLOR_BG_CREAM);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(COLOR_BG_CREAM);
        mainPanel.setBorder(new CompoundBorder(
                new EmptyBorder(15, 15, 15, 15),
                new LineBorder(COLOR_TEXT_BROWN, 1) 
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6); 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Personal info 
        JLabel lblSectionPersonal = new JLabel("Personal Information");
        styleSectionHeader(lblSectionPersonal);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(lblSectionPersonal, gbc);
        row++;

        gbc.gridwidth = 1;

        // First name
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(createLabel("First name:"), gbc);
        txtFirstName = new JTextField();
        styleInput(txtFirstName);
        gbc.gridx = 1;
        mainPanel.add(txtFirstName, gbc);
        row++;

        // Last name
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(createLabel("Last name:"), gbc);
        txtLastName = new JTextField();
        styleInput(txtLastName);
        gbc.gridx = 1;
        mainPanel.add(txtLastName, gbc);
        row++;

        // Gender
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(createLabel("Gender:"), gbc);
        cmbGender = new JComboBox<>(new String[]{"M", "F"});
        cmbGender.setBackground(Color.WHITE);
        cmbGender.setForeground(COLOR_TEXT_BROWN);
        cmbGender.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 1;
        mainPanel.add(cmbGender, gbc);
        row++;

        // Contact info 
        JLabel lblSectionContact = new JLabel("Contact Information");
        styleSectionHeader(lblSectionContact);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(lblSectionContact, gbc);
        row++;
        gbc.gridwidth = 1;

        // Email
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(createLabel("Email:"), gbc);
        txtEmail = new JTextField();
        styleInput(txtEmail);
        gbc.gridx = 1;
        mainPanel.add(txtEmail, gbc);
        row++;

        // Phone
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(createLabel("Phone:"), gbc);
        txtPhone = new JTextField();
        styleInput(txtPhone);
        gbc.gridx = 1;
        mainPanel.add(txtPhone, gbc);
        row++;

        //  Address 
        JLabel lblSectionAddress = new JLabel("Address");
        styleSectionHeader(lblSectionAddress);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(lblSectionAddress, gbc);
        row++;
        gbc.gridwidth = 1;

        // Country
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(createLabel("Country:"), gbc);
        txtCountry = new JTextField("Turkey");
        styleInput(txtCountry);
        gbc.gridx = 1;
        mainPanel.add(txtCountry, gbc);
        row++;

        // City
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(createLabel("City:"), gbc);
        txtCity = new JTextField();
        styleInput(txtCity);
        gbc.gridx = 1;
        mainPanel.add(txtCity, gbc);
        row++;

        // Address line
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(createLabel("Address line:"), gbc);
        txtAddressLine = new JTextField();
        styleInput(txtAddressLine);
        gbc.gridx = 1;
        mainPanel.add(txtAddressLine, gbc);
        row++;

        // Postal code
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(createLabel("Postal code:"), gbc);
        txtPostalCode = new JTextField();
        styleInput(txtPostalCode);
        gbc.gridx = 1;
        mainPanel.add(txtPostalCode, gbc);
        row++;

        //  Login info
        JLabel lblSectionLogin = new JLabel("Login Information");
        styleSectionHeader(lblSectionLogin);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(lblSectionLogin, gbc);
        row++;
        gbc.gridwidth = 1;

        // Username
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(createLabel("Username:"), gbc);
        txtUsername = new JTextField();
        styleInput(txtUsername);
        gbc.gridx = 1;
        mainPanel.add(txtUsername, gbc);
        row++;

        // Password
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(createLabel("Password:"), gbc);
        txtPassword = new JPasswordField();
        styleInput(txtPassword);
        gbc.gridx = 1;
        mainPanel.add(txtPassword, gbc);
        row++;

        // Confirm password
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(createLabel("Confirm password:"), gbc);
        txtPasswordConfirm = new JPasswordField();
        styleInput(txtPasswordConfirm);
        gbc.gridx = 1;
        mainPanel.add(txtPasswordConfirm, gbc);
        row++;

        // Buttons 
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(COLOR_BG_CREAM); 

        btnRegister = new JButton("Register");
        styleButton(btnRegister);

        btnCancel = new JButton("Cancel");
        styleButton(btnCancel);

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnCancel);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(15, 6, 6, 6); 
        mainPanel.add(buttonPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(COLOR_BG_CREAM);
        add(scrollPane, BorderLayout.CENTER);

        // Action listeners 
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerCustomer();
            }
        });

        btnCancel.addActionListener(e -> {
            new CustomerLogOrSignFrame().setVisible(true);
            SwingUtilities.getWindowAncestor(btnCancel).dispose();
        });

    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(COLOR_TEXT_BROWN);
        return lbl;
    }

    private void styleSectionHeader(JLabel lbl) {
        lbl.setFont(new Font("Georgia", Font.BOLD, 18));
        lbl.setForeground(COLOR_TEXT_BROWN);
        lbl.setBorder(new CompoundBorder(
                new EmptyBorder(10, 0, 5, 0),
                new MatteBorder(0, 0, 2, 0, COLOR_TEXT_BROWN)
        ));
    }

    private void styleInput(JTextField txt) {
        txt.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txt.setBackground(COLOR_INPUT_BG);
        txt.setForeground(Color.BLACK);
        // Boxy brown border
        txt.setBorder(new CompoundBorder(
                new LineBorder(COLOR_TEXT_BROWN, 1),
                new EmptyBorder(4, 4, 4, 4)
        ));
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(COLOR_BTN_WOOD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(COLOR_TEXT_BROWN, 2),
                new EmptyBorder(8, 20, 8, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(COLOR_BTN_GREEN);
            }

            public void mouseExited(MouseEvent evt) {
                btn.setBackground(COLOR_BTN_WOOD);
            }
        });
    }

    //  DATABASE
    /* Validates inputs and inserts into: - Customers - CustomerContactInfo - Address - CustomerUsers
     */
    private void registerCustomer() {
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();

        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        String country = txtCountry.getText().trim();
        String city = txtCity.getText().trim();
        String addressLine = txtAddressLine.getText().trim();
        String postalCode = txtPostalCode.getText().trim();

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String passwordConfirm = new String(txtPasswordConfirm.getPassword());

        //  basic validations
        if (firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "First name and last name are required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (gender == null || gender.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Gender is required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Phone number is required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username and password are required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(passwordConfirm)) {
            JOptionPane.showMessageDialog(this,
                    "Password and Confirm Password do not match.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            conn = DbConfig.getConnection();
            conn.setAutoCommit(false);

            // Username already exists?
            if (isUsernameExists(conn, username)) {
                JOptionPane.showMessageDialog(this,
                        "This username is already in use. Please choose another one.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                conn.rollback();
                return;
            }

            //Insert into Customers
            int customerId;

            String insertCustomerSql = """
                    INSERT INTO Customers (FirstName, LastName, Gender, CreatedAt)
                    VALUES (?, ?, ?, SYSDATETIME())
                    """;

            try (PreparedStatement ps = conn.prepareStatement(insertCustomerSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setString(3, gender);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        customerId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated CustomerID.");
                    }
                }
            }

            // Insert into CustomerContactInfo
            String insertContactSql = """
                    INSERT INTO CustomerContactInfo (CustomerID, Email, PhoneNumber)
                    VALUES (?, ?, ?)
                    """;

            try (PreparedStatement ps = conn.prepareStatement(insertContactSql)) {
                ps.setInt(1, customerId);
                if (email.isEmpty()) {
                    ps.setNull(2, Types.NVARCHAR);
                } else {
                    ps.setString(2, email);
                }
                ps.setString(3, phone);
                ps.executeUpdate();
            }

            //  Insert into Address
            String insertAddressSql = """
                    INSERT INTO Address (CustomerID, City, Country, AddressLine, PostalCode)
                    VALUES (?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement ps = conn.prepareStatement(insertAddressSql)) {
                ps.setInt(1, customerId);
                ps.setString(2, city.isEmpty() ? null : city);
                ps.setString(3, country.isEmpty() ? null : country);
                ps.setString(4, addressLine.isEmpty() ? null : addressLine);
                ps.setString(5, postalCode.isEmpty() ? null : postalCode);
                ps.executeUpdate();
            }

            //  Insert into CustomerUsers
            //(CustomerID, Username, PasswordHash, Email)
            String insertUserSql = """
                    INSERT INTO CustomerUsers (CustomerID, Username, PasswordHash, Email)
                    VALUES (?, ?, ?, ?)
                    """;

            try (PreparedStatement ps = conn.prepareStatement(insertUserSql)) {
                ps.setInt(1, customerId);
                ps.setString(2, username);
                ps.setString(3, password); // in real life: hash it!
                if (email.isEmpty()) {
                    ps.setNull(4, Types.NVARCHAR);
                } else {
                    ps.setString(4, email);
                }
                ps.executeUpdate();
            }

            conn.commit();

            JOptionPane.showMessageDialog(this,
                    "Registration successful.\nYour CustomerID: " + customerId,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose(); // close sign-up window
            

        } catch (Exception ex) {
            ex.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            JOptionPane.showMessageDialog(this,
                    "Error while creating customer:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*CustomerUsers already exists?*/
    private boolean isUsernameExists(Connection conn, String username) throws SQLException {
        String sql = "SELECT COUNT(*) AS Cnt FROM CustomerUsers WHERE Username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Cnt") > 0;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        // custom colors 
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            new CustomerSignUpFrame().setVisible(true);
        });
    }
}
