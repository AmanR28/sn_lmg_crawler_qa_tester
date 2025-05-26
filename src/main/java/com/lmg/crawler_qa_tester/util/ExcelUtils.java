package com.lmg.crawler_qa_tester.util;

import com.lmg.crawler_qa_tester.dto.comparator.CompareRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public final class ExcelUtils {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    private ExcelUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static Sheet createSheetWithHeader(Workbook wb, String name, String firstEnv, String secondEnv) {
        Sheet sheet = wb.createSheet(name);
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Path");
        header.createCell(1).setCellValue(firstEnv);
        header.createCell(2).setCellValue(secondEnv);
        header.createCell(3).setCellValue("Match");
        return sheet;
    }

    public static Sheet getOrCreateSheet(Workbook wb, String name, String firstEnv, String secondEnv) {
        Sheet existingSheet = wb.getSheet(name);
        if (existingSheet != null) {
            return existingSheet;
        }
        return createSheetWithHeader(wb, name, firstEnv, secondEnv);
    }

    public static void writeComparisonRow(Sheet sheet, String path, String val1, String val2) {
        int rowNum = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowNum);
        val1 = val1.replaceAll("\\s+", " ").trim();
        val2 = val2.replaceAll("\\s+", " ").trim();
        row.createCell(0).setCellValue(path);
        row.createCell(1).setCellValue(val1);
        row.createCell(2).setCellValue(val2);
        row.createCell(3).setCellValue(val1.equals(val2) ? "YES" : "NO");
    }

    public static String generateFileName(CompareRequest req) {
        String country = req.country();
        String concept = req.concept();
        String fromEnv = req.compareEnvFrom();
        String toEnv = req.compareEnvTo();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return String.format("%s_%s_%s_%s_%s.xlsx", country, concept,fromEnv,toEnv, timestamp);
    }

    public static void writeWorkbookToFile(Workbook wb, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            wb.write(fos);
        }
        log.info("Excel sheet written to {}", fileName);
    }
} 