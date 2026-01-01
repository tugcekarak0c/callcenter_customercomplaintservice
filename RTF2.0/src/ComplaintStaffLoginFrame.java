import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * ComplaintStaffLoginFrame
 *
 * Provides the login screen for staff members
 * who access the complaint management system.
 * Validates staff credentials and redirects
 * successful logins to the staff dashboard.
 *
 * Tables used:
 *  - StaffLogins (staff login credentials)
 *  - Staff (staff account information)
 */


public class ComplaintStaffLoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JButton btnLogin;
    private JButton btnBack;
    private JLabel lblTitle;

    private final Color COLOR_BG_CREAM = new Color(255, 248, 225);
    private final Color COLOR_TEXT_BROWN = new Color(93, 64, 55);  
    private final Color COLOR_BTN_WOOD = new Color(121, 85, 72);   
    private final Color COLOR_BTN_GREEN = new Color(56, 142, 60);  
    private final Color COLOR_BORDER = new Color(93, 64, 55);      

    public ComplaintStaffLoginFrame() {
        setTitle("Staff Login");
        setSize(450, 320); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        getContentPane().setBackground(COLOR_BG_CREAM);
        setLayout(new BorderLayout(10, 10));

        initComponents();
    }

    private void initComponents() {
        lblTitle = new JLabel("Staff Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 24)); // Rustic serif font
        lblTitle.setForeground(COLOR_TEXT_BROWN);
        lblTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(COLOR_BG_CREAM); // Match background
        
        centerPanel.setBorder(new CompoundBorder(
                new EmptyBorder(0, 20, 0, 20),
                new LineBorder(COLOR_BORDER, 2, true) // Rounded brown border
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblUser.setForeground(COLOR_TEXT_BROWN);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPass.setForeground(COLOR_TEXT_BROWN);

        txtUsername = new JTextField(20);
        styleInput(txtUsername);

        txtPassword = new JPasswordField(20);
        styleInput(txtPassword);

        chkShowPassword = new JCheckBox("Show password");
        chkShowPassword.setBackground(COLOR_BG_CREAM);
        chkShowPassword.setForeground(COLOR_TEXT_BROWN);
        chkShowPassword.setFont(new Font("SansSerif", Font.PLAIN, 12));
        chkShowPassword.setFocusPainted(false);

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

        gbc.gridx = 1; gbc.gridy = row;
        centerPanel.add(chkShowPassword, gbc);
        row++;

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(COLOR_BG_CREAM);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 15, 15));

        btnBack = new JButton("Back");
        styleButton(btnBack);

        btnLogin = new JButton("Log In");
        styleButton(btnLogin);

        bottomPanel.add(btnBack);
        bottomPanel.add(btnLogin);

        add(bottomPanel, BorderLayout.SOUTH);

        // ----- EVENTS -----

        // Show / Hide password
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0); // show
            } else {
                txtPassword.setEchoChar('●');      // hide
            }
        });

        // Back → RoleSelectionFrame
        btnBack.addActionListener(e -> {
            new RoleSelectionFrame().setVisible(true);
            dispose();
        });

        // Login
        btnLogin.addActionListener(e -> handleStaffLogin());

        // Enter = Login
        getRootPane().setDefaultButton(btnLogin);
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(COLOR_BTN_WOOD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 2),
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

    private void styleInput(JTextField txt) {
        txt.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txt.setBackground(Color.WHITE);
        txt.setForeground(Color.BLACK);
        txt.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
    }

    private void handleStaffLogin() {
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
            SELECT sl.StaffID,
                   sl.Username,
                   s.FirstName,
                   s.LastName,
                   s.Role
            FROM StaffLogins sl
            JOIN Staff s ON sl.StaffID = s.StaffID
            WHERE sl.Username = ? AND sl.PasswordHash = ?
            """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int staffId = rs.getInt("StaffID");
                    String fullName = rs.getString("FirstName") + " " + rs.getString("LastName");
                    String role = rs.getString("Role");

                    StaffUser staffUser =
                            new StaffUser(staffId, username, fullName, role);

                    ComplaintListFrame frame = new ComplaintListFrame(staffUser);
                    frame.setVisible(true);
                    dispose();

                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Invalid staff username or password.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error while checking staff login:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        // cleaner look and feel
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() ->
                new ComplaintStaffLoginFrame().setVisible(true)
        );
    }
}