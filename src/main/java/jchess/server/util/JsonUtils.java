package jchess.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    public static ObjectMapper getMapper() {
        // TODO erja, general serialization configuration here
        return new ObjectMapper();
    }
}
