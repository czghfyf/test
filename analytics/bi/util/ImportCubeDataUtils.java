package cn.bgotech.analytics.bi.util;

import cn.bgotech.analytics.bi.exception.BIException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by ChenZhigang on 2018/11/16.
 */
public class ImportCubeDataUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportCubeDataUtils.class);

    public static String excel2text(byte[] bytes) throws BIException {

        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        XSSFWorkbook workbook;

        try {
            workbook = new XSSFWorkbook(is);
        } catch (IOException e) {
            LOGGER.warn("excel文件格式错误");
            throw new BIException(e);
        }

        XSSFSheet sheet = workbook.getSheetAt(0);
        XSSFRow row = sheet.getRow(0);
        XSSFCell grid;
        String cellStr;

        StringBuilder plainText = new StringBuilder();

        int UniversalDimensionRoleSize = 0;

        for (int c = 0; ; c++) {
            if ((grid = row.getCell(c)) == null) {
                break;
            }
            cellStr = grid.toString();
            if (cellStr.startsWith("{") && cellStr.endsWith("}")) {
                plainText.append(cellStr);
            } else {
                UniversalDimensionRoleSize++;
                String[] tempStrArr = cellStr.split("#");
                plainText.append("[" + tempStrArr[0] + "]##@@" + tempStrArr[tempStrArr.length - 1] + "@@##");
            }
        }

        plainText.append("\n");

        for (int r = 1; ; r++) {
            if ((row = sheet.getRow(r)) == null) {
                break;
            }
            for (int c = 0; ; c++) {
                if ((grid = row.getCell(c)) == null) {
                    break;
                }
                if (c < UniversalDimensionRoleSize) {
                    plainText.append("(" + grid.toString() + ")");
                } else {
                    plainText.append("<" + grid.getRawValue() + ">");
                }
            }
            plainText.append("\n");
        }

        try {
            workbook.close();
        } catch (IOException e) {
            LOGGER.error("关闭Excel文件XSSFWorkbook对象报错：" + e.getMessage());
            throw new BIException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                LOGGER.error("关闭Excel文件ByteArrayInputStream对象报错：" + e.getMessage());
                throw new BIException(e);
            }
        }
        return plainText.toString();
    }
}
