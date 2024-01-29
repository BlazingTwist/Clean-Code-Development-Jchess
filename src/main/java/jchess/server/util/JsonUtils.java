package jchess.server.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    public static ObjectMapper getMapper() {
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

    public static Traverse traverse(JsonNode node) {
        return new Traverse(node);
    }

    public static final class Traverse {
        private final JsonNode currentNode;

        public Traverse(JsonNode currentNode) {
            this.currentNode = currentNode;
        }

        public Traverse get(String propertyName) {
            return currentNode == null ? this : new Traverse(currentNode.get(propertyName));
        }

        public String textValue() {
            return currentNode == null ? null : currentNode.textValue();
        }
    }
}
