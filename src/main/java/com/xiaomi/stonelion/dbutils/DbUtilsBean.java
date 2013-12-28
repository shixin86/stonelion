package com.xiaomi.stonelion.dbutils;

import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Created with IntelliJ IDEA.
 * User: shixin
 * Date: 12/28/13
 * Time: 3:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class DbUtilsBean {
    private int tinyIntValue;
    private int smallIntValue;
    private int mediumIntValue;
    private int intValue;
    private int bigIntValue;
    private float floatValue;
    private double doubleValue;
    private String charValue;
    private String varcharValue;
    private byte[] blobValue;
    private String textValue;
    private Date dateValue;
    private Time timeValue;
    private String yearValue;
    private Timestamp datetimeValue;
    private Timestamp timestampValue;

    public int getTinyIntValue() {
        return tinyIntValue;
    }

    public void setTinyIntValue(int tinyIntValue) {
        this.tinyIntValue = tinyIntValue;
    }

    public int getSmallIntValue() {
        return smallIntValue;
    }

    public void setSmallIntValue(int smallIntValue) {
        this.smallIntValue = smallIntValue;
    }

    public int getMediumIntValue() {
        return mediumIntValue;
    }

    public void setMediumIntValue(int mediumIntValue) {
        this.mediumIntValue = mediumIntValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public int getBigIntValue() {
        return bigIntValue;
    }

    public void setBigIntValue(int bigIntValue) {
        this.bigIntValue = bigIntValue;
    }

    public String getCharValue() {
        return charValue;
    }

    public void setCharValue(String charValue) {
        this.charValue = charValue;
    }

    public String getVarcharValue() {
        return varcharValue;
    }

    public void setVarcharValue(String varcharValue) {
        this.varcharValue = varcharValue;
    }

    public byte[] getBlobValue() {
        return blobValue;
    }

    public void setBlobValue(byte[] blobValue) {
        this.blobValue = blobValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Time getTimeValue() {
        return timeValue;
    }

    public void setTimeValue(Time timeValue) {
        this.timeValue = timeValue;
    }

    public String getYearValue() {
        return yearValue;
    }

    public void setYearValue(String yearValue) {
        this.yearValue = yearValue;
    }

    public Timestamp getDatetimeValue() {
        return datetimeValue;
    }

    public void setDatetimeValue(Timestamp datetimeValue) {
        this.datetimeValue = datetimeValue;
    }

    public Timestamp getTimestampValue() {
        return timestampValue;
    }

    public void setTimestampValue(Timestamp timestampValue) {
        this.timestampValue = timestampValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public void print() throws UnsupportedEncodingException {
        System.out.println(" ----- ");
        System.out.println("tinyint " + this.getTinyIntValue());
        System.out.println("smallint " + this.getSmallIntValue());
        System.out.println("mediumint " + this.getMediumIntValue());
        System.out.println("int " + this.getIntValue());
        System.out.println("bigint " + this.getBigIntValue());
        System.out.println("float " + this.getFloatValue());
        System.out.println("double " + this.getDoubleValue());
        System.out.println("char " + this.getCharValue());
        System.out.println("varchar " + this.getVarcharValue());
        System.out.println("blob " + new String(this.getBlobValue(), "utf-8"));
        System.out.println("text " + this.getTextValue());
        System.out.println("date " + this.getDateValue());
        System.out.println("time " + this.getTimeValue());
        System.out.println("year " + this.getYearValue());
        System.out.println("datetime " + this.getDatetimeValue());
        System.out.println("timestamp " + this.getTimestampValue());
    }
}
