package uk.co.objectivity.test.db.comparators.printer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class Excel {

    private final static Logger log = Logger.getLogger(Excel.class);

    public static void printFile(HSSFWorkbook workbook, String sheetName, File csvFile, String separator) {
        Pattern linePattern = Pattern.compile("\\Q" + separator + "\\E(?=(?:(?:[^\"]*\"){2})*[^\"]*$)");
        try {
            HSSFSheet sheet = workbook.createSheet(sheetName);
            ArrayList<ArrayList<String>> rows = getFileAsList(csvFile, linePattern);

            for (int k = 0; k < rows.size(); k++) {
                ArrayList<String> columns = rows.get(k);
                HSSFRow row = sheet.createRow((short) k);
                for (int p = 0; p < columns.size(); p++) {
                    HSSFCell cell = row.createCell((short) p);
                    cell.setCellValue(columns.get(p));
                }
            }

        } catch (Exception ex) {
            log.info(ex);
        }
    }

    public static ArrayList<ArrayList<String>> getFileAsList(File csvFile, Pattern linePattern) throws IOException {
        ArrayList<ArrayList<String>> rows = new ArrayList<>();
        String line;
        ArrayList<String> columnList;
        FileInputStream fis = new FileInputStream(csvFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        while ((line = br.readLine()) != null) {
            columnList = new ArrayList<>();
            String[] columns = linePattern.split(line, -1);
            for (int j = 0; j < columns.length; j++) {
                if (columns[j] != null && !columns[j].isEmpty()) {
                    columns[j] = columns[j].replaceAll("^\"|\"$", "").replaceAll("\"\"", "\"");
                } else {
                    columns[j] = null;
                }
                columnList.add(columns[j]);
            }
            rows.add(columnList);
        }
        fis.close();
        br.close();
        return rows;
    }

    public static void printString(HSSFWorkbook workbook, String sheetName, String stringToPrint) {
        try {
            HSSFCellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            HSSFSheet sheet = workbook.createSheet(sheetName);
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            row.setRowStyle(style);
            cell.setCellValue("\r\n" + stringToPrint + "\r\n");
            cell.setCellStyle(style);
        } catch (Exception ex) {
            log.info(ex);
        }
    }

    public static void printRow(HSSFSheet sheet, List<String> columns, ExcelStyle excelStyle) {
        HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        for (int i = 0; i < columns.size(); i++) {
            HSSFCell cell = row.createCell((short) i);
            cell.setCellValue(columns.get(i));
            if (excelStyle != null) {
                cell.setCellStyle(excelStyle.getStyle(sheet.getWorkbook()));
            }
        }
    }

    public static void printRow(HSSFSheet sheet, List<String> columns) {
        printRow(sheet, columns, null);
    }

    public static void printRows(HSSFSheet sheet, List<List<String>> rows) {
        rows.stream().forEach(c -> printRow(sheet, c));
    }

    public static void autoSizeColumns(Workbook workbook) {
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() > 0) {
                Row row = sheet.getRow(sheet.getFirstRowNum());
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    sheet.autoSizeColumn(columnIndex);
                }
            }
        }
    }

}
