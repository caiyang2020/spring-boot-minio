package org.sensetimeframework.office.excel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class ExcelUtil {
    public static final String XLS = "xls";
    public static final String XLSX = "xlsx";
    public static final String CELL_VALUE_ERROR = "#VALUE!";

    /**
     * 根据文件后缀名类型获取对应的工作簿对象
     * @param inputStream 读取文件的输入流
     * @param fileType 文件后缀名类型（xls或xlsx）
     * @return 包含文件数据的工作簿对象
     */
    public static Workbook getWorkbook(InputStream inputStream, String fileType) throws IOException {
        Workbook workbook = null;
        if (XLS.equalsIgnoreCase(fileType)) {
            workbook = new HSSFWorkbook(inputStream);
        } else if (XLSX.equalsIgnoreCase(fileType)) {
            workbook = new XSSFWorkbook(inputStream);
        }
        return workbook;
    }

    /**
     * 根据文件后缀名类型获取对应的工作簿对象
     * @param filePath 读取文件的路径
     * @return 包含文件数据的工作簿对象
     */
    public static Workbook getWorkbook(String filePath) throws IOException {
        File excelFile = new File(filePath);
        if (!excelFile.exists()) {
            return null;
        }
        return getWorkbook(new FileInputStream(excelFile), filePath.substring(filePath.lastIndexOf('.') + 1));
    }

    /**
     * 根据文件后缀名类型创建对应的工作簿对象
     * @param fileType 文件后缀名类型（xls或xlsx）
     * @return 新的工作簿对象
     */
    public static Workbook createWorkbook(String fileType) {
        Workbook workbook = null;
        if (XLS.equalsIgnoreCase(fileType)) {
            workbook = new HSSFWorkbook();
        } else if (XLSX.equalsIgnoreCase(fileType)) {
            workbook = new XSSFWorkbook();
        }
        return workbook;
    }

    /**
     * 根据cell对象获取单元格的值
     * @param cell cell对象
     * @return 单元格的值
     */
    public static String getCellData(Cell cell) {
        String cellData;

        switch (cell.getCellType()) {
            case NUMERIC -> cellData = String.valueOf(cell.getNumericCellValue());
            case STRING -> cellData = cell.getStringCellValue();
            case BOOLEAN -> cellData = String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                CellValue evaluatedCell = evaluator.evaluate(cell);
                boolean isNumeric = evaluatedCell.getCellType() == CellType.NUMERIC;
                cellData = isNumeric? String.valueOf(evaluatedCell.getNumberValue()) : evaluatedCell.getStringValue();
            }
            case ERROR -> cellData = CELL_VALUE_ERROR;
            case null, default -> cellData = "";
        }

        return cellData;
    }

    /**
     * 根据sheet对象以及rowNum、colNum获取单元格的值
     * @param sheet sheet对象
     * @param rowNum 行数
     * @param colNum 列数
     * @return 单元格的值
     */
    public static String getCellData(Sheet sheet, int rowNum, int colNum) {
        return getCellData(sheet.getRow(rowNum).getCell(colNum));
    }

    /**
     * 根据row对象，通过属性映射，生成指定实体类
     * @param row row对象
     * @param clazz 要转换成的类型
     * @param fieldNames 属性名
     * @return 映射成的实体对象
     */
    public static <T> T rowToEntity(Row row, Class<T> clazz, List<String> fieldNames) {
        Iterator<Cell> cellIterator = row.cellIterator();

        JsonObject jo = new JsonObject();
        for (String fieldName : fieldNames) {
            String cellData;
            if (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                cellData = getCellData(cell);
            } else {
                cellData = "";
            }
            jo.addProperty(fieldName, cellData);
        }

        return new Gson().fromJson(jo, clazz);
    }

    /**
     * 根据row列表，通过属性映射，生成指定实体类的列表
     * @param rowList row列表
     * @param clazz 要转换成的类型
     * @param fieldNames 属性名
     * @return 映射成的实体对象的列表
     */
    public static <T> List<T> rowsToEntityList(List<Row> rowList, Class<T> clazz, List<String> fieldNames) {
        List<T> resultList = new ArrayList<>();

        for (Row row : rowList) {
            T t = rowToEntity(row, clazz, fieldNames);
            resultList.add(t);
        }

        return resultList;
    }

    /**
     * 根据sheet对象指定起始和结束行index(从0开始)，通过属性映射，生成指定实体类的列表
     * @param sheet sheet对象
     * @param rowStartIndex 起始行的index
     * @param rowEndIndex 结束行的index
     * @param clazz 要转换成的类型
     * @param fieldNames 属性名
     * @return 映射成的实体对象的列表
     */
    public static <T> List<T> rowsToEntityList(Sheet sheet, int rowStartIndex, int rowEndIndex, Class<T> clazz, List<String> fieldNames) {
        List<T> resultList = new ArrayList<>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        int rowNum = 0;

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            if (rowNum >= rowStartIndex && rowNum <= rowEndIndex) {
                resultList.add(rowToEntity(row, clazz, fieldNames));
            }
            rowNum++;
        }

        return resultList;
    }

    /**
     * 根据sheet对象的全部row，第一行作为映射的fieldNames(需要与java对象的属性名保持完全一致)，
     * 剩下行作为生成实体类对象的主体，通过属性映射，生成指定实体类的列表
     * @param sheet sheet对象
     * @param clazz 要转换成的类型
     * @return 映射成的实体对象的列表
     */
    public static <T> List<T> rowsToEntityList(Sheet sheet, Class<T> clazz) {
        Row header = sheet.getRow(0);
        Iterator<Cell> headerIterator = header.cellIterator();
        List<String> fieldNames = new ArrayList<>();
        while(headerIterator.hasNext()) {
            fieldNames.add(getCellData(headerIterator.next()));
        }
        int lastRowNum = sheet.getLastRowNum();

        return rowsToEntityList(sheet, 1, lastRowNum, clazz, fieldNames);
    }

    /**
     * 在指定sheet对象的指定地址的cell里填入给定的value
     * @param sheet sheet对象
     * @param rowNum 指定单元格的行index
     * @param colNum 指定单元格的列index
     * @param value 要填入的值
     */
    public static <T> void writeToCell(Sheet sheet, int rowNum, int colNum, T value) {
        Row row;
        Cell cell;
        if ((row = sheet.getRow(rowNum)) == null) {
            row = sheet.createRow(rowNum);
        }

        if ((cell = row.getCell(colNum)) == null) {
            cell = row.createCell(colNum);
        }

        switch (value) {
            case null -> cell.setCellValue("");
            case String s -> cell.setCellValue(s);
            case Number n ->  cell.setCellValue(n.doubleValue());
            case Boolean b ->  cell.setCellValue(b);
            case Date d ->  cell.setCellValue(d);
            case LocalDateTime ldt ->  cell.setCellValue(ldt);
            case LocalDate ld ->  cell.setCellValue(ld);
            case Calendar c ->  cell.setCellValue(c);
            case RichTextString lts -> cell.setCellValue(lts);
            default -> cell.setCellValue(value.toString());
        }
    }

    /**
     * 在指定sheet对象的指定行上，按照fieldNames的顺序，将实体entity的对应属性依次填入改行的单元格中(没有则填入空值)
     * @param sheet sheet对象
     * @param rowNum 指定单元格的行index
     * @param entity 要填入的实体对象
     * @param fieldNames 实体对象属性名列表
     */
    public static <E> void writeToRow(Sheet sheet, int rowNum, E entity, List<String> fieldNames) {
        try {
            Map<String, Object> fieldMap = PropertyUtils.describe(entity);

            if (sheet.getRow(rowNum) == null) {
                sheet.createRow(rowNum);
            }

            int colNum = 0;

            for (String fieldName : fieldNames) {
                writeToCell(sheet, rowNum++, colNum, fieldMap.get(fieldName));
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("读取java对象为属性map时出错！错误信息：" + e.getMessage());
        }
    }

    /**
     * 在指定sheet对象的指定行上，按照fieldNames的顺序，逐行将实体列表entityList的对应属性依次填入改行的单元格中(没有则填入空值)，并在sheet的第一行依次写入header的元素作为表头
     * @param sheet sheet对象
     * @param entityList 要填入的实体对象列表
     * @param fieldNames 实体对象属性名列表
     * @param headers 表头列表
     */
    public static <E> void writeToSheetWithHeader(Sheet sheet, List<E> entityList, List<String> fieldNames, List<String> headers) {
        if (sheet == null) {
            log.error("The sheet object is null!");
        } else {
            for (int i = 0;i < headers.size(); i++) {
                writeToCell(sheet, 0, i, headers.get(i));
            }

            int rowNum = 1;

            for (E e : entityList) {
                writeToRow(sheet, rowNum++, e, fieldNames);
            }
        }
    }

    /**
     * 在指定sheet对象的指定行上，按照fieldNames的顺序，逐行将实体列表entityList的对应属性依次填入改行的单元格中(没有则填入空值)，并在sheet的第一行依次写入该实体的属性名作为表头
     * @param sheet sheet对象
     * @param entityList 要填入的实体对象列表
     * @param fieldNames 实体对象属性名列表
     */
    public static <E> void writeToSheetWithHeader(Sheet sheet, List<E> entityList, List<String> fieldNames) {
        writeToSheetWithHeader(sheet, entityList, fieldNames, fieldNames);
    }

    /**
     * 在指定sheet对象的指定行上，按照fieldNames的顺序，逐行将实体列表entityList的对应属性依次填入改行的单元格中(没有则填入空值)，无表头
     * @param sheet sheet对象
     * @param entityList 要填入的实体对象列表
     * @param fieldNames 实体对象属性名列表
     */
    public static <E> void writeToSheetWithoutHeader(Sheet sheet, List<E> entityList, List<String> fieldNames) {
        if (sheet == null) {
            log.error("The sheet object is null!");
        } else {
            int rowNum = 0;

            for (E e : entityList) {
                writeToRow(sheet, rowNum++, e, fieldNames);
            }
        }
    }

    /**
     * 在指定sheet对象的指定行上，设置指定单元格的样式
     * @param sheet sheet对象
     * @param rowNum 指定单元格的行index
     * @param colNum 指定单元格的列index
     * @param cellStyle 单元格样式
     */
    public static void setCellStyle(Sheet sheet, int rowNum, int colNum, CellStyle cellStyle) {
        sheet.getRow(rowNum).getCell(colNum).setCellStyle(cellStyle);
    }
}
