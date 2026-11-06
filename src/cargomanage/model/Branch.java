package cargomanage.model;

public class Branch {
    private int id;
    private String name;
    private String location;
    private String code;
    private String phone;

    public Branch() {}

    public Branch(int id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public Branch(int id, String name, String location, String code, String phone) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.code = code;
        this.phone = phone;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}

