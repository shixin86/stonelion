package com.xiaomi.stonelion.hadoop;

public class Validate {
    public static boolean isEmpty(String value){
        return null == value || value.isEmpty();
    }
    
    public static <T> boolean isEqualsToLength(T[] array, int length){
        if(null == array){
            return false;
        }
        return array.length == length;
    }
}
