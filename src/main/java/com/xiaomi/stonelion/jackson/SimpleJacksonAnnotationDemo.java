package com.xiaomi.stonelion.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class SimpleJacksonAnnotationDemo {
    private static class MyBean1 {
        public String value1;

        @JsonIgnore
        public String value2;

        public String value3;
    }

    @JsonIgnoreProperties({"value2"})
    private static class MyBean2 {
        public String value1;
        public String value2;
    }

    @Test
    public void testJSONIgnore() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        MyBean1 myBean1 = new MyBean1();
        myBean1.value1 = "a";
        myBean1.value2 = "b";
        System.out.println(objectMapper.writeValueAsString(myBean1));

        MyBean2 myBean2 = new MyBean2();
        myBean2.value1 = "a";
        myBean2.value2 = "b";
        System.out.println(objectMapper.writeValueAsString(myBean2));
    }
}
