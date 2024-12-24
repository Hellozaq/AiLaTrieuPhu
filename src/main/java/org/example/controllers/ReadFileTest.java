package org.example.controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

public class ReadFileTest {
    public static void main(String[] args) throws IOException {
        readExcel();

    }

    public static void readExcel() throws IOException {
        FileInputStream file = new FileInputStream("Ngân hàng câu hỏi Ai là triệu phú.xlsx");
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet=workbook.getSheetAt(0);  // doc tu sheet dau tien
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        for(Row row : sheet) {
            for(Cell cell : row) {
                switch (evaluator.evaluate(cell).getCellType()){
                    case CellType.STRING: {
                        System.out.println(cell.getStringCellValue());
                        break;
                    }
                    case CellType.NUMERIC:{
                        System.out.println(cell.getNumericCellValue());
                        break;
                    }

                }
            }
        }
        workbook.close();

    }
}
