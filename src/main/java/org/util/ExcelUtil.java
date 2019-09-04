package org.util;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

/**
 * @Author: super bao
 * @Date: 2019/9/3 20:19
 */
public class ExcelUtil {

    private static final String EMPTY = "";

    public static String getStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case NUMERIC:
                return Double.toString(cell.getNumericCellValue());
            case STRING:
                return cell.getStringCellValue();
            case BLANK:
                return EMPTY;
            default:
                return null;
        }
    }
}
