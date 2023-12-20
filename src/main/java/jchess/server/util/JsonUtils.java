package jchess.server.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    public static ObjectMapper getMapper() {
        // TODO erja, general serialization configuration here
        return new ObjectMapper();
    }

    public static <T> String serialize(T data) {
        try {
            return getMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize data.", e);
            return "";
        }
    }
}
