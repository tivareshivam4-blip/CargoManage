package cargomanage.util;

import cargomanage.model.Payment;
import cargomanage.model.Transfer;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;

public class PdfGenerator {

    public static void generatePaymentReceiptPdf(Payment p, File outputFile) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();

        // Colors
        java.awt.Color navy = new java.awt.Color(23, 55, 86);
        java.awt.Color gray = new java.awt.Color(100, 116, 139);
        java.awt.Color border = new java.awt.Color(226, 232, 240);
        java.awt.Color green = new java.awt.Color(22, 163, 74);

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, navy);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, gray);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, navy);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, gray);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, java.awt.Color.BLACK);
        Font amountFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, navy);
        Font badgeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, green);

        // Company Header
        Paragraph compName = new Paragraph("CARGOMANAGE ENTERPRISE MANAGEMENT", titleFont);
        compName.setAlignment(Element.ALIGN_CENTER);
        document.add(compName);

        Paragraph compSub = new Paragraph("Enterprise Warehouse, Inventory & Business Management System\nManage. Track. Control.\nOfficial Payment Receipt\n\n", subTitleFont);
        compSub.setAlignment(Element.ALIGN_CENTER);
        document.add(compSub);

        // Meta Info Table
        Table metaTable = new Table(2);
        metaTable.setWidth(100);
        metaTable.setBorderWidth(1);
        metaTable.setBorderColor(border);
        metaTable.setPadding(6);

        Cell c1 = new Cell(new Phrase("RECEIPT NUMBER:\n" + p.getReceiptNo(), sectionFont));
        c1.setBorderColor(border);
        metaTable.addCell(c1);

        Cell c2 = new Cell(new Phrase("TRANSACTION ID:\n" + p.getTransactionId(), sectionFont));
        c2.setBorderColor(border);
        metaTable.addCell(c2);

        document.add(metaTable);
        document.add(new Paragraph(" "));

        // Details Table
        Table details = new Table(2);
        details.setWidth(100);
        details.setBorderWidth(1);
        details.setBorderColor(border);
        details.setPadding(6);

        addTableRow(details, "Payment Date & Time", p.getPaymentDate(), labelFont, valueFont, border);
        addTableRow(details, "Customer / Supplier Name", p.getPartyName(), labelFont, valueFont, border);
        addTableRow(details, "Party Type", p.getPartyType(), labelFont, valueFont, border);
        addTableRow(details, "Payment Direction", p.getDirection(), labelFont, valueFont, border);
        addTableRow(details, "Payment Method", p.getPaymentMethod(), labelFont, valueFont, border);
        addTableRow(details, "Reference / UTR Number", p.getReferenceNo() != null && !p.getReferenceNo().isBlank() ? p.getReferenceNo() : "N/A", labelFont, valueFont, border);
        if (p.getRelatedType() != null && !p.getRelatedType().isEmpty()) {
            addTableRow(details, "Related Operation", p.getRelatedType() + " #" + (p.getRelatedId() != null ? p.getRelatedId() : "-"), labelFont, valueFont, border);
        }
        addTableRow(details, "Description", p.getDescription(), labelFont, valueFont, border);
        addTableRow(details, "Payment Status", p.getStatus().toUpperCase(), labelFont, badgeFont, border);

        document.add(details);
        document.add(new Paragraph(" "));

        // Total Amount Box
        Table amountTable = new Table(1);
        amountTable.setWidth(100);
        amountTable.setBorderWidth(1);
        amountTable.setBorderColor(navy);
        amountTable.setPadding(10);

        Cell amountCell = new Cell();
        amountCell.setBackgroundColor(new java.awt.Color(244, 247, 251));
        amountCell.add(new Paragraph("TOTAL AMOUNT SETTLED:", labelFont));
        amountCell.add(new Paragraph(String.format("INR %.2f", p.getAmount()), amountFont));
        amountTable.addCell(amountCell);
        document.add(amountTable);

        // Signatures / Footer
        Paragraph footer = new Paragraph("\n\n\nAuthorized Accountant / Recorded By: " + p.getRecordedBy() +
                "\nSystem Timestamp: " + p.getPaymentDate() +
                "\n\nThank you for choosing CargoManage Enterprise Logistics.", subTitleFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
    }

    public static void generateTransferDispatchPdf(Transfer t, File outputFile) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();

        java.awt.Color navy = new java.awt.Color(23, 55, 86);
        java.awt.Color gray = new java.awt.Color(100, 116, 139);
        java.awt.Color border = new java.awt.Color(226, 232, 240);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, navy);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, gray);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, navy);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, gray);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, java.awt.Color.BLACK);

        Paragraph compName = new Paragraph("CARGOMANAGE ENTERPRISE LOGISTICS", titleFont);
        compName.setAlignment(Element.ALIGN_CENTER);
        document.add(compName);

        Paragraph compSub = new Paragraph("Enterprise Warehouse, Inventory & Business Management System\nManage. Track. Control.\nInter-Warehouse Stock Transfer & Dispatch Note\n\n", subTitleFont);
        compSub.setAlignment(Element.ALIGN_CENTER);
        document.add(compSub);

        Table metaTable = new Table(2);
        metaTable.setWidth(100);
        metaTable.setBorderWidth(1);
        metaTable.setBorderColor(border);
        metaTable.setPadding(6);

        Cell c1 = new Cell(new Phrase("TRANSFER ORDER ID: #" + t.getId(), sectionFont));
        c1.setBorderColor(border);
        metaTable.addCell(c1);

        Cell c2 = new Cell(new Phrase("STATUS: " + t.getStatus().toUpperCase(), sectionFont));
        c2.setBorderColor(border);
        metaTable.addCell(c2);

        document.add(metaTable);
        document.add(new Paragraph(" "));

        Table details = new Table(2);
        details.setWidth(100);
        details.setBorderWidth(1);
        details.setBorderColor(border);
        details.setPadding(6);

        addTableRow(details, "Source Warehouse", t.getSourceBranchName(), labelFont, valueFont, border);
        addTableRow(details, "Destination Warehouse", t.getDestBranchName(), labelFont, valueFont, border);
        addTableRow(details, "Product Name", t.getProductName(), labelFont, valueFont, border);
        addTableRow(details, "Transfer Quantity", t.getQuantity() + " " + t.getProductUnit(), labelFont, valueFont, border);
        addTableRow(details, "Requested By", t.getRequestedBy() + " (" + t.getRequestDate() + ")", labelFont, valueFont, border);
        addTableRow(details, "Approved / Dispatched By", (t.getApprovedBy() != null ? t.getApprovedBy() : "Pending") +
                (t.getDispatchDate() != null ? " at " + t.getDispatchDate() : ""), labelFont, valueFont, border);
        addTableRow(details, "Received By", (t.getReceivedBy() != null ? t.getReceivedBy() : "Pending Confirmation") +
                (t.getReceivedDate() != null ? " at " + t.getReceivedDate() : ""), labelFont, valueFont, border);
        addTableRow(details, "Remarks", t.getRemarks() != null ? t.getRemarks() : "None", labelFont, valueFont, border);

        document.add(details);

        Paragraph signSection = new Paragraph("\n\n\nDispatch Supervisor Signature: _______________________      Receiving Supervisor Signature: _______________________", valueFont);
        signSection.setAlignment(Element.ALIGN_CENTER);
        document.add(signSection);

        document.close();
    }

    public static void generateReportPdf(String title, String content, File outputFile) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();

        java.awt.Color navy = new java.awt.Color(23, 55, 86);
        java.awt.Color gray = new java.awt.Color(100, 116, 139);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, navy);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, gray);
        Font codeFont = FontFactory.getFont(FontFactory.COURIER, 9, java.awt.Color.BLACK);

        Paragraph compName = new Paragraph("CARGOMANAGE ENTERPRISE REPORT", titleFont);
        compName.setAlignment(Element.ALIGN_CENTER);
        document.add(compName);

        Paragraph reportTitle = new Paragraph(title + "\nManage. Track. Control.  |  Generated on: " + java.time.LocalDateTime.now() + "\n\n", subTitleFont);
        reportTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(reportTitle);

        Paragraph text = new Paragraph(content, codeFont);
        document.add(text);

        document.close();
    }

    private static void addTableRow(Table table, String label, String value, Font labelFont, Font valFont, java.awt.Color border) throws BadElementException {
        Cell c1 = new Cell(new Phrase(label, labelFont));
        c1.setBorderColor(border);
        c1.setBackgroundColor(new java.awt.Color(248, 250, 252));
        Cell c2 = new Cell(new Phrase(value != null ? value : "-", valFont));
        c2.setBorderColor(border);
        table.addCell(c1);
        table.addCell(c2);
    }
}

