import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * CustomerChangePasswordDialog
 *
 * Provides a dialog for customers to change their account password.
 * Allows entering the current password and setting a new password,
 * with necessary validation checks.
 *
 * Tables used:
 *  - CustomerUsers (stores username and password hash)
 *
 * Updates the customer's password securely after verification.
 */


public class CustomerChangePasswordDialog extends JDialog {

    private final String username;

    private final Color PRIMARY_DARK = new Color(62, 39, 35);    
    private final Color ACCENT_COLOR = new Color(191, 54, 12);   
    private final Color BG_COLOR = new Color(245, 245, 245);     
    private final Color TEXT_COLOR = new Color(33, 33, 33);      

    private JLabel lblInfo;
    private JLabel lblOld;
    private JLabel lblNew;
    private JLabel lblConfirm;

    private JPasswordField pfOld;
    private JPasswordField pfNew;
    private JPasswordField pfConfirm;

    private JButton btnEyeOld;
    private JButton btnEyeNew;
    private JButton btnEyeConfirm;

    private char echoOld;
    private char echoNew;
    private char echoConfirm;

    private JButton btnSave;
    private JButton btnCancel;

    public CustomerChangePasswordDialog(Frame parent, String username) {
        super(parent, "Security Settings", true);
        this.username = username;

        getContentPane().setBackground(BG_COLOR);

        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setSize(450, 300); 
        setLayout(new BorderLayout(0, 0));

        //  HEADER 
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_DARK);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        lblInfo = new JLabel("CHANGE PASSWORD: " + username.toUpperCase(), SwingConstants.CENTER);
        lblInfo.setFont(new Font("Arial", Font.BOLD, 14));
        lblInfo.setForeground(Color.WHITE);
        headerPanel.add(lblInfo, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        //  CENTER FORM 
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BG_COLOR);
        centerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5); // Better spacing
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblOld = createStyledLabel("Old Password:");
        lblNew = createStyledLabel("New Password:");
        lblConfirm = createStyledLabel("Confirm Password:");

        pfOld = createStyledPasswordField(18);
        pfNew = createStyledPasswordField(18);
        pfConfirm = createStyledPasswordField(18);

        echoOld = pfOld.getEchoChar();
        echoNew = pfNew.getEchoChar();
        echoConfirm = pfConfirm.getEchoChar();

        btnEyeOld = createEyeButton(pfOld, () -> echoOld);
        btnEyeNew = createEyeButton(pfNew, () -> echoNew);
        btnEyeConfirm = createEyeButton(pfConfirm, () -> echoConfirm);

        int row = 0;

        // Old row
        addFormRow(centerPanel, gbc, row++, lblOld, pfOld, btnEyeOld);
        // New row
        addFormRow(centerPanel, gbc, row++, lblNew, pfNew, btnEyeNew);
        // Confirm row
        addFormRow(centerPanel, gbc, row++, lblConfirm, pfConfirm, btnEyeConfirm);

        add(centerPanel, BorderLayout.CENTER);

        // BUTTONS 
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        btnCancel = createStyledButton("Cancel", Color.LIGHT_GRAY, Color.BLACK);
        btnSave = createStyledButton("Save Changes", ACCENT_COLOR, Color.WHITE);

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnSave);
        add(bottomPanel, BorderLayout.SOUTH);

        //  EVENTS
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleChangePassword());

        getRootPane().setDefaultButton(btnSave);

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }


    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, JLabel lbl, JComponent field, JButton eye) {
        // Label
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(lbl, gbc);

        // Field
        gbc.gridx = 1; gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);

        // Eye Button
        gbc.gridx = 2; gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(eye, gbc);
    }

    private JLabel createStyledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private JPasswordField createStyledPasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setFont(new Font("Arial", Font.PLAIN, 14));
        // Flat modern border
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return pf;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createEyeButton(JPasswordField field, EchoSupplier echoSupplier) {
        JButton b = new JButton("👁"); 
        b.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        b.setFocusable(false);
        b.setBackground(BG_COLOR); // Blend with background
        b.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5)); // No border, just padding
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setToolTipText("Hold to show password");

        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                field.setEchoChar((char) 0);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                field.setEchoChar(echoSupplier.get());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                field.setEchoChar(echoSupplier.get());
            }
        });

        return b;
    }

    @FunctionalInterface
    private interface EchoSupplier {
        char get();
    }

    private final Color TEXT_DARK = new Color(50, 50, 50);


    private void handleChangePassword() {
        String oldPass = new String(pfOld.getPassword()).trim();
        String newPass = new String(pfNew.getPassword()).trim();
        String confirm = new String(pfConfirm.getPassword()).trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!checkOldPassword(username, oldPass)) {
            JOptionPane.showMessageDialog(this, "Old password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (updatePassword(username, newPass)) {
            JOptionPane.showMessageDialog(this, "Password successfully changed.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Password could not be changed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean checkOldPassword(String username, String oldPass) {
        String sql = "SELECT PasswordHash FROM CustomerUsers WHERE Username = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String current = rs.getString("PasswordHash");
                    return current != null && current.equals(oldPass);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error checking old password:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private boolean updatePassword(String username, String newPass) {
        String sql = "UPDATE CustomerUsers SET PasswordHash = ? WHERE Username = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPass);
            ps.setString(2, username);

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating password:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}