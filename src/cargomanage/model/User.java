package cargomanage.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String role; // "ADMIN", "MANAGER", "ACCOUNTANT", "BRANCH_STAFF"
    private Integer branchId;
    private String status; // "ACTIVE", "INACTIVE"
    private String createdAt;
    private String lastLogin;

    public User() {}

    public User(int id, String username, String passwordHash, String fullName, String role, Integer branchId, String status) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.branchId = branchId;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isManager() {
        return "MANAGER".equalsIgnoreCase(role);
    }

    public boolean isAccountant() {
        return "ACCOUNTANT".equalsIgnoreCase(role);
    }

    public boolean isBranchStaff() {
        return "BRANCH_STAFF".equalsIgnoreCase(role);
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}

