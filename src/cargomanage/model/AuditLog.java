package cargomanage.model;

public class AuditLog {
    private int id;
    private String username;
    private String action;
    private String details;
    private String timestamp;

    public AuditLog() {}

    public AuditLog(String username, String action, String details, String timestamp) {
        this.username = username;
        this.action = action;
        this.details = details;
        this.timestamp = timestamp;
    }

    public AuditLog(int id, String username, String action, String details, String timestamp) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.details = details;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

