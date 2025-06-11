package com.example.smartclass;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;

import java.awt.*;
import java.io.*;
import java.util.List;

public class GsaPdfGenerator {
    public static void generateGsaPdf(String pdfPath, InputStream logoStream, String schoolName, String[] student, String term, List<String[]> enrolledCourses) throws Exception {
        com.lowagie.text.Document document = new com.lowagie.text.Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
        document.open();

        // Header: logo left, title right
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1.2f, 4f});
        PdfPCell logoBox = new PdfPCell();
        logoBox.setBorder(Rectangle.NO_BORDER);
        logoBox.setHorizontalAlignment(Element.ALIGN_LEFT);
        logoBox.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (logoStream != null) {
            Image logo = Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(logoStream));
            logo.scaleToFit(60, 60);
            logo.setAlignment(Image.ALIGN_LEFT);
            logoBox.addElement(logo);
        }
        headerTable.addCell(logoBox);

        // GSA title and auto-generated note (smaller)
        Font arialTitle = FontFactory.getFont("Arial", 12, Font.BOLD);
        Paragraph gsaTitle = new Paragraph("Generated Schedule and Assessment (GSA)", arialTitle);
        gsaTitle.setAlignment(Element.ALIGN_RIGHT);
        Font autoFont = FontFactory.getFont("Arial", 9, Font.ITALIC, new Color(120,120,120));
        Paragraph autoNote = new Paragraph("This document is automatically generated.", autoFont);
        autoNote.setAlignment(Element.ALIGN_RIGHT);
        PdfPCell titleCell = new PdfPCell();
        titleCell.addElement(gsaTitle);
        titleCell.addElement(autoNote);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(titleCell);
        document.add(headerTable);
        document.add(new Paragraph("\n"));

        // --- Student Information Section ---
        Paragraph studentSection = new Paragraph("Student Information", FontFactory.getFont("Arial", 12, Font.BOLD));
        studentSection.setSpacingAfter(4f);
        document.add(studentSection);
        LineSeparator beforeStudentSep = new LineSeparator();
        beforeStudentSep.setLineColor(new Color(120,120,120));
        beforeStudentSep.setLineWidth(1.2f);
        document.add(beforeStudentSep);
        document.add(new Paragraph("\n"));

        // Student info grid (Arial)
        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        Font infoLabelFont = FontFactory.getFont("Arial", 10, Font.BOLD);
        Font infoValueFont = FontFactory.getFont("Arial", 10);
        infoTable.addCell(new Phrase("Student Name:", infoLabelFont));
        infoTable.addCell(new Phrase(student[1], infoValueFont));
        infoTable.addCell(new Phrase("Student ID:", infoLabelFont));
        infoTable.addCell(new Phrase(student[0], infoValueFont));
        infoTable.addCell(new Phrase("Program:", infoLabelFont));
        infoTable.addCell(new Phrase(student[7], infoValueFont));
        infoTable.addCell(new Phrase("Year:", infoLabelFont));
        infoTable.addCell(new Phrase(student[8], infoValueFont));
        infoTable.addCell(new Phrase("Term:", infoLabelFont));
        infoTable.addCell(new Phrase(term, infoValueFont));
        for (int i = 0; i < 2; i++) infoTable.addCell("");
        document.add(infoTable);
        document.add(new Paragraph("\n"));
        LineSeparator afterStudentSep = new LineSeparator();
        afterStudentSep.setLineColor(new Color(120,120,120));
        afterStudentSep.setLineWidth(1.2f);
        document.add(afterStudentSep);
        document.add(new Paragraph("\n"));

        // --- Courses Section ---
        Paragraph coursesSection = new Paragraph("Courses", FontFactory.getFont("Arial", 12, Font.BOLD));
        coursesSection.setSpacingAfter(4f);
        document.add(coursesSection);
        // Tuition per unit info (right-aligned)
        int tuitionPerUnit = 1500;
        Paragraph tuitionInfo = new Paragraph("Tuition per unit: ₱" + tuitionPerUnit, FontFactory.getFont("Arial", 11));
        tuitionInfo.setAlignment(Element.ALIGN_RIGHT);
        tuitionInfo.setSpacingAfter(6f);
        document.add(tuitionInfo);
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5f);
        table.setSpacingAfter(10f);
        float[] columnWidths = {0.7f, 2f, 4f, 1.5f, 2.5f};
        table.setWidths(columnWidths);
        String[] headers = {"No.", "Code", "Name", "Units", "Tuition Fee"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont("Arial", 11, Font.BOLD)));
            cell.setBackgroundColor(new Color(220, 220, 220));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6f);
            cell.setBorderWidth(1f);
            table.addCell(cell);
        }
        int totalUnits = 0;
        int rowNum = 1;
        for (String[] course : enrolledCourses) {
            String code = course[0];
            String name = course[1];
            String unitsStr = (course.length > 2 ? course[2] : "0");
            int units = 0;
            try { units = Integer.parseInt(unitsStr.trim()); } catch (Exception ignored) {}
            int fee = units * tuitionPerUnit;
            table.addCell(new PdfPCell(new Phrase(String.valueOf(rowNum++), FontFactory.getFont("Arial", 10))));
            table.addCell(new PdfPCell(new Phrase(code, FontFactory.getFont("Arial", 10))));
            table.addCell(new PdfPCell(new Phrase(name, FontFactory.getFont("Arial", 10))));
            table.addCell(new PdfPCell(new Phrase(unitsStr, FontFactory.getFont("Arial", 10))));
            table.addCell(new PdfPCell(new Phrase("₱" + fee, FontFactory.getFont("Arial", 10))));
            totalUnits += units;
        }
        document.add(table);
        LineSeparator afterCoursesSep = new LineSeparator();
        afterCoursesSep.setLineColor(new Color(120,120,120));
        afterCoursesSep.setLineWidth(1.2f);
        document.add(afterCoursesSep);
        document.add(new Paragraph("\n"));

        // --- Other Fees Section ---
        // --- Totals Section ---
        Paragraph totalsSection = new Paragraph("Totals", FontFactory.getFont("Arial", 11, Font.BOLD));
        totalsSection.setAlignment(Element.ALIGN_RIGHT);
        totalsSection.setSpacingAfter(4f);
        document.add(totalsSection);
        Font valueFont = FontFactory.getFont("Arial", 9);
        int tuitionTotal = totalUnits * tuitionPerUnit;
        Paragraph tuitionLine = new Paragraph(String.format("%-20s ₱%s", "Tuition Fee:", tuitionTotal), valueFont);
        tuitionLine.setAlignment(Element.ALIGN_RIGHT);
        tuitionLine.setSpacingAfter(2f);
        document.add(tuitionLine);
        // Add miscellaneous fees (from CSV) after tuition fee
        int otherFeesTotal = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("miscellaneous.csv"))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                String feeName = parts[0].trim();
                String amountStr = parts[1].trim();
                int amount = 0;
                try { amount = Integer.parseInt(amountStr); } catch (Exception ignored) {}
                Paragraph feeLine = new Paragraph(String.format("%-20s ₱%s", feeName + ":", amountStr), valueFont);
                feeLine.setAlignment(Element.ALIGN_RIGHT);
                feeLine.setSpacingAfter(2f);
                document.add(feeLine);
                otherFeesTotal += amount;
            }
        } catch (Exception e) {
            Paragraph noFees = new Paragraph("(No miscellaneous fees found)", valueFont);
            noFees.setAlignment(Element.ALIGN_RIGHT);
            document.add(noFees);
        }
        // Boxed Total (smaller box, right-aligned, labeled 'Total')
        int grandTotal = tuitionTotal + otherFeesTotal;
        Paragraph totalBox = new Paragraph("Total:  ₱" + grandTotal, FontFactory.getFont("Arial", 11, Font.BOLD));
        totalBox.setAlignment(Element.ALIGN_RIGHT);
        totalBox.setSpacingBefore(6f);
        totalBox.setSpacingAfter(6f);
        PdfPCell totalCell = new PdfPCell();
        totalCell.addElement(totalBox);
        totalCell.setBorder(Rectangle.BOX);
        totalCell.setBorderWidth(1.2f);
        totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        totalCell.setPadding(2.5f);
        PdfPTable totalTable = new PdfPTable(1);
        totalTable.setWidthPercentage(18);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.addCell(totalCell);
        document.add(totalTable);
        document.close();
    }
}
