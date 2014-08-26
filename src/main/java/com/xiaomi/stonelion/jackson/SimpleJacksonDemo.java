package com.xiaomi.stonelion.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaomi.stonelion.jackson.MyValue.Dog;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: shixin
 * Date: 2/28/14
 * Time: 4:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleJacksonDemo {
    @Test
    public void testConvertPOJOToJSON() throws JsonProcessingException {
        MyValue myValue = new MyValue();
        myValue.age = 28;
        myValue.name = "shixin";

        List<String> tags = new ArrayList<String>();
        tags.add("tagA");
        tags.add("tagB");
        myValue.myTagsList = tags;

        Map<String, Integer> myTagsCountMap = new HashMap<String, Integer>();
        myTagsCountMap.put("tagA", 1);
        myTagsCountMap.put("tagB", 2);
        myValue.myTagsCountMap = myTagsCountMap;

        List<Dog> myDogList = new ArrayList<Dog>();
        Map<String, Dog> myDogMap = new HashMap<String, Dog>();
        Dog dog1 = new Dog();
        dog1.dogName = "dog1";
        Dog dog2 = new Dog();
        dog2.dogName = "dog2";
        myDogList.add(dog1);
        myDogList.add(dog2);
        myDogMap.put(dog1.dogName, dog1);
        myDogMap.put(dog2.dogName, dog2);
        myValue.myDogList = myDogList;
        myValue.myDogMap = myDogMap;

        ObjectMapper objectMapper = new ObjectMapper();

        String jsonStr = objectMapper.writeValueAsString(myValue);

        System.out.format("JSON Str : %s.\n", jsonStr);
    }

    @Test
    public void testConvertJSONToPOJO() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonStr = "{\"name\":\"shixin\",\"age\":28,\"myTagsList\":[\"tagA\",\"tagB\"],\"myTagsCountMap\":{\"tagB\":2,\"tagA\":1}}";
        MyValue myValue = objectMapper.readValue(jsonStr, MyValue.class);
        System.out.println(myValue);
    }

    @Test
    public void testConvertMapToJSON() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<Integer, String> simpleMap = new HashMap<Integer, String>();
        simpleMap.put(1, "a");

        String jsonStr = objectMapper.writeValueAsString(simpleMap);
        System.out.format("JSON Str : %s.\n", jsonStr);

        Map<Integer, MyValue> complexMap = new HashMap<Integer, MyValue>();
        MyValue myValue = new MyValue();
        myValue.name = "shixin";
        complexMap.put(1, myValue);

        jsonStr = new ObjectMapper().writeValueAsString(complexMap);
        System.out.format("JSON Str : %s.\n", jsonStr);
    }

    @Test
    public void testConvertJSONToMap() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonStr = "{\"1\":\"a\"}";

        Map simpleMap = objectMapper.readValue(jsonStr, Map.class);
        System.out.println(simpleMap.get("1"));

        simpleMap = objectMapper.readValue(jsonStr, new TypeReference<Map<Integer, String>>() {
        });
        System.out.println(simpleMap.get(1));
    }
}
