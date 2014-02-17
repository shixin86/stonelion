package com.xiaomi.stonelion.common;

import java.util.Calendar;

/**
 * Created by ishikin on 14-1-26.
 */
public class TestCalander {
    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        System.out.println(calendar.get(Calendar.YEAR));
        System.out.println(calendar.get(Calendar.MONTH + 1));
        System.out.println(calendar.get(Calendar.DAY_OF_MONTH));


    }
}
