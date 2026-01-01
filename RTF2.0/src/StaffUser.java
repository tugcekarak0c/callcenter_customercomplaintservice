public class StaffUser {
    private int staffId;
    private String firstName;
    private String lastName;
    private String role;

    public StaffUser(int staffId, String firstName, String lastName, String role) {
        this.staffId = staffId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public int getStaffId() {
        return staffId;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getRole() {
        return role;
    }
}
