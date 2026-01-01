import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * CustomerDashboard
 *
 * Displays the main dashboard for customers after login.
 * Shows customer profile information and lists of
 * open and closed complaints.
 *
 * Allows customers to create new complaints,
 * view complaint history, and fill out
 * satisfaction surveys for closed complaints.
 *
 * Tables used:
 *  - Customers (customer basic information)
 *  - CustomerContactInfo (email and phone details)
 *  - Address (customer address information)
 *  - Complaints (customer complaints and active/closed status)
 *  - ComplaintTexts (complaint titles)
 *  - ComplaintStatus (status of complaints)
 *  - ComplaintPriority (priority information)
 */


public class CustomerDashboard extends JFrame {

    private final int customerId;

    // UI 
    private JLabel lblProfile;       
    private JTextArea txtDetails;   

    private JList<String> listOpenComplaints;
    private JList<String> listClosedComplaints;

    private DefaultListModel<String> openModel;
    private DefaultListModel<String> closedModel;

    private JButton btnNewComplaint;
    private JButton btnLogout;

    private final Color PRIMARY_DARK = new Color(62, 39, 35); 
    private final Color ACCENT_COLOR = new Color(191, 54, 12); 
    private final Color BG_COLOR = new Color(245, 245, 245);
    private final Color TEXT_COLOR = new Color(33, 33, 33);

    private final Font FONT_TITLE = new Font("Arial", Font.BOLD, 22);
    private final Font FONT_NAME_BOLD = new Font("Arial", Font.BOLD, 24); // Bigger & Bold Name
    private final Font FONT_NORMAL = new Font("Arial", Font.PLAIN, 14);

    // Auto refresh timer
    private Timer autoRefreshTimer;

