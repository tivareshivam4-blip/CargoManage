package cargomanage.model;

public class Transfer {
    private int id;
    private int productId;
    private int sourceBranchId;
    private int destBranchId;
    private int quantity;
    private String status = "PENDING";
    private String requestedBy;
    private String approvedBy;
    private String receivedBy;
    private String requestDate;
    private String dispatchDate;
    private String receivedDate;
    private String remarks;

    private String productName;
    private String productUnit = "KG";
    private String sourceBranchName;
    private String destBranchName;

    public Transfer() {}

    public Transfer(int id, int productId, int sourceBranchId, int destBranchId, int quantity,
                    String requestedBy, String requestDate) {
        this.id = id;
        this.productId = productId;
        this.sourceBranchId = sourceBranchId;
        this.destBranchId = destBranchId;
        this.quantity = quantity;
        this.requestedBy = requestedBy;
        this.requestDate = requestDate;
        this.status = "PENDING";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getSourceBranchId() { return sourceBranchId; }
    public void setSourceBranchId(int sourceBranchId) { this.sourceBranchId = sourceBranchId; }

    public int getDestBranchId() { return destBranchId; }
    public void setDestBranchId(int destBranchId) { this.destBranchId = destBranchId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }

    public String getRequestDate() { return requestDate; }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }

    public String getDispatchDate() { return dispatchDate; }
    public void setDispatchDate(String dispatchDate) { this.dispatchDate = dispatchDate; }

    public String getReceivedDate() { return receivedDate; }
    public void setReceivedDate(String receivedDate) { this.receivedDate = receivedDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductUnit() { return productUnit; }
    public void setProductUnit(String productUnit) { this.productUnit = productUnit; }

    public String getSourceBranchName() { return sourceBranchName; }
    public void setSourceBranchName(String sourceBranchName) { this.sourceBranchName = sourceBranchName; }

    public String getDestBranchName() { return destBranchName; }
    public void setDestBranchName(String destBranchName) { this.destBranchName = destBranchName; }
}

