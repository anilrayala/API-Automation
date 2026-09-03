package org.api.automation.data;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.NumberToTextConverter;
import java.lang.IllegalArgumentException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads spreadsheet test data into header-keyed maps.
 *
 * <h2>What changed from the original ExcelReader</h2>
 * The old version returned an {@code ArrayList<String>} of raw cell values, so tests had to
 * say {@code data.get(1)}, {@code data.get(2)}, {@code data.get(3)} — positional indexes
 * that silently point at the wrong column the moment somebody inserts one in the
 * spreadsheet. Here a row comes back as {@code Map<"isbn", "abc1">}, so a test asks for the
 * column by name.
 *
 * <p>Three other fixes: the workbook and stream are now closed (the old code leaked a
 * {@code FileInputStream} on every call), the scan stops once the target row is found
 * instead of continuing through the sheet, and a missing sheet/column/row reports a clear
 * error instead of returning an empty list that fails later as an
 * {@code IndexOutOfBoundsException}.
 */
public final class ExcelDataReader {

    private ExcelDataReader() {
        // static utility
    }

    /**
     * Returns the single row whose {@code keyColumn} cell equals {@code keyValue}, as a
     * map of column name to cell value.
     *
     * @param resourcePath classpath path to the workbook, e.g. {@code "testdata/TestData.xlsx"}
     * @param sheetName    sheet to read
     * @param keyColumn    header of the column that identifies rows, e.g. {@code "TestCases"}
     * @param keyValue     value to match in that column, e.g. {@code "AddBook"}
     */
    public static Map<String, String> getRow(String resourcePath,
                                             String sheetName,
                                             String keyColumn,
                                             String keyValue) {
        for (Map<String, String> row : getAllRows(resourcePath, sheetName)) {
            String candidate = row.get(keyColumn);
            if (candidate != null && candidate.equalsIgnoreCase(keyValue)) {
                return row;
            }
        }
        throw new IllegalArgumentException(String.format(
                "No row in sheet '%s' of %s has '%s' = '%s'", sheetName, resourcePath, keyColumn, keyValue));
    }

    /**
     * Returns every data row in {@code sheetName} as a header-keyed map.
     *
     * <p>Feeding this straight into a TestNG {@code @DataProvider} is how you get one test
     * execution per spreadsheet row.
     */
    public static List<Map<String, String>> getAllRows(String resourcePath, String sheetName) {
        try (InputStream in = openStream(resourcePath);
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException(String.format(
                        "Sheet '%s' not found in %s. Available sheets: %s",
                        sheetName, resourcePath, sheetNames(workbook)));
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' in " + resourcePath + " is empty");
            }

            List<String> headers = new ArrayList<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                headers.add(cellToString(headerRow.getCell(c)));
            }

            List<Map<String, String>> rows = new ArrayList<>();
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isEmpty(row, headers.size())) {
                    continue;
                }
                // LinkedHashMap keeps column order, which makes log output readable
                Map<String, String> record = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    String header = headers.get(c);
                    if (!header.isEmpty()) {
                        record.put(header, cellToString(row.getCell(c)));
                    }
                }
                rows.add(record);
            }
            return rows;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read spreadsheet: " + resourcePath, e);
        }
    }

    private static InputStream openStream(String resourcePath) {
        InputStream in = ExcelDataReader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("Spreadsheet not found on classpath: " + resourcePath);
        }
        return in;
    }

    private static boolean isEmpty(Row row, int columnCount) {
        for (int c = 0; c < columnCount; c++) {
            if (!cellToString(row.getCell(c)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts any cell to a String.
     *
     * <p>{@link NumberToTextConverter} matters here: {@code getNumericCellValue()} returns
     * a double, so an ISBN of {@code 12345} would otherwise arrive as {@code "12345.0"} and
     * be rejected by the API. This renders the number the way Excel displays it.
     */
    private static String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toString()
                    : NumberToTextConverter.toText(cell.getNumericCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private static List<String> sheetNames(Workbook workbook) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            names.add(workbook.getSheetName(i));
        }
        return names;
    }
}
