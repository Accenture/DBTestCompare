package uk.co.objectivity.test.db.comparators.printer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import uk.co.objectivity.test.db.beans.FileRow;
import uk.co.objectivity.test.db.beans.xml.Compare;
import uk.co.objectivity.test.db.beans.xml.Sql;
import uk.co.objectivity.test.db.comparators.printer.utils.Excel;
import uk.co.objectivity.test.db.comparators.printer.utils.ExcelStyle;
import uk.co.objectivity.test.db.comparators.results.DBResults;
import uk.co.objectivity.test.db.comparators.results.DBRow;

public class ExcelPrinter {
	private final static Logger log = Logger.getLogger(ExcelPrinter.class);

	private static String SHEET_FILE_NAME = "(1) FILE";
	private static String SHEET_FILE_SEL_COL_NAME = "(2) FILE_TRANSFORMED";
	private static String SHEET_FILE_AGGREGATED_NAME = "(3) FILE_AGGREGATED";
	private static String SHEET_SQL_NAME = "(4) DB_SQL";
	private static String SHEET_DB_NAME = "(5) DB_RESULTS";
	private static String SHEET_DIFF_NAME = "DIFFERENCES (3 vs 5)";
	private static String SHEET_MATCH_NAME = "MATCHES (3 and 5)";
	private static String SHEET_PO_CLOSED = "PO Closed";
	private static String SHEET_EVIDENCES = "EVIDENCES";

	private static int MAX_DB_RESULTS_IN_EXCEL = 30000;

	private HSSFWorkbook workbook;
	private File excelFile;
	private Compare compare;

	public ExcelPrinter(File excelFile, Compare compare) {
		workbook = new HSSFWorkbook();
		this.excelFile = excelFile;
		this.compare = compare;
	}

	public void saveExcelEvidence(Headers headers, File testFile, List<CompareResult> diffs,
			List<CompareResult> matches, List<CompareResult> poCmp, DBResults dbResults,
			Collection<FileRow> aggregatedFileRows, Map<String, String> testSetsMap, Integer[] grayedColumns) {

		ArrayList<Integer> grayedColumnsList = new ArrayList<Integer>(Arrays.asList(grayedColumns));
		Excel.printFile(workbook, SHEET_FILE_NAME, testFile, compare.getFile().getSeparator());
		printFileTransformationSheets(headers, aggregatedFileRows);

		printSQLQuery();

		printSQLResults(headers, dbResults);
		printCompareResultEvidence(SHEET_MATCH_NAME, headers, matches, testSetsMap, true, grayedColumnsList);
		printCompareResultEvidence(SHEET_DIFF_NAME, headers, diffs, testSetsMap, true, grayedColumnsList);
		if (poCmp.size() > 0) {
			printCompareResultEvidence(SHEET_PO_CLOSED, headers, poCmp, testSetsMap, true, grayedColumnsList);
		}

		orderExcelSheets(workbook);
		Excel.autoSizeColumns(workbook);
		save();
	}

	private void printSQLQuery() {
		HSSFSheet sheet = workbook.createSheet(SHEET_SQL_NAME);
		List<Sql> sqlList = compare.getSqls();
		Iterator<Sql> it = sqlList.iterator();
		while (it.hasNext()) {

			HSSFCellStyle style = workbook.createCellStyle();
			style.setWrapText(true);

			HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
			HSSFCell cell = row.createCell(1);
			row.setRowStyle(style);

			cell.setCellValue("\r\n" + it.next().getSql() + "\r\n");
			cell.setCellStyle(style);
		}
	}

	private void save() {
		try {
			FileOutputStream fileOut = new FileOutputStream(excelFile);
			workbook.write(fileOut);
			fileOut.flush();
			fileOut.close();
		} catch (IOException ioe) {
			log.error(ioe);
		}
	}

	private void printFileTransformationSheets(Headers headers, Collection<FileRow> aggregatedRows) {
		printRowsWithSelectedColumns(headers, aggregatedRows);
		printAggregatedRows(headers, aggregatedRows);
	}

	private void printRowsWithSelectedColumns(Headers headers, Collection<FileRow> aggregatedRows) {
		HSSFSheet selColsSheet = workbook.createSheet(SHEET_FILE_SEL_COL_NAME);
		printColumnsRow(workbook, selColsSheet, headers.getHeaders());
		aggregatedRows.iterator().forEachRemaining(ra -> Excel.printRows(selColsSheet, ra.getAllRows()));
	}

	private void printAggregatedRows(Headers headers, Collection<FileRow> fileRows) {
		HSSFSheet sheet = workbook.createSheet(SHEET_FILE_AGGREGATED_NAME);
		HSSFSheet finalSheet = sheet;
		printColumnsRow(workbook, sheet, headers.getHeaders());

		for (FileRow fr : fileRows) {
			if (fr.isAggregated()) {
				Excel.printRow(finalSheet, fr.getAggregatedRow(), ExcelStyle.GREEN);
				for (List<String> row : fr.getExcludedRows()) {
					Excel.printRow(finalSheet, row, null);
				}
			} else {
				for (List<String> row : fr.getAllRows()) {
					Excel.printRow(finalSheet, row, null);
				}
			}
		}
	}

