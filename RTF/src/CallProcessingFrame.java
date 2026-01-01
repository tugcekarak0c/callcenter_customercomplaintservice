package com.mycompany.rtf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

public class CallProcessingFrame extends JFrame {

    // Phone format: 0(555) 123 45 67
    private static final String PHONE_REGEX = "^0\\(\\d{3}\\) \\d{3} \\d{2} \\d{2}$";
    private static final String PHONE_PLACEHOLDER = "0(555) 123 45 67";

    // !!! UPDATE THESE IDs ACCORDING TO YOUR DATABASE !!!
    // ComplaintSources: e.g. row "Call Center"
    private static final int DEFAULT_COMPLAINT_SOURCE_ID = 2;
    // ComplaintStatus: e.g. row "Open"
    private static final int DEFAULT_COMPLAINT_STATUS_OPEN_ID = 1;
    // ComplaintPriority: e.g. row "Medium"
    private static final int DEFAULT_COMPLAINT_PRIORITY_NORMAL_ID = 2;

    private final StaffUser staff;

    private JFormattedTextField txtPhone;
    private JButton btnFindCustomer;
    private JLabel lblCustomerInfo;

    private JComboBox<ComboItem> cbCallType;
    private JComboBox<ComboItem> cbCallTopic;
    private JComboBox<ComboItem> cbCallResult;

    private JTextArea txtNotes;
    private JButton btnEndCall;

    // If a customer is found (or created) by phone, their ID is stored here
    private Integer currentCustomerId = null;

    // Call start time (when this screen is opened)
    private LocalDateTime callStartTime;