    public CustomerDashboard(int customerId) {
        this.customerId = customerId;

        setTitle("Farm Management System");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(BG_COLOR);

        initComponents();

        // DB load
        loadCustomerInfo();
        loadComplaintLists();

        // Auto refresh every 5 seconds
        autoRefreshTimer = new Timer(5000, e -> loadComplaintLists());
        autoRefreshTimer.start();

        // Stop timer when window closes
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (autoRefreshTimer != null) autoRefreshTimer.stop();
            }
        });
    }

    private void initComponents() {
        //  HEADER 
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_DARK);
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel title = new JLabel("CUSTOMER DASHBOARD");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE); 

        // Logout Button
        btnLogout = createStyledButton("LOG OUT", new Color(180, 180, 180), Color.BLACK);
        btnLogout.addActionListener(e -> {
            if (autoRefreshTimer != null) autoRefreshTimer.stop();
            new RoleSelectionFrame().setVisible(true);
            dispose();
        });

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(BG_COLOR);
        mainContent.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel profileCard = new JPanel(new BorderLayout());
        profileCard.setBackground(Color.WHITE);
        profileCard.setBorder(new CompoundBorder(
                new LineBorder(ACCENT_COLOR, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        profileCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel lblIcon = new JLabel(" USER PROFILE ");
        lblIcon.setOpaque(true);
        lblIcon.setBackground(BG_COLOR);
        lblIcon.setFont(new Font("Arial", Font.BOLD, 12));
        lblIcon.setBorder(new EmptyBorder(5,5,5,5));
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(Color.WHITE);

        lblProfile = new JLabel("Loading...", SwingConstants.LEFT);
        lblProfile.setFont(FONT_NAME_BOLD);
        lblProfile.setForeground(PRIMARY_DARK); 

        txtDetails = new JTextArea("Loading details...");
        txtDetails.setFont(FONT_NORMAL);
        txtDetails.setForeground(Color.GRAY);
        txtDetails.setEditable(false);
        txtDetails.setBackground(Color.WHITE);
        txtDetails.setBorder(null);
        txtDetails.setLineWrap(true);

        infoPanel.add(lblProfile);
        infoPanel.add(txtDetails);

        profileCard.add(lblIcon, BorderLayout.NORTH);
        profileCard.add(infoPanel, BorderLayout.CENTER);

        //  LISTS 
        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 25, 0));
        listsPanel.setBackground(BG_COLOR);
        listsPanel.setBorder(new EmptyBorder(25, 0, 25, 0));

        openModel = new DefaultListModel<>();
        closedModel = new DefaultListModel<>();

        listOpenComplaints = new JList<>(openModel);
        styleList(listOpenComplaints);

        listClosedComplaints = new JList<>(closedModel);
        styleList(listClosedComplaints);

        // Click Event
        listClosedComplaints.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                Integer complaintId = extractComplaintId(listClosedComplaints.getSelectedValue());
                if (complaintId == null) return;
                new SatisfactionSurveyFrame(complaintId).setVisible(true);
            }
        });

        
        listsPanel.add(createTitledScrollPane(listOpenComplaints, "Open Complaints"));
        listsPanel.add(createTitledScrollPane(listClosedComplaints, "Complaint History"));

        // NEW BUTTON 
        btnNewComplaint = createStyledButton("+ CREATE NEW COMPLAINT", ACCENT_COLOR, Color.WHITE);
        btnNewComplaint.setFont(new Font("Arial", Font.BOLD, 16));
        btnNewComplaint.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnNewComplaint.setMaximumSize(new Dimension(320, 55));

        btnNewComplaint.addActionListener(e -> {
            NewComplaintScreen nc = new NewComplaintScreen(customerId);
            nc.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) { loadComplaintLists(); }
                @Override
                public void windowClosing(WindowEvent e) { SwingUtilities.invokeLater(() -> loadComplaintLists()); }
            });
            nc.setVisible(true);
        });

        mainContent.add(profileCard);
        mainContent.add(listsPanel);
        mainContent.add(btnNewComplaint);

        add(headerPanel, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);
    }


    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        // Clean flat border
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleList(JList<String> list) {
        list.setFont(FONT_NORMAL);
        list.setForeground(TEXT_COLOR);
        list.setBackground(Color.WHITE);
        list.setSelectionBackground(new Color(230, 230, 230)); 
        list.setSelectionForeground(ACCENT_COLOR); 
        list.setFixedCellHeight(35);
    }

    private JScrollPane createTitledScrollPane(JComponent view, String title) {
        JScrollPane scroller = new JScrollPane(view);
        scroller.getViewport().setBackground(Color.WHITE);
        
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                title
        );
        border.setTitleFont(new Font("Arial", Font.BOLD, 14));
        border.setTitleColor(PRIMARY_DARK);
        scroller.setBorder(border);
        return scroller;
    }

    //  DB METHODS 

    private Integer extractComplaintId(String s) {
        if (s == null) return null;
        s = s.trim();
        if (!s.startsWith("#")) return null;
        int dash = s.indexOf(" - ");
        if (dash <= 1) return null;
        try {
            return Integer.parseInt(s.substring(1, dash).trim());
        } catch (NumberFormatException ex) { return null; }
    }

    private void loadCustomerInfo() {
        String sql = """
            SELECT c.FirstName, c.LastName, ci.Email, ci.PhoneNumber,
                   a.City, a.Country, a.AddressLine
            FROM Customers c
            LEFT JOIN CustomerContactInfo ci ON ci.CustomerID = c.CustomerID
            LEFT JOIN Address a ON a.CustomerID = c.CustomerID
            WHERE c.CustomerID = ?
            """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String fullName = (rs.getString("FirstName") + " " + rs.getString("LastName")).trim();
                    lblProfile.setText(fullName.isEmpty() ? "Unknown User" : fullName);

                    StringBuilder details = new StringBuilder();
                    String phone = rs.getString("PhoneNumber");
                    String email = rs.getString("Email");
                    String city = rs.getString("City");
                    String address = rs.getString("AddressLine");

                    if (phone != null) details.append("Phone: ").append(phone).append("  ");
                    if (email != null) details.append("Email: ").append(email);
                    details.append("\n");
                    if (address != null) details.append("Address: ").append(address);
                    if (city != null) details.append(", ").append(city);

                    txtDetails.setText(details.toString());

                } else {
                    lblProfile.setText("User Not Found");
                    txtDetails.setText("-");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            lblProfile.setText("Error!");
        }
    }

    private void loadComplaintLists() {
        openModel.clear();
        closedModel.clear();

        String baseSql = """
            SELECT c.ComplaintID, t.Title, s.Name AS StatusName, p.Name AS PriorityName
            FROM Complaints c
            JOIN ComplaintTexts t ON t.ComplaintID = c.ComplaintID
            JOIN ComplaintStatus s ON s.ComplaintStatusID = c.ComplaintStatusID
            JOIN ComplaintPriority p ON p.ComplaintPriorityID = c.ComplaintPriorityID
            WHERE c.CustomerID = ? AND c.IsActive = ?
            ORDER BY t.CreatedAt DESC
            """;

        try (Connection conn = DbConfig.getConnection()) {
            // Open
            try (PreparedStatement ps = conn.prepareStatement(baseSql)) {
                ps.setInt(1, customerId);
                ps.setBoolean(2, true);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean hasAny = false;
                    while (rs.next()) {
                        hasAny = true;
                        openModel.addElement("#" + rs.getInt("ComplaintID") + " - " + rs.getString("Title"));

                    }
                    if (!hasAny) openModel.addElement("No active complaints.");
                }
            }
            // Closed
            try (PreparedStatement ps = conn.prepareStatement(baseSql)) {
                ps.setInt(1, customerId);
                ps.setBoolean(2, false);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean hasAny = false;
                    while (rs.next()) {
                        hasAny = true;
                        closedModel.addElement("#" + rs.getInt("ComplaintID") + " - " + rs.getString("Title") );
                    }
                    if (!hasAny) closedModel.addElement("No closed complaints.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            openModel.addElement("Data error.");
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new CustomerDashboard(1).setVisible(true));
    }
}