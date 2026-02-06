package files;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/*
    * This class is used to read data from Excel file and return it as an ArrayList of Strings.
    * It takes three parameters: sheetName, columnName and testCaseName.
    * It first identifies the sheet by name, then identifies the column by name and finally identifies the row by test case name.
    * Once it identifies the row, it pulls all the data of that row and returns it as an ArrayList of Strings.
    * This class is used in the test class to get the data from Excel file and feed it into the test.
 */
public class ExcelReader {

    public static ArrayList<String> getData(String sheetName, String columnName, String testCaseName) throws IOException {

        FileInputStream fis = new FileInputStream("src/main/resources/TestData.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        int sheets = workbook.getNumberOfSheets();
        ArrayList<String> a = new ArrayList<>();
        for (int i = 0; i < sheets; i++) {
            if (workbook.getSheetName(i).equalsIgnoreCase(sheetName)) {

                XSSFSheet sheet = workbook.getSheetAt(i);
                // Identify Testcases column by scanning the entire 1st row
                Iterator<Row> rows = sheet.iterator();
                Row firstRow = rows.next();

                Iterator<Cell> cells = firstRow.cellIterator();

                while (cells.hasNext()) {
                    Cell cell = cells.next();
                    if (cell.getStringCellValue().equalsIgnoreCase(columnName)) {
                        int testCaseColumn = cell.getColumnIndex();
                        System.out.println(testCaseColumn);

                        // Once column is identified then scan entire testcase column to identify purchase testcase row
                        while (rows.hasNext()) {
                            Row currentRow = rows.next();
                            if (currentRow.getCell(testCaseColumn).getStringCellValue().equalsIgnoreCase(testCaseName)) {
                                // After you grab purchase testcase row = pull all the data of that row and feed into test
                                Iterator<Cell> cellValue = currentRow.cellIterator();
                                while (cellValue.hasNext()) {
                                    Cell c = cellValue.next();
                                    if (c.getCellType() == CellType.STRING) {
                                        a.add(c.getStringCellValue());
                                    } else {
                                        a.add(NumberToTextConverter.toText(c.getNumericCellValue()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return a;
    }
}

