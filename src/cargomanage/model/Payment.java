package cargomanage.model;

public class Payment {
    private int id;
    private String transactionId;
    private String receiptNo;
    private String paymentDate;
    private String partyName;
    private String partyType = "SUPPLIER";
    private String relatedType;
    private Integer relatedId;
    private String paymentMethod = "Bank Transfer";
    private String direction = "Outgoing";
    private double amount = 0.0;
    private String status = "Paid";
    private String referenceNo = "";
    private String description = "";
    private String recordedBy = "system";
    private String remarks = "";

    public Payment() {}

    public Payment(int id, String transactionId, String receiptNo, String paymentDate,
                   String partyName, String partyType, String relatedType, Integer relatedId,
                   String paymentMethod, String direction, double amount, String status,
                   String referenceNo, String description, String recordedBy, String remarks) {
        this.id = id;
        this.transactionId = transactionId;
        this.receiptNo = receiptNo;
        this.paymentDate = paymentDate;
        this.partyName = partyName;
        this.partyType = partyType;
        this.relatedType = relatedType;
        this.relatedId = relatedId;
        this.paymentMethod = paymentMethod;
        this.direction = direction;
        this.amount = amount;
        this.status = status;
        this.referenceNo = referenceNo;
        this.description = description;
        this.recordedBy = recordedBy;
        this.remarks = remarks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }

    public String getPartyType() { return partyType; }
    public void setPartyType(String partyType) { this.partyType = partyType; }

    public String getRelatedType() { return relatedType; }
    public void setRelatedType(String relatedType) { this.relatedType = relatedType; }

    public Integer getRelatedId() { return relatedId; }
    public void setRelatedId(Integer relatedId) { this.relatedId = relatedId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}

