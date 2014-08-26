package com.xiaomi.stonelion.jackson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: shixin
 * Date: 2/28/14
 * Time: 4:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyValue {
    public String name;
    public int age;
    public List<String> myTagsList = new ArrayList<String>();
    public Map<String, Integer> myTagsCountMap = new HashMap<String, Integer>();
    public List<Dog> myDogList = new ArrayList<Dog>();
    public Map<String, Dog> myDogMap = new HashMap<String, Dog>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(name).append("\n");
        sb.append("age=").append(age).append("\n");
        sb.append("myTags=").append(myTagsList).append("\n");
        sb.append("myTagsCountMap=").append(myTagsCountMap).append("\n");
        sb.append("myDogList=").append(myDogList).append("\n");
        sb.append("myDogMap=").append(myDogMap).append("\n");
        return sb.toString();
    }

    public static class Dog {
        public String dogName;
    }

}
