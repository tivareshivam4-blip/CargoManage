package cargomanage.model;

public class Shipment {
    private int id;
    private int transferId;
    private String trackingNumber;
    private String carrier = "CargoManage Fleet";
    private String status = "DISPATCHED";
    private String eta;
    private String dispatchDate;
    private String deliveryDate;

    private String sourceBranchName;
    private String destBranchName;
    private String productName;
    private int quantity;

    public Shipment() {}

    public Shipment(int id, int transferId, String status, String eta) {
        this.id = id;
        this.transferId = transferId;
        this.status = status;
        this.eta = eta;
        this.trackingNumber = "CM-TRK-" + (1000 + id);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTransferId() { return transferId; }
    public void setTransferId(int transferId) { this.transferId = transferId; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }

    public String getDispatchDate() { return dispatchDate; }
    public void setDispatchDate(String dispatchDate) { this.dispatchDate = dispatchDate; }

    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getSourceBranchName() { return sourceBranchName; }
    public void setSourceBranchName(String sourceBranchName) { this.sourceBranchName = sourceBranchName; }

    public String getDestBranchName() { return destBranchName; }
    public void setDestBranchName(String destBranchName) { this.destBranchName = destBranchName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