	private void printSQLResults(Headers headers, DBResults dbResults) {
		HSSFSheet sheet = workbook.createSheet(SHEET_DB_NAME);
		printColumnsRow(workbook, sheet, headers.getHeaders());

		int maxResults = MAX_DB_RESULTS_IN_EXCEL + 1; // Limit due to
														// HSSFWorkbook rows
														// limit (~65K); +1
														// because of the
														// header;
		List<DBRow> allRows = dbResults.getAll();
		allRows.stream().limit(maxResults).forEach(dbRow -> Excel.printRow(sheet, dbRow.getRow()));
		if (allRows.size() > maxResults) {
			HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
			HSSFCell cell = row.createCell(1);
			cell.setCellValue("Rest of the DB results in the CSV file...");
		}
	}

	private void printCompareResultEvidence(String sheetName, Headers headers, List<CompareResult> results,
			Map<String, String> testSetsMap, boolean replaceDiff, ArrayList<Integer> ignoredColumns) {
		try {
			HSSFSheet sheet = workbook.createSheet(sheetName);
			printColumnsRow(workbook, sheet, headers.getExtendedHeaders());

			boolean includeTestSetColumn = testSetsMap != null;
			// Add file desc column to ignored
			if (!ignoredColumns.contains(0)) {
				ignoredColumns.add(0);
			}
			// Add testSet column to ignored
			if (includeTestSetColumn) {
				results.forEach(cr -> cr.setTestSets(testSetsMap));
				ignoredColumns.add(1);
			}

			results.forEach(cr -> {
				printRow(cr.getRowFile(), replaceDiff, sheet, false, ignoredColumns, cr.getTest());
				printRow(cr.getRowDB(), replaceDiff, sheet, true, ignoredColumns, cr.getTest());
			});
		} catch (Exception ex) {
			log.info(ex);
		}
	}

	private void printRow(List<String> columns, boolean replaceDiff, HSSFSheet sheet, boolean border,
			ArrayList<Integer> grayedColumns, CompareResult.Test test) {
		HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);

		for (int p = 0; p < columns.size(); p++) {
			HSSFCell cell = row.createCell((short) p);

			if (CompareResult.Test.CHECK_ALL_COLUMNS == test) {
				if (grayedColumns.contains(p)) {
					cell.setCellStyle(
							border ? ExcelStyle.GREY_BORDER.getStyle(workbook) : ExcelStyle.GREY.getStyle(workbook));
				} else if (replaceDiff && columns.get(p).contains("<DIFF>")) {
					String rmvDiffSrt = columns.get(p).replace("<DIFF>", "");
					columns.set(p, rmvDiffSrt);
					cell.setCellStyle(
							border ? ExcelStyle.RED_BORDER.getStyle(workbook) : ExcelStyle.RED.getStyle(workbook));
				} else {
					cell.setCellStyle(
							border ? ExcelStyle.GREEN_BORDER.getStyle(workbook) : ExcelStyle.GREEN.getStyle(workbook));
				}
				cell.setCellValue(columns.get(p));
			} else {
				if (grayedColumns.contains(p)) {
					cell.setCellStyle(
							border ? ExcelStyle.GREY_BORDER.getStyle(workbook) : ExcelStyle.GREY.getStyle(workbook));
				} else if (CompareResult.Test.PASSED == test) {
					cell.setCellStyle(
							border ? ExcelStyle.GREEN_BORDER.getStyle(workbook) : ExcelStyle.GREEN.getStyle(workbook));
				} else {
					cell.setCellStyle(
							border ? ExcelStyle.RED_BORDER.getStyle(workbook) : ExcelStyle.RED.getStyle(workbook));
				}
				cell.setCellValue(columns.get(p).replace("<DIFF>", ""));
			}
		}
	}

	private void printColumnsRow(HSSFWorkbook workbook, HSSFSheet sheet, List<String> columns) {
		HSSFRow row = sheet.createRow(sheet.getLastRowNum());
		for (int i = 0; i < columns.size(); i++) {
			HSSFCell cell = row.createCell((short) i);
			cell.setCellStyle(ExcelStyle.HEADER.getStyle(workbook));
			cell.setCellValue(columns.get(i));
		}
	}

	private void orderExcelSheets(Workbook workbook) {
		try {
			workbook.setSheetOrder(SHEET_FILE_NAME, 0);
			workbook.setSheetOrder(SHEET_FILE_SEL_COL_NAME, 1);
			workbook.setSheetOrder(SHEET_FILE_AGGREGATED_NAME, 2);
			workbook.setSheetOrder(SHEET_SQL_NAME, 3);
			workbook.setSheetOrder(SHEET_DB_NAME, 4);
			workbook.setSheetOrder(SHEET_MATCH_NAME, 5);
			workbook.setSheetOrder(SHEET_DIFF_NAME, 6);
			workbook.createSheet(SHEET_EVIDENCES);
			workbook.setSheetOrder(SHEET_EVIDENCES, 7);
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			log.warn(aiobe);
		}
	}

}
