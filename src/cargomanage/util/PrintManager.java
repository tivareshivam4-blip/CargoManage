package cargomanage.util;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;

public class PrintManager {

    public static void printComponent(Component comp, String jobTitle) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName(jobTitle);

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            double scaleX = pageFormat.getImageableWidth() / comp.getWidth();
            double scaleY = pageFormat.getImageableHeight() / comp.getHeight();
            double scale = Math.min(1.0, Math.min(scaleX, scaleY));
            g2.scale(scale, scale);

            comp.paint(g2);
            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(comp, "Print error: " + e.getMessage(), "CargoManage - Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

