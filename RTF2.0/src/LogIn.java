import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

/**
 * LogIn
 *
 * Provides the main login interface of the application.
 * Allows users to authenticate and redirects them
 * to the appropriate dashboard based on their role.
 *
 * Tables used:
 *  - StaffLogins (staff login credentials)
 *  - Staff (staff information)
 *  - CustomerUsers (customer account information)
 *  - Customers (customer profile information)
 *
 * Acts as the entry point for both staff and customer users.
 */


public class LogIn extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private char defaultEchoChar;

    private JButton btnLogin;
    private JButton btnBack;
    private JLabel lblTitle;
    private JLabel lblChangePassword;

    private final Color COLOR_BG_CREAM = new Color(255, 248, 225);
    private final Color COLOR_TEXT_BROWN = new Color(93, 64, 55);
    private final Color COLOR_BTN_WOOD = new Color(121, 85, 72);
    private final Color COLOR_BTN_GREEN = new Color(56, 142, 60);
    private final Color COLOR_LINK = new Color(21, 101, 192);

    public LogIn() {
        setTitle("Customer Login");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(460, 340); 
        setLocationRelativeTo(null);
        
        getContentPane().setBackground(COLOR_BG_CREAM);
        setLayout(new BorderLayout(10, 10));

        initComponents();
    }

    private void initComponents() {
        //  TITLE 
        lblTitle = new JLabel("Customer Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 24)); 
        lblTitle.setForeground(COLOR_TEXT_BROWN);
        lblTitle.setBorder(new EmptyBorder(15, 0, 5, 0));
        add(lblTitle, BorderLayout.NORTH);

        //  CENTER (username + password + show + link)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(COLOR_BG_CREAM);
        
        centerPanel.setBorder(new CompoundBorder(
                new EmptyBorder(0, 20, 0, 20),
                new LineBorder(COLOR_TEXT_BROWN, 1, true)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblUser = new JLabel("Username:");
        styleLabel(lblUser);

        JLabel lblPass = new JLabel("Password:");
        styleLabel(lblPass);

        txtUsername = new JTextField(20);
        styleInput(txtUsername);

        txtPassword = new JPasswordField(20);
        styleInput(txtPassword);

        defaultEchoChar = txtPassword.getEchoChar();
        
        chkShowPassword = new JCheckBox("Show password");
        chkShowPassword.setBackground(COLOR_BG_CREAM);
        chkShowPassword.setForeground(COLOR_TEXT_BROWN);
        chkShowPassword.setFont(new Font("SansSerif", Font.PLAIN, 12));
        chkShowPassword.setFocusPainted(false);

        lblChangePassword = new JLabel("<HTML><U>Do you want to change your password?</U></HTML>");
        lblChangePassword.setForeground(COLOR_LINK);
        lblChangePassword.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblChangePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        centerPanel.add(lblUser, gbc);
        gbc.gridx = 1;
        centerPanel.add(txtUsername, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        centerPanel.add(lblPass, gbc);
        gbc.gridx = 1;
        centerPanel.add(txtPassword, gbc);
        row++;

        // Show password checkbox
        gbc.gridx = 1; gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(chkShowPassword, gbc);
        row++;

        // Change password link
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 10, 5, 10); 
        centerPanel.add(lblChangePassword, gbc);
        row++;

        add(centerPanel, BorderLayout.CENTER);

        // BOTTOM BUTTONS 
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(COLOR_BG_CREAM);
        bottomPanel.setBorder(new EmptyBorder(5, 10, 15, 15));

        btnBack = new JButton("Back");
        styleButton(btnBack);

        btnLogin = new JButton("Log In");
        styleButton(btnLogin);

        bottomPanel.add(btnBack);
        bottomPanel.add(btnLogin);

        add(bottomPanel, BorderLayout.SOUTH);

        //  EVENTS

        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar(defaultEchoChar);
            }
        });

        btnBack.addActionListener(e -> {
            new CustomerLogOrSignFrame().setVisible(true);
            dispose();
        });

        btnLogin.addActionListener(e -> handleLogin());
        getRootPane().setDefaultButton(btnLogin);

        lblChangePassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String username = txtUsername.getText().trim();
                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            LogIn.this,
                            "Please enter your username first.",
                            "Info",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }
                CustomerChangePasswordDialog dialog =
                        new CustomerChangePasswordDialog(LogIn.this, username);
                dialog.setVisible(true);
            }
        });
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

    private void styleLabel(JLabel lbl) {
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(COLOR_TEXT_BROWN);
    }

    private void styleInput(JTextField txt) {
        txt.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txt.setBackground(Color.WHITE);
        txt.setForeground(Color.BLACK);
        txt.setBorder(new CompoundBorder(
                new LineBorder(COLOR_TEXT_BROWN, 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both username and password.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String sql = """
                SELECT cu.CustomerID,
                       cu.Username,
                       c.FirstName,
                       c.LastName
                FROM CustomerUsers cu
                JOIN Customers c ON cu.CustomerID = c.CustomerID
                WHERE cu.Username = ? AND cu.PasswordHash = ?
                """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int customerId = rs.getInt("CustomerID");
                    String fullName = rs.getString("FirstName") + " " + rs.getString("LastName");

                    JOptionPane.showMessageDialog(
                            this,
                            "Login successful.\nWelcome, " + fullName + " (ID: " + customerId + ")",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    CustomerDashboard dashboard = new CustomerDashboard(customerId);
                    dashboard.setVisible(true);
                    dispose();

                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Invalid username or password.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error while checking login:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        // custom colors
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {}

        SwingUtilities.invokeLater(() -> new LogIn().setVisible(true));
    }
}