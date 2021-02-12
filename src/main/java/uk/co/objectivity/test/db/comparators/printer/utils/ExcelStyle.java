package uk.co.objectivity.test.db.comparators.printer.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;

public enum ExcelStyle {

    HEADER, GREY, GREY_BORDER, RED, RED_BORDER, GREEN, GREEN_BORDER;

    private final static Logger log = Logger.getLogger(ExcelStyle.class);
    Map<HSSFWorkbook, Map<ExcelStyle, CellStyle>> styles = new HashMap<>();
    boolean colorsInitialized = false;

    public CellStyle getStyle(HSSFWorkbook workbook) {
        switch (this) {
            case HEADER:
                ensureStyleExists(workbook, HEADER);
                return styles.get(workbook).get(HEADER);
            case GREY:
                ensureStyleExists(workbook, GREY);
                return styles.get(workbook).get(GREY);
            case GREY_BORDER:
                ensureStyleExists(workbook, GREY_BORDER);
                return styles.get(workbook).get(GREY_BORDER);
            case RED:
                ensureStyleExists(workbook, RED);
                return styles.get(workbook).get(RED);
            case RED_BORDER:
                ensureStyleExists(workbook, RED_BORDER);
                return styles.get(workbook).get(RED_BORDER);
            case GREEN:
                ensureStyleExists(workbook, GREEN);
                return styles.get(workbook).get(GREEN);
            case GREEN_BORDER:
                ensureStyleExists(workbook, GREEN_BORDER);
                return styles.get(workbook).get(GREEN_BORDER);
        }
        return null;

    }

    private void ensureStyleExists(HSSFWorkbook workbook, ExcelStyle excelStyle) {
        if (!colorsInitialized) {
            replaceWorkbookColors(workbook);
            colorsInitialized = true;
        }
        if (styles.containsKey(workbook)) {
            if (!styles.get(workbook).containsKey(excelStyle)) {
                styles.get(workbook).put(excelStyle, createStyle(workbook, excelStyle));
            }
        } else {
            Map<ExcelStyle, CellStyle> styleMap = new HashedMap<>();
            styleMap.put(excelStyle, createStyle(workbook, excelStyle));
            styles.put(workbook, styleMap);
        }
    }

    private CellStyle createStyle(HSSFWorkbook workbook, ExcelStyle excelStyle) {
        CellStyle style = workbook.createCellStyle();
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        switch (excelStyle) {
            case HEADER:
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFont(getFont(workbook, IndexedColors.GREY_80_PERCENT, (short) 12, true));
                style.setBorderBottom(BorderStyle.DOUBLE);
                break;
            case GREY_BORDER:
                style.setBorderBottom(BorderStyle.THIN);
            case GREY:
                style.setFont(getFont(workbook, IndexedColors.GREY_50_PERCENT, (short) 9, false));
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                break;
            case RED_BORDER:
                style.setBorderBottom(BorderStyle.THIN);
            case RED:
                style.setFont(getFont(workbook, IndexedColors.RED, (short) 10, false));
                style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                break;
            case GREEN_BORDER:
                style.setBorderBottom(BorderStyle.THIN);
            case GREEN:
                style.setFont(getFont(workbook, IndexedColors.DARK_GREEN, (short) 10, false));
                style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
                break;
        }
        return style;
    }

    private HSSFFont getFont(HSSFWorkbook workbook, IndexedColors indexedColors, short size, boolean bold) {
        HSSFFont font = workbook.createFont();
        font.setFontHeightInPoints(size);
        font.setFontName("Arial");
        font.setColor(indexedColors.getIndex());
        font.setBold(bold);
        //        font.setItalic(false);
        return font;
    }

    private static void replaceWorkbookColors(HSSFWorkbook workbook) {
        setColor(workbook, IndexedColors.ROSE.getIndex(), (byte) 255, (byte) 199, (byte) 206);
        setColor(workbook, IndexedColors.RED.getIndex(), (byte) 156, (byte) 0, (byte) 6);
        setColor(workbook, IndexedColors.DARK_GREEN.getIndex(), (byte) 0, (byte) 97, (byte) 0);
        setColor(workbook, IndexedColors.GREEN.getIndex(), (byte) 198, (byte) 239, (byte) 206);
        setColor(workbook, IndexedColors.GREY_25_PERCENT.getIndex(), (byte) 237, (byte) 237, (byte) 237);
    }

    private static HSSFColor setColor(HSSFWorkbook workbook, short colorIndex, byte r, byte g, byte b) {
        HSSFPalette palette = workbook.getCustomPalette();
        HSSFColor hssfColor = null;
        try {
            hssfColor = palette.findColor(r, g, b);
            if (hssfColor == null) {
                palette.setColorAtIndex(colorIndex, r, g, b);
                hssfColor = palette.getColor(colorIndex);
            }
        } catch (Exception e) {
            log.error(e);
        }

        return hssfColor;
    }

}
