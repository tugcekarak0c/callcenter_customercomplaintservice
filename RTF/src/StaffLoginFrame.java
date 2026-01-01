import javax.swing.*;
import java.awt.*;

public class StaffLoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblStatus;
    private JButton btnLogin;

    private StaffLoginService loginService = new StaffLoginService();

    public StaffLoginFrame() {
        super("Staff Login");
        initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 220);
        setLocationRelativeTo(null); // ekran ortası
    }

    private void initComponents() {
        JLabel lblUser = new JLabel("Username:");
        JLabel lblPass = new JLabel("Password:");

        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        btnLogin = new JButton("Login");
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.RED);

        btnLogin.addActionListener(e -> doLogin());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username row
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblUser, gbc);
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        // Password row
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(lblPass, gbc);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // Button row
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnLogin, gbc);

        // Status row
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(lblStatus, gbc);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
    String username = txtUsername.getText().trim();
    String password = new String(txtPassword.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
        lblStatus.setText("Lütfen kullanıcı adı ve şifre girin.");
        return;
    }

    StaffUser staff = loginService.authenticate(username, password);

    if (staff != null) {
        lblStatus.setForeground(new Color(0, 128, 0));
        lblStatus.setText("Hoş geldin, " + staff.getFullName());

        JOptionPane.showMessageDialog(this,
                "Login başarılı!\nStaffID: " + staff.getStaffId() +
                "\nAd: " + staff.getFullName() +
                "\nRol: " + staff.getRole());

        // 🔹 Buradan sonra Staff Dashboard ekranını açıyoruz
        StaffDashboardFrame dashboard = new StaffDashboardFrame(staff);
        dashboard.setVisible(true);

        // Login penceresini kapat
        this.dispose();

    } else {
        lblStatus.setForeground(Color.RED);
        lblStatus.setText("Hatalı kullanıcı adı veya şifre.");
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StaffLoginFrame().setVisible(true));
    }
}
