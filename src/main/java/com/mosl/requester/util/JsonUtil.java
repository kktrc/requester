package com.mosl.requester.util;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by moslpc on 2017/1/17.
 */
public class JsonUtil {

    public static <T> T  fromJson(String json,Class<T> tClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json,tClass);
    }
}
