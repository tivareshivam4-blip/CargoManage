package cargomanage.model;

public class Purchase {
    private int id;
    private String supplierName;
    private String supplierContact;
    private int productId;
    private int branchId;
    private int quantity;
    private double purchasePrice;
    private double totalAmount;
    private String purchaseDate;
    private String paymentStatus = "PENDING";
    private String paymentRef = "";
    private int receivedQty;
    private String status = "RECEIVED";
    private String createdBy;

    private String productName;
    private String branchName;
    private String unit = "KG";

    public Purchase() {}

    public Purchase(int id, String supplierName, int productId, int branchId, int quantity, String purchaseDate) {
        this.id = id;
        this.supplierName = supplierName;
        this.productId = productId;
        this.branchId = branchId;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.receivedQty = quantity;
        this.status = "RECEIVED";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierContact() { return supplierContact; }
    public void setSupplierContact(String supplierContact) { this.supplierContact = supplierContact; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentRef() { return paymentRef; }
    public void setPaymentRef(String paymentRef) { this.paymentRef = paymentRef; }

    public int getReceivedQty() { return receivedQty; }
    public void setReceivedQty(int receivedQty) { this.receivedQty = receivedQty; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}

