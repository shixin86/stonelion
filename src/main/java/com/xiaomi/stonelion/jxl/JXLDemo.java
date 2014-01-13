package com.xiaomi.stonelion.jxl;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: shixin
 * Date: 1/9/14
 * Time: 3:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class JXLDemo {
    public static void main(String[] args) throws IOException, WriteException {
        writeExcel();
    }

    private static void writeExcel() throws IOException, WriteException {
        WritableWorkbook workbook = Workbook.createWorkbook(new File("/home/shixin/workspace/stonelion/test.xls"));
        WritableSheet sheet = workbook.createSheet("First Sheet", 0);

        Label label = new Label(1, 2, "haha");
        sheet.addCell(label);

        workbook.write();
        workbook.close();
    }
}
