package cargomanage.model;

public class Product {
    private int id;
    private String name;
    private String category;
    private String brand = "Generic";
    private String description = "";
    private String sku = "";
    private String barcode = "";
    private String unit = "KG";
    private double purchasePrice = 0.0;
    private double sellingPrice = 0.0;
    private int reorderLevel = 10;
    private String supplierName = "";
    private String location = "Rack A-1";
    private String mfgDate = "";
    private String expDate = "";
    private String status = "Active";
    private int totalStock = 0;

    public Product() {}

    public Product(int id, String name, String category, String unit, int reorderLevel, String barcode) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.reorderLevel = reorderLevel;
        this.barcode = barcode;
        this.sku = barcode;
    }

    public Product(int id, String name, String category, String brand, String description,
                   String sku, String barcode, String unit, double purchasePrice, double sellingPrice,
                   int reorderLevel, String supplierName, String location, String mfgDate,
                   String expDate, String status) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.description = description;
        this.sku = sku;
        this.barcode = barcode;
        this.unit = unit;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.reorderLevel = reorderLevel;
        this.supplierName = supplierName;
        this.location = location;
        this.mfgDate = mfgDate;
        this.expDate = expDate;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getMfgDate() { return mfgDate; }
    public void setMfgDate(String mfgDate) { this.mfgDate = mfgDate; }

    public String getExpDate() { return expDate; }
    public void setExpDate(String expDate) { this.expDate = expDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalStock() { return totalStock; }
    public void setTotalStock(int totalStock) { this.totalStock = totalStock; }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}

