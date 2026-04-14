package com.tydic.filehub.service.impl;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileFormatParser {

    /**
     * Parse a file into rows based on format.
     * Each row is a LinkedHashMap<String, Object> where key=column index (0,1,2...)
     */
    public List<Map<String, Object>> parseFile(String format, InputStream inputStream,
                                               String splitLabel, int skipHeaderLines) throws Exception {
        return switch (format != null ? format.toUpperCase() : "CSV") {
            case "XLS", "XLSX" -> parseExcel(inputStream, skipHeaderLines);
            default -> parseCsvText(inputStream, splitLabel, skipHeaderLines);
        };
    }

    private List<Map<String, Object>> parseCsvText(InputStream inputStream, String splitLabel,
                                                    int skipHeaderLines) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            for (int i = 0; i < skipHeaderLines; i++) {
                if (reader.readLine() == null) {
                    return rows;
                }
            }
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cells = line.split(splitLabel, -1);
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < cells.length; i++) {
                    row.put(String.valueOf(i), cells[i]);
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private List<Map<String, Object>> parseExcel(InputStream inputStream, int skipHeaderLines) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = 0;
            for (Row row : sheet) {
                if (rowNum < skipHeaderLines) {
                    rowNum++;
                    continue;
                }
                Map<String, Object> rowData = new LinkedHashMap<>();
                for (Cell cell : row) {
                    rowData.put(String.valueOf(cell.getColumnIndex()), getCellValue(cell));
                }
                rows.add(rowData);
                rowNum++;
            }
        }
        return rows;
    }

    private Object getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num) && !Double.isInfinite(num)) {
                    yield String.valueOf((long) num);
                }
                yield String.valueOf(num);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> "";
        };
    }

    /**
     * Write rows to a file in the specified format.
     * Each row is a Map<String, Object> where value order follows insertion order.
     */
    public void writeFile(String format, File outputFile, String splitLabel,
                          List<Map<String, Object>> rows) throws Exception {
        switch (format != null ? format.toUpperCase() : "CSV") {
            case "XLS" -> writeExcelHssf(outputFile, rows);
            case "XLSX" -> writeExcelXssf(outputFile, rows);
            default -> writeCsvText(outputFile, splitLabel, rows);
        }
    }

    private void writeCsvText(File outputFile, String splitLabel,
                              List<Map<String, Object>> rows) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            for (Map<String, Object> row : rows) {
                StringBuilder line = new StringBuilder();
                boolean first = true;
                for (Object value : row.values()) {
                    if (!first) {
                        line.append(splitLabel);
                    }
                    if (value != null) {
                        line.append(value.toString().replaceAll("[\\r\\n]", ""));
                    }
                    first = false;
                }
                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    private void writeExcelHssf(File outputFile, List<Map<String, Object>> rows) throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            writeExcelSheet(workbook, rows);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        }
    }

    private void writeExcelXssf(File outputFile, List<Map<String, Object>> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            writeExcelSheet(workbook, rows);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        }
    }

    private void writeExcelSheet(Workbook workbook, List<Map<String, Object>> rows) {
        Sheet sheet = workbook.createSheet("Sheet1");
        int rowNum = 0;
        for (Map<String, Object> row : rows) {
            Row excelRow = sheet.createRow(rowNum++);
            int colNum = 0;
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                Cell cell = excelRow.createCell(colNum);
                Object value = entry.getValue();
                if (value == null) {
                    cell.setBlank();
                } else if (value instanceof Number num) {
                    cell.setCellValue(num.doubleValue());
                } else {
                    cell.setCellValue(value.toString());
                }
                colNum++;
            }
        }
    }
}