    public CallProcessingFrame(StaffUser staff) {
        this.staff = staff;

        setTitle("Call Processing - " + staff.getFullName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        loadLookupData(); // Load CallTypes, CallTopics, CallResults

        // Call is considered "started" when this screen is created
        this.callStartTime = LocalDateTime.now();

        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Call Processing");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel lblStaff = new JLabel("Agent: " + staff.getFullName() + " (Role: " + staff.getRole() + ")");

        // Phone & customer search
        JLabel lblPhone = new JLabel("Caller Phone:");

        try {
            MaskFormatter phoneMask = new MaskFormatter("0(###) ### ## ##");
            phoneMask.setPlaceholderCharacter('_'); // show underscores on empty digits
            txtPhone = new JFormattedTextField(phoneMask);
            txtPhone.setColumns(15);
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
            // fallback if mask cannot be created
            txtPhone = new JFormattedTextField();
            txtPhone.setColumns(15);
        }

        btnFindCustomer = new JButton("Find Customer");
        lblCustomerInfo = new JLabel("Customer: (not found)");

        btnFindCustomer.addActionListener(e -> findCustomerByPhone());

        // Comboboxes
        JLabel lblCallType = new JLabel("Call Type:");
        cbCallType = new JComboBox<>();

        JLabel lblCallTopic = new JLabel("Topic:");
        cbCallTopic = new JComboBox<>();

        JLabel lblCallResult = new JLabel("Result:");
        cbCallResult = new JComboBox<>();

        // Notes area
        JLabel lblNotes = new JLabel("Notes:");
        txtNotes = new JTextArea(4, 20);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(txtNotes);

        // End Call button (call is saved when this is pressed)
        btnEndCall = new JButton("End Call");
        btnEndCall.addActionListener(e -> endCall());

        // Header panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.add(lblTitle);
        headerPanel.add(lblStaff);

        // Phone panel
        JPanel phonePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        phonePanel.add(lblPhone);
        phonePanel.add(txtPhone);
        phonePanel.add(btnFindCustomer);

        // Call info (lookup) panel
        JPanel lookupPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        lookupPanel.setBorder(BorderFactory.createTitledBorder("Call Information"));
        lookupPanel.add(lblCallType);
        lookupPanel.add(cbCallType);
        lookupPanel.add(lblCallTopic);
        lookupPanel.add(cbCallTopic);
        lookupPanel.add(lblCallResult);
        lookupPanel.add(cbCallResult);

        // Customer info panel
        JPanel customerPanel = new JPanel(new BorderLayout());
        customerPanel.setBorder(BorderFactory.createTitledBorder("Customer Information"));
        customerPanel.add(lblCustomerInfo, BorderLayout.CENTER);

        // Notes panel
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createTitledBorder("Notes"));
        notesPanel.add(lblNotes, BorderLayout.NORTH);
        notesPanel.add(notesScroll, BorderLayout.CENTER);

        // Bottom (End Call) panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnEndCall);

        // Center panel (lookup + customer + notes)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(lookupPanel, BorderLayout.NORTH);
        centerPanel.add(customerPanel, BorderLayout.CENTER);
        centerPanel.add(notesPanel, BorderLayout.SOUTH);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(phonePanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Optional placeholder logic – currently not called.
     * If you want, call setupPhonePlaceholder() from initComponents().
     */
    @SuppressWarnings("unused")
    private void setupPhonePlaceholder() {
        txtPhone.setForeground(Color.GRAY);
        txtPhone.setText(PHONE_PLACEHOLDER);

        txtPhone.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtPhone.getText().equals(PHONE_PLACEHOLDER)) {
                    txtPhone.setText("");
                    txtPhone.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtPhone.getText().trim().isEmpty()) {
                    txtPhone.setForeground(Color.GRAY);
                    txtPhone.setText(PHONE_PLACEHOLDER);
                }
            }
        });
    }

    private String getCleanPhoneFromField() {
        String text = txtPhone.getText();
        if (text == null) return "";
        // if mask underscores remain → treat as invalid
        if (text.contains("_")) {
            return "";
        }
        return text.trim();
    }

    /**
     * Uses tables:
     *  - CallTypes
     *  - CallTopics
     *  - CallResults
     *
     * Loads lookup data into combo boxes.
     */
    private void loadLookupData() {
        loadCombo(cbCallType,
                "SELECT CallTypeID, Name FROM CallTypes ORDER BY Name",
                "CallTypeID", "Name");

        loadCombo(cbCallTopic,
                "SELECT CallTopicID, Name FROM CallTopics ORDER BY Name",
                "CallTopicID", "Name");

        loadCombo(cbCallResult,
                "SELECT CallResultID, Name FROM CallResults ORDER BY Name",
                "CallResultID", "Name");
    }

    private void loadCombo(JComboBox<ComboItem> combo,
                           String sql,
                           String idColumn,
                           String nameColumn) {

        combo.removeAllItems();

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt(idColumn);
                String name = rs.getString(nameColumn);
                combo.addItem(new ComboItem(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error while loading lookup data:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidPhoneFormat(String phone) {
        return phone != null && phone.matches(PHONE_REGEX);
    }

    /**
     * Uses tables:
     *  - Customers
     *  - CustomerContactInfo
     *
     * Logic: find customer by exact phone number.
     * If not found → open NewCustomerPopup (modal) and create a new customer
     * with username + password (CustomerUsers).
     */
    private void findCustomerByPhone() {
        String raw = txtPhone.getText().trim();
        String phone = raw.equals(PHONE_PLACEHOLDER) ? "" : raw;

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter the caller phone number.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isValidPhoneFormat(phone)) {
            JOptionPane.showMessageDialog(this,
                    "Phone format is invalid.\nRequired format: 0(***) *** ** **\nExample: 0(555) 123 45 67",
                    "Format Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = """
            SELECT c.CustomerID, c.FirstName, c.LastName
            FROM CustomerContactInfo ci
            JOIN Customers c ON ci.CustomerID = c.CustomerID
            WHERE ci.PhoneNumber = ?
            """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // CUSTOMER FOUND
                    currentCustomerId = rs.getInt("CustomerID");
                    String firstName = rs.getString("FirstName");
                    String lastName = rs.getString("LastName");

                    lblCustomerInfo.setText("Customer: " + firstName + " " + lastName +
                            " (ID: " + currentCustomerId + ")");

                    JOptionPane.showMessageDialog(this,
                            "Customer found:\n" +
                                    firstName + " " + lastName + "\nCustomer ID: " + currentCustomerId,
                            "Customer Found",
                            JOptionPane.INFORMATION_MESSAGE);

                } else {
                    // CUSTOMER NOT FOUND → POPUP FOR NEW CUSTOMER
                    currentCustomerId = null;
                    lblCustomerInfo.setText("Customer not found.");

                    int choice = JOptionPane.showConfirmDialog(
                            this,
                            "No customer found with this phone number.\n" +
                                    "Do you want to create a new customer with this number?",
                            "Customer Not Found",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        NewCustomerPopup popup = new NewCustomerPopup(this, phone);
                        popup.setVisible(true); // modal – waits until closed

                        Integer newId = popup.getCreatedCustomerId();
                        if (newId != null) {
                            currentCustomerId = newId;

                            String name = popup.getCreatedCustomerName();
                            String username = popup.getCreatedUsername();

                            lblCustomerInfo.setText(
                                    "Customer: " +
                                            (name != null ? name : "(ID " + newId + ")") +
                                            " (ID: " + newId +
                                            (username != null ? ", Username: " + username : "") +
                                            ")"
                            );

                            JOptionPane.showMessageDialog(this,
                                    "New customer created and linked to this call.\n" +
                                            "CustomerID: " + newId,
                                    "New Customer",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error while searching customer:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Uses tables:
     *  - Calls
     *  - CallDetails
     *  - CallTypes
     *  - CallTopics (for complaint detection)
     *  - CallResults
     *  - Complaints (only if topic is Complaint)
     *  - ComplaintTexts (only if topic is Complaint)
     *  - ComplaintSources, ComplaintStatus, ComplaintPriority (via default IDs)
     *
     * Logic:
     *  1) Call starts when this screen opens (callStartTime).
     *  2) When agent clicks "End Call":
     *     - Insert into Calls (CustomerID, StaffID, CallTypeID, CallTopicID, CallResultID)
     *     - If Topic = Complaint → insert into Complaints + ComplaintTexts
     *     - Insert into CallDetails (CallID, PhoneNumber, StartTime, EndTime, DurationSec, RelatedComplaintID, Notes)
     */
    private void endCall() {
        String raw = txtPhone.getText().trim();
        String phone = raw.equals(PHONE_PLACEHOLDER) ? "" : raw;

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Phone number cannot be empty.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isValidPhoneFormat(phone)) {
            JOptionPane.showMessageDialog(this,
                    "Phone format is invalid.\nRequired format: 0(***) *** ** **\nExample: 0(555) 123 45 67",
                    "Format Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ComboItem callTypeItem = (ComboItem) cbCallType.getSelectedItem();
        ComboItem callTopicItem = (ComboItem) cbCallTopic.getSelectedItem();
        ComboItem callResultItem = (ComboItem) cbCallResult.getSelectedItem();

        if (callTypeItem == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a call type.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer callTypeId = callTypeItem.id();
        Integer callTopicId = (callTopicItem != null) ? callTopicItem.id() : null;
        Integer callResultId = (callResultItem != null) ? callResultItem.id() : null;

        String notes = txtNotes.getText().trim();

        // Determine if selected topic represents a complaint
        boolean isComplaintTopic = false;
        if (callTopicItem != null) {
            String topicName = callTopicItem.name().toLowerCase();
            if (topicName.contains("complaint") || topicName.contains("şikayet")) {
                isComplaintTopic = true;
            }
        }

        // Call end time = when End Call button is pressed
        LocalDateTime callEndTime = LocalDateTime.now();
        long durationSecLong = ChronoUnit.SECONDS.between(callStartTime, callEndTime);
        int durationSec = (int) Math.max(durationSecLong, 0);

        Connection conn = null;
        try {
            conn = DbConfig.getConnection();
            conn.setAutoCommit(false);

            int callId;

            // 1) Insert into Calls
            String insertCallSql = """
                INSERT INTO Calls (CustomerID, StaffID, CallTypeID, CallTopicID, CallResultID)
                VALUES (?, ?, ?, ?, ?)
                """;

            try (PreparedStatement ps = conn.prepareStatement(insertCallSql, Statement.RETURN_GENERATED_KEYS)) {

                if (currentCustomerId != null) {
                    ps.setInt(1, currentCustomerId);
                } else {
                    ps.setNull(1, Types.INTEGER);
                }

                ps.setInt(2, staff.getStaffId());
                ps.setInt(3, callTypeId);

                if (callTopicId != null) {
                    ps.setInt(4, callTopicId);
                } else {
                    ps.setNull(4, Types.INTEGER);
                }

                if (callResultId != null) {
                    ps.setInt(5, callResultId);
                } else {
                    ps.setNull(5, Types.INTEGER);
                }

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        callId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated CallID.");
                    }
                }
            }

            // 2) If topic is Complaint and we have a customer → create complaint records
            Integer relatedComplaintId = null;

            if (isComplaintTopic) {
                if (currentCustomerId == null) {
                    JOptionPane.showMessageDialog(this,
                            "Topic is 'Complaint' but no customer is linked to this call.\n" +
                                    "Complaint record will not be created.",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                } else {
                    // Insert into Complaints
                    String insertComplaintSql = """
                        INSERT INTO Complaints
                        (CustomerID, ProductID, ComplaintCategoryID, ComplaintSourceID,
                         CallID, ComplaintStatusID, ComplaintPriorityID, AssignedStaffID, IsActive)
                        VALUES (?, NULL, NULL, ?, ?, ?, ?, ?, 1)
                        """;

                    try (PreparedStatement psC = conn.prepareStatement(insertComplaintSql, Statement.RETURN_GENERATED_KEYS)) {

                        psC.setInt(1, currentCustomerId);
                        psC.setInt(2, DEFAULT_COMPLAINT_SOURCE_ID);
                        psC.setInt(3, callId);
                        psC.setInt(4, DEFAULT_COMPLAINT_STATUS_OPEN_ID);
                        psC.setInt(5, DEFAULT_COMPLAINT_PRIORITY_NORMAL_ID);
                        psC.setInt(6, staff.getStaffId());

                        psC.executeUpdate();

                        try (ResultSet keysC = psC.getGeneratedKeys()) {
                            if (keysC.next()) {
                                relatedComplaintId = keysC.getInt(1);
                            } else {
                                throw new SQLException("Failed to retrieve generated ComplaintID.");
                            }
                        }
                    }

                    // Insert into ComplaintTexts
                    if (relatedComplaintId != null) {
                        String title = "Complaint from call #" + callId;
                        String description = notes.isEmpty()
                                ? "Complaint created from call. No additional notes."
                                : notes;

                        String insertTextsSql = """
                            INSERT INTO ComplaintTexts
                            (ComplaintID, Title, Description, CreatedAt, ClosedAt, LastUpdatedAt)
                            VALUES (?, ?, ?, SYSDATETIME(), NULL, SYSDATETIME())
                            """;

                        try (PreparedStatement psT = conn.prepareStatement(insertTextsSql)) {
                            psT.setInt(1, relatedComplaintId);
                            psT.setString(2, title);
                            psT.setString(3, description);
                            psT.executeUpdate();
                        }
                    }
                }
            }

            // 3) Insert into CallDetails
            String insertDetailsSql = """
                INSERT INTO CallDetails
                (CallID, PhoneNumber, StartTime, EndTime, DurationSec, RelatedComplaintID, Notes)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement ps2 = conn.prepareStatement(insertDetailsSql)) {
                ps2.setInt(1, callId);
                ps2.setString(2, phone);

                Timestamp startTs = Timestamp.valueOf(callStartTime);
                Timestamp endTs = Timestamp.valueOf(callEndTime);

                ps2.setTimestamp(3, startTs);   // StartTime = when screen opened
                ps2.setTimestamp(4, endTs);     // EndTime = when End Call pressed
                ps2.setInt(5, durationSec);     // DurationSec

                if (relatedComplaintId != null) {
                    ps2.setInt(6, relatedComplaintId);
                } else {
                    ps2.setNull(6, Types.INTEGER);
                }

                ps2.setString(7, notes.isEmpty() ? null : notes);
                ps2.executeUpdate();
            }

            conn.commit();

            String msg = "Call ended and saved successfully.\nCallID: " + callId +
                    "\nDuration: " + durationSec + " second(s).";

            if (isComplaintTopic && currentCustomerId != null) {
                msg += "\nA complaint record was also created and linked to this call.";
            }

            JOptionPane.showMessageDialog(this, msg, "Call Ended", JOptionPane.INFORMATION_MESSAGE);

            // Prevent double save
            btnEndCall.setEnabled(false);

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            JOptionPane.showMessageDialog(this,
                    "Error while ending/saving call:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
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

    // Simple helper class for JComboBox items (id + display text)
    private static class ComboItem {
        private final int id;
        private final String name;

        public ComboItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int id() {
            return id;
        }

        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
