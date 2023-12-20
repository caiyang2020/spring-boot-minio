package org.sensetimeframework.office.excel;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Getter
public class WbWrapper {
    private final Workbook workbook;

    public WbWrapper(Workbook workbook) {
        this.workbook = workbook;
    }

    public WbWrapper(InputStream inputStream, String fileType) {
        Workbook wb;
        try {
            wb = ExcelUtil.getWorkbook(inputStream, fileType);
        } catch (IOException e) {
            log.error("读取数据流出错！错误信息：" + e.getMessage());
            wb = null;
        }
        this.workbook = wb;
    }

    public WbWrapper(String filePath) {
        Workbook wb;
        try {
            wb = ExcelUtil.getWorkbook(filePath);
        } catch (IOException e) {
            log.error("读取数据流出错！错误信息：" + e.getMessage());
            wb = null;
        }
        this.workbook = wb;
    }
    public void setAutoFormulaRecalculation() {
        this.workbook.setForceFormulaRecalculation(true);
    }

    public <T> Sheet getSheet(T indexOrName) {
        if (this.workbook == null) {
            return null;
        }

        Sheet sheet = null;
        switch (indexOrName) {
            case String s -> sheet = this.workbook.getSheet(s);
            case Integer i -> {
                if (i >= this.workbook.getNumberOfSheets()) {
                    log.warn("Sheet index (" + i + ") is out of range " + this.workbook.getNumberOfSheets());
                } else {
                    sheet = this.workbook.getSheetAt(i);
                }
            }
            case null, default -> System.out.println("Unsupported data type: " + Objects.requireNonNull(indexOrName).getClass().getSimpleName());
        }

        return sheet;
    }

    public Iterator<Sheet> getSheetIterator() {
        return this.workbook.sheetIterator();
    }

    public <T> String getCellData(T indexOrName, int rowNum, int colNum) {
        Sheet sheet = getSheet(indexOrName);

        if (sheet != null) {
            return ExcelUtil.getCellData(sheet, rowNum, colNum);
        }

        return null;
    }

    public <T> T rowToEntity(T indexOrName, int rowNum, Class<T> clazz, List<String> fieldNames) {
        Sheet sheet = getSheet(indexOrName);
        return sheet == null? null : ExcelUtil.rowToEntity(sheet.getRow(rowNum), clazz, fieldNames);
    }

    public <T> List<T> rowsToEntityList(T indexOrName, int rowStartIndex, int rowEndIndex, Class<T> clazz, List<String> fieldNames) {
        Sheet sheet = getSheet(indexOrName);
        return sheet == null? null : ExcelUtil.rowsToEntityList(sheet, rowStartIndex, rowEndIndex, clazz, fieldNames);
    }

    public <T> List<T> rowsToEntityList(T indexOrName, int rowStartIndex, Class<T> clazz, List<String> fieldNames) {
        Sheet sheet = getSheet(indexOrName);
        return sheet == null? null : ExcelUtil.rowsToEntityList(sheet, rowStartIndex, sheet.getLastRowNum(), clazz, fieldNames);
    }

    public <T> List<T> rowsToEntityList(T indexOrName, Class<T> clazz, List<String> fieldNames) {
        Sheet sheet = getSheet(indexOrName);
        return sheet == null? null : ExcelUtil.rowsToEntityList(sheet, 0, sheet.getLastRowNum(), clazz, fieldNames);
    }

    public <T> List<T> rowsToEntityList(T indexOrName, Class<T> clazz) {
        Sheet sheet = getSheet(indexOrName);
        return sheet == null? null : ExcelUtil.rowsToEntityList(sheet, clazz);
    }

    public Sheet createSheet() {
        return this.workbook.createSheet();
    }

    public Sheet createSheet(String name) {
        return this.workbook.createSheet(name);
    }

    public <T> int getPhysicalNumberOfRows(T indexOrName) {
        return getSheet(indexOrName).getPhysicalNumberOfRows();
    }

    public <U,V> void writeToCell(U indexOrName, int rowNum, int colNum, V value) {
        ExcelUtil.writeToCell(getSheet(indexOrName), rowNum, colNum, value);
    }

    public <U,E> void writeToRow(U indexOrName, int rowNum, E entity, List<String> fieldNames) {
        ExcelUtil.writeToRow(getSheet(indexOrName), rowNum, entity, fieldNames);
    }

    public <U,E> void writeToSheetWithHeader(U indexOrName, List<E> entityList, List<String> fieldNames, List<String> headers) {
        ExcelUtil.writeToSheetWithHeader(getSheet(indexOrName), entityList, fieldNames, headers);
    }

    public <U,E> void writeToSheetWithHeader(U indexOrName, List<E> entityList, List<String> fieldNames) {
        ExcelUtil.writeToSheetWithHeader(getSheet(indexOrName), entityList, fieldNames);
    }

    public <U,E> void writeToSheetWithoutHeader(U indexOrName, List<E> entityList, List<String> fieldNames) {
        Sheet sheet = getSheet(indexOrName);
        if (sheet == null) {
            log.error("The sheet " + indexOrName + " is not exist!");
        } else {
            ExcelUtil.writeToSheetWithoutHeader(sheet, entityList, fieldNames);
        }
    }

    public CellStyle createCellStyle() {
        return workbook.createCellStyle();
    }

    public <T> void setCellStyle(T indexOrName, int rowNum, int colNum, CellStyle cellStyle) {
        ExcelUtil.setCellStyle(getSheet(indexOrName), rowNum, colNum, cellStyle);
    }

    public void writeToFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            log.warn(filePath + "文件已存在,重新写入将覆盖该文件！");
        } else {
            if (!file.getParentFile().exists()) {
                FileUtils.forceMkdirParent(file);
            }

            boolean created = file.createNewFile();

            if (!created) {
                log.error(filePath + "创建文件失败！");
                throw new RuntimeException();
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
            workbook.close();
        }
    }
}
