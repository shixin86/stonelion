
package com.xiaomi.stonelion.common;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TextFormatDemo {

    public static void main(String[] args) {
        testDecimalFormat();
        testSimpleDateFormat();
    }

    private static void testDecimalFormat() {
        String shortString = "00000";
        String intString = "00000000000000000000";
        String longString = "0000000000000000000000000000000000000000";
        String floatString = "00000000000000000000.00";
        String doubleString = "0000000000000000000000000000000000000000.00";

        DecimalFormat dFormat = new DecimalFormat(shortString);
        System.out.println(dFormat.format(123));

        dFormat = new DecimalFormat(intString);
        System.out.println(dFormat.format(123));

        dFormat = new DecimalFormat(longString);
        System.out.println(dFormat.format(123));

        dFormat = new DecimalFormat(floatString);
        System.out.println(dFormat.format(123.123));

        dFormat = new DecimalFormat(doubleString);
        System.out.println(dFormat.format(123.123));
    }

    private static void testSimpleDateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-kk:mm");
        System.out.println(format.format(new Date()));
        System.out.println(format.format(new Date().getTime()));
    }
}
