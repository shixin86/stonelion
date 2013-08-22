
package com.xiaomi.stonelion.common;

public class ShiftDemo {
    public static void main(String[] args) {
        System.out.println(Integer.toBinaryString(6));
        System.out.println(Integer.toBinaryString(-6)); // 补码

        System.out.println(Integer.toBinaryString(6 >> 2));
        System.out.println(6 >> 2);

        System.out.println(Integer.toBinaryString(-6 >> 2));
        System.out.println(-6 >> 2);

        System.out.println(Integer.toBinaryString(-6 >>> 2));
        System.out.println(-6 >>> 2);
    }
}
