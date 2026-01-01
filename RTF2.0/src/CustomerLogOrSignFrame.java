import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * CustomerLogOrSignFrame
 *
 * Provides the entry screen for customers to either
 * log in to an existing account or create a new account.
 * Acts as a navigation gateway before accessing
 * customer-specific features.
 *
 * Tables used:
 *  - Customers (customer basic information)
 *  - CustomerUsers (customer login credentials)
 */


public class CustomerLogOrSignFrame extends JFrame {

    private JButton btnSignUp;
    private JButton btnLogIn;
    private JButton btnBack;
    private JLabel lblTitle;
    private JLabel lblHaveAccount;
    private JLabel lblNoAccount;

    private final Color COLOR_BG_CREAM = new Color(255, 248, 225);
    private final Color COLOR_TEXT_BROWN = new Color(93, 64, 55);
    private final Color COLOR_BTN_WOOD = new Color(121, 85, 72);
    private final Color COLOR_BTN_GREEN = new Color(56, 142, 60);

    public CustomerLogOrSignFrame() {
        setTitle("Customer Login or Sign Up");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 450); 
        setLocationRelativeTo(null);
        
        getContentPane().setBackground(COLOR_BG_CREAM);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        lblTitle = new JLabel("WELCOME!", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 28)); 
        lblTitle.setForeground(COLOR_TEXT_BROWN);
        lblTitle.setBorder(new EmptyBorder(30, 0, 10, 0)); 
        add(lblTitle, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(COLOR_BG_CREAM); 
        
        centerPanel.setBorder(new EmptyBorder(10, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;

        lblHaveAccount = new JLabel("Do you already have an account?", SwingConstants.CENTER);
        styleLabel(lblHaveAccount);

        lblNoAccount = new JLabel("If you don't have an account", SwingConstants.CENTER);
        styleLabel(lblNoAccount);

        btnLogIn = new JButton("Log In");
        styleButton(btnLogIn);

        btnSignUp = new JButton("Sign Up");
        styleButton(btnSignUp);
        
        Dimension btnDim = new Dimension(220, 45);
        btnLogIn.setPreferredSize(btnDim);
        btnSignUp.setPreferredSize(btnDim);

        int row = 0;

        // Have Account Label
        gbc.gridx = 0; gbc.gridy = row++;
        centerPanel.add(lblHaveAccount, gbc);

        // Login Button
        gbc.gridx = 0; gbc.gridy = row++;
        centerPanel.add(btnLogIn, gbc);

        // Spacer (Visual gap)
        gbc.gridx = 0; gbc.gridy = row++;
        centerPanel.add(Box.createVerticalStrut(15), gbc);

        // No Account Label
        gbc.gridx = 0; gbc.gridy = row++;
        centerPanel.add(lblNoAccount, gbc);

        // Sign Up Button
        gbc.gridx = 0; gbc.gridy = row++;
        centerPanel.add(btnSignUp, gbc);

        add(centerPanel, BorderLayout.CENTER);

        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(COLOR_BG_CREAM); 
        bottomPanel.setBorder(new EmptyBorder(10, 15, 20, 15)); 
        
        btnBack = new JButton("Back"); 
        styleButton(btnBack);
        btnBack.setPreferredSize(new Dimension(100, 35)); 

        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        // EVENTS 
        btnLogIn.addActionListener(e -> {
            LogIn log = new LogIn();
            log.setVisible(true);
            dispose();
        });

        btnSignUp.addActionListener(e -> {
            CustomerSignUpFrame s = new CustomerSignUpFrame();
            s.setVisible(true);
            dispose();
        });

        btnBack.addActionListener(e -> { 
            new RoleSelectionFrame().setVisible(true);
            dispose();
        });
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setBackground(COLOR_BTN_WOOD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(COLOR_TEXT_BROWN, 2),
                new EmptyBorder(8, 15, 8, 15)
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

    public static void main(String[] args) {
        // Set CrossPlatform colors
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {}

        SwingUtilities.invokeLater(() -> new CustomerLogOrSignFrame().setVisible(true));
    }
}