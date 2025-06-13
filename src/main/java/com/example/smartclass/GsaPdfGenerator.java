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
        // Set document to landscape
        com.lowagie.text.Document document = new com.lowagie.text.Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
        document.open();

        PdfPTable mainTable = new PdfPTable(2);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{0.95f, 1.05f}); // Make both sides smaller
        mainTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        // Header: logo left, title right (no box, no table border)
        float logoX = document.left() + 10f;
        float logoY = document.top() - 60f;
        if (logoStream != null) {
            Image logo = Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(logoStream));
            logo.scaleToFit(60, 60);
            logo.setAbsolutePosition(logoX, logoY);
            document.add(logo);
        }
        // GSA title and auto-generated note (right side, above schedule, no box)
        Font gsaTitleFont = FontFactory.getFont("Arial", 12, Font.BOLD);
        Font autoFont = FontFactory.getFont("Arial", 9, Font.ITALIC, new Color(120,120,120));
        float gsaTextX = document.right() - 370f; // right side, above schedule
        float gsaTextY = document.top() - 20f;
        PdfContentByte cb = writer.getDirectContent();
        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase("Generated Schedule and Assessment (GSA)", gsaTitleFont), gsaTextX + 350f, gsaTextY, 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase("This document is automatically generated.", autoFont), gsaTextX + 350f, gsaTextY - 15f, 0);

        // --- LEFT SIDE: Info, Courses, Totals ---
        PdfPTable leftTable = new PdfPTable(1);
        leftTable.setWidthPercentage(100);
        leftTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        leftTable.getDefaultCell().setPadding(0f);

        // Student info (smaller font)
        // Draw student info title and line separator using directContent for precise control
        float leftX = document.left();
        float schedWidth = 370f, schedHeight = 300f;
        float schedX = document.right() - schedWidth - 5f; // move even further right
        float schedY = document.top() - schedHeight - 100f;
        float sectionTitleY = schedY + schedHeight + 18f; // Align with schedule visualization title
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("Student Information", FontFactory.getFont("Arial", 10, Font.BOLD)), leftX, sectionTitleY, 0);
        cb.setLineWidth(0.7f);
        cb.moveTo(leftX, sectionTitleY - 3f);
        cb.lineTo(leftX + schedWidth, sectionTitleY - 3f);
        cb.stroke();
        // Add some vertical space after drawing title and line
        leftTable.addCell(new Paragraph("\n\n"));

        // Info table
        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        Font infoLabelFont = FontFactory.getFont("Arial", 8, Font.BOLD);
        Font infoValueFont = FontFactory.getFont("Arial", 8);
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
        leftTable.addCell(infoTable);
        leftTable.addCell(new Paragraph(" "));

        // Courses section title and line separator
        float coursesY = sectionTitleY - 80f;
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("Courses", FontFactory.getFont("Arial", 10, Font.BOLD)), leftX, coursesY, 0);
        cb.setLineWidth(0.7f);
        cb.moveTo(leftX, coursesY - 3f);
        cb.lineTo(leftX + schedWidth, coursesY - 3f);
        cb.stroke();
        leftTable.addCell(new Paragraph(" "));

        // Courses table (smaller font)
        int tuitionPerUnit = 1250;
        leftTable.addCell(new Paragraph(" "));
        // Tuition per unit info (centered, with less spacing above and below)
        Paragraph tuitionInfo = new Paragraph("Tuition per unit: ₱" + tuitionPerUnit, FontFactory.getFont("Arial", 8));
        tuitionInfo.setAlignment(Element.ALIGN_CENTER);
        leftTable.addCell(tuitionInfo);
        leftTable.addCell(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        float[] columnWidths = {0.7f, 2f, 4f, 1.5f, 2.5f};
        table.setWidths(columnWidths);
        String[] headers = {"No.", "Code", "Name", "Units", "Tuition Fee"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont("Arial", 8, Font.BOLD)));
            cell.setBackgroundColor(new Color(220, 220, 220));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4f);
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
            table.addCell(new PdfPCell(new Phrase(String.valueOf(rowNum++), FontFactory.getFont("Arial", 8))));
            table.addCell(new PdfPCell(new Phrase(code, FontFactory.getFont("Arial", 8))));
            table.addCell(new PdfPCell(new Phrase(name, FontFactory.getFont("Arial", 8))));
            table.addCell(new PdfPCell(new Phrase(unitsStr, FontFactory.getFont("Arial", 8))));
            table.addCell(new PdfPCell(new Phrase("₱" + fee, FontFactory.getFont("Arial", 8))));
            totalUnits += units;
        }
        leftTable.addCell(table);
        // Add line separator as a cell with margin after courses table
        // Totals (smaller font)
        int tuitionTotal = totalUnits * tuitionPerUnit;
        int otherFeesTotal = 0;
        StringBuilder miscFeesStr = new StringBuilder();
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
                miscFeesStr.append(String.format("%-20s ₱%s\n", feeName + ":", amountStr));
                otherFeesTotal += amount;
            }
        } catch (Exception e) {
            miscFeesStr.append("(No miscellaneous fees found)\n");
        }
        int grandTotal = tuitionTotal + otherFeesTotal;
        Font valueFont = FontFactory.getFont("Arial", 7);
        // Right-align tuition and misc fees using a table for perfect alignment
        PdfPTable feesTable = new PdfPTable(1);
        feesTable.setWidthPercentage(100);
        PdfPCell tuitionLineCell = new PdfPCell(new Phrase(String.format("Tuition Fee: ₱%s", tuitionTotal), valueFont));
        tuitionLineCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tuitionLineCell.setBorder(Rectangle.NO_BORDER);
        feesTable.addCell(tuitionLineCell);
        PdfPCell miscFeesLineCell = new PdfPCell(new Phrase(miscFeesStr.toString(), valueFont));
        miscFeesLineCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        miscFeesLineCell.setBorder(Rectangle.NO_BORDER);
        feesTable.addCell(miscFeesLineCell);
        leftTable.addCell(feesTable);
        // Add space before total
        // Boxed total, right-aligned, same font, not bigger
        PdfPCell totalBoxCell = new PdfPCell(new Phrase("Total:  ₱" + grandTotal, FontFactory.getFont("Arial", 8, Font.BOLD)));
        totalBoxCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalBoxCell.setBorder(Rectangle.BOX);
        totalBoxCell.setPadding(2f);
        totalBoxCell.setColspan(1);
        totalBoxCell.setBackgroundColor(new Color(240,240,240));
        PdfPTable totalBoxTable = new PdfPTable(1);
        totalBoxTable.setWidthPercentage(100);
        totalBoxTable.addCell(totalBoxCell);
        leftTable.addCell(totalBoxTable);

        // --- RIGHT SIDE: Schedule Visualization ---
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        // Draw schedule grid
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        int startHour = 7, endHour = 21;
        int numRows = endHour - startHour;
        float cellW = schedWidth / (days.length + 1);
        float cellH = schedHeight / (numRows + 1);
        // Draw header row
        for (int d = 0; d < days.length; d++) {
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(days[d], FontFactory.getFont("Arial", 8, Font.BOLD)), schedX + cellW * (d + 1) + cellW / 2, schedY + schedHeight - cellH / 1.5f, 0);
        }
        // Draw time labels (7:00 AM, 8:00 AM, ...)
        for (int h = 0; h < numRows; h++) {
            int hour = startHour + h;
            int displayHour = hour % 12 == 0 ? 12 : hour % 12;
            String ampm = hour < 12 ? "AM" : "PM";
            String timeLabel = String.format("%d:00 %s", displayHour, ampm);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(timeLabel, FontFactory.getFont("Arial", 7)), schedX + cellW / 2, schedY + schedHeight - cellH * (h + 1.5f), 0);
        }
        // Draw grid lines
        cb.setLineWidth(0.7f);
        for (int r = 0; r <= numRows; r++) {
            float y = schedY + schedHeight - cellH * (r + 1);
            cb.moveTo(schedX, y);
            cb.lineTo(schedX + schedWidth, y);
        }
        for (int c = 0; c <= days.length; c++) {
            float x = schedX + cellW * (c + 1);
            cb.moveTo(x, schedY);
            cb.lineTo(x, schedY + schedHeight);
        }
        cb.stroke();
        // Fill grid with enrolled courses (add room in label)
        int[][] cellCount = new int[numRows][days.length];
        for (String[] course : enrolledCourses) {
            if (course.length < 5) continue;
            String code = course[0];
            String section = course[3];
            String sched = course[4];
            if (sched == null || sched.isEmpty()) continue;
            String[] parts = sched.split(" ", 3);
            if (parts.length < 3) continue;
            String timeRange = parts[0];
            String daysStr = parts[2];
            String[] schedDays = daysStr.split("/");
            String[] times = timeRange.split("-");
            if (times.length != 2) continue;
            int start = Integer.parseInt(times[0].split(":")[0]);
            int end = Integer.parseInt(times[1].split(":")[0]);
            for (String d : schedDays) {
                d = d.trim();
                for (int col = 0; col < days.length; col++) {
                    if (days[col].equalsIgnoreCase(d)) {
                        for (int row = start - startHour; row < end - startHour; row++) {
                            if (row >= 0 && row < numRows) {
                                cellCount[row][col]++;
                            }
                        }
                    }
                }
            }
        }
        for (String[] course : enrolledCourses) {
            if (course.length < 5) continue;
            String code = course[0];
            String section = course[3];
            String sched = course[4];
            if (sched == null || sched.isEmpty()) continue;
            String[] parts = sched.split(" ", 3);
            if (parts.length < 3) continue;
            String timeRange = parts[0];
            String room = parts[1].replaceAll("[()]", "");
            String daysStr = parts[2];
            String[] schedDays = daysStr.split("/");
            String[] times = timeRange.split("-");
            if (times.length != 2) continue;
            int start = Integer.parseInt(times[0].split(":")[0]);
            int end = Integer.parseInt(times[1].split(":")[0]);
            for (String d : schedDays) {
                d = d.trim();
                for (int col = 0; col < days.length; col++) {
                    if (days[col].equalsIgnoreCase(d)) {
                        for (int row = start - startHour; row < end - startHour; row++) {
                            if (row >= 0 && row < numRows) {
                                float x = schedX + cellW * (col + 1);
                                float y = schedY + schedHeight - cellH * (row + 2);
                                cb.saveState();
                                if (cellCount[row][col] > 1) {
                                    cb.setColorFill(new Color(255, 204, 204));
                                } else {
                                    cb.setColorFill(new Color(224, 247, 250));
                                }
                                cb.rectangle(x + 1, y + 1, cellW - 2, cellH - 2);
                                cb.fill();
                                cb.restoreState();
                                String labelText = code + " (" + section + ")";
                                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(labelText, FontFactory.getFont("Arial", 6)), x + cellW / 2, y + cellH / 2 + 2, 0);
                                // Draw room just below the code/section, not at the very bottom
                                String roomLabel = room;
                                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(roomLabel, FontFactory.getFont("Arial", 6)), x + cellW / 2, y + cellH / 2 - 8, 0);
                            }
                        }
                    }
                }
            }
        }
        // Add a border around the schedule grid
        cb.setLineWidth(1.2f);
        cb.rectangle(schedX, schedY, schedWidth, schedHeight);
        cb.stroke();
        // Add a title above the schedule (smaller font)
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("Schedule Visualization", FontFactory.getFont("Arial", 10, Font.BOLD)), schedX, schedY + schedHeight + 18f, 0);
        // Add line separator with a gap below the title
        cb.setLineWidth(0.7f);
        cb.moveTo(schedX, schedY + schedHeight + 15f);
        cb.lineTo(schedX + schedWidth, schedY + schedHeight + 15f);
        cb.stroke();
        // --- Move main content (leftTable and rightCell) further down to avoid logo/header collision ---
        // Add vertical space before mainTable
        document.add(new Paragraph("\n\n\n\n")); // Add 4 newlines for spacing
        mainTable.addCell(leftTable);
        mainTable.addCell(rightCell);
        document.add(mainTable);
        document.close();
    }
}