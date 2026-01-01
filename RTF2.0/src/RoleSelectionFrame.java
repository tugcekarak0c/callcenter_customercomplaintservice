import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * RoleSelectionFrame
 *
 * Allows users to select their role before authentication.
 * Provides navigation options for staff and customer access,
 * redirecting users to the appropriate login or registration screens.
 *
 * Tables used:
 *  - StaffLogins (staff authentication)
 *  - Staff (staff information)
 *  - CustomerUsers (customer account information)
 *  - Customers (customer profile information)
 */


public class RoleSelectionFrame extends JFrame {
    
    private JButton btnCustomerLogin;
    private JButton btnStaffLogin;
    private JLabel lblTitle;

    private final Color COLOR_BG_CREAM = new Color(255, 248, 225);
    private final Color COLOR_TEXT_BROWN = new Color(93, 64, 55);
    private final Color COLOR_BTN_WOOD = new Color(121, 85, 72);
    private final Color COLOR_BTN_GREEN = new Color(56, 142, 60);

    public RoleSelectionFrame() {
        setTitle("Select Login Type");
        setSize(450, 320); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        
        getContentPane().setBackground(COLOR_BG_CREAM);
        setLayout(new BorderLayout());
        
        initComponents();
    }

    private void initComponents() {
        lblTitle = new JLabel("Please select your login type", SwingConstants.CENTER);
        // Georgia font for that rustic look
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_TEXT_BROWN);
        lblTitle.setBorder(new EmptyBorder(25, 0, 10, 0)); 
        add(lblTitle, BorderLayout.NORTH);

        // BUTTON PANEL 
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 15, 15)); 
        buttonPanel.setBackground(COLOR_BG_CREAM); 
        buttonPanel.setBorder(new CompoundBorder(
                new EmptyBorder(20, 50, 40, 50),
                new EmptyBorder(0, 0, 0, 0)
        ));

        btnCustomerLogin = new JButton("Customer Login");
        styleButton(btnCustomerLogin);

        btnStaffLogin = new JButton("Staff Login");
        styleButton(btnStaffLogin);

        buttonPanel.add(btnCustomerLogin);
        buttonPanel.add(btnStaffLogin);

        add(buttonPanel, BorderLayout.CENTER);

        // EVENT

        // Customer → CustomerLogOrSignFrame
        btnCustomerLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CustomerLogOrSignFrame customerFrame = new CustomerLogOrSignFrame();
                customerFrame.setVisible(true);

                dispose();
            }
        });

        // Staff → ComplaintStaffLoginFrame
        btnStaffLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ComplaintStaffLoginFrame staffFrame = new ComplaintStaffLoginFrame();
                staffFrame.setVisible(true);

                dispose();
            }
        });
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setBackground(COLOR_BTN_WOOD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(COLOR_TEXT_BROWN, 2),
                new EmptyBorder(10, 20, 10, 20)
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

    public static void main(String[] args) {
        // custom colors 
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            new RoleSelectionFrame().setVisible(true);
        });
    }
}