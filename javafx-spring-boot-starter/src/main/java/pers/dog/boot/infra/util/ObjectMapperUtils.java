package pers.dog.boot.infra.util;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author 废柴 2021/6/21 20:25
 */
public class ObjectMapperUtils {
    private static final Logger logger = LoggerFactory.getLogger(ObjectMapperUtils.class);
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();

        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static String writeAsString(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("[Object Mapper] Unable to write {} as json string.", value, e);
            return null;
        }
    }

    public static <T> T readValue(String json, Class<T> type) {
        if (StringUtils.hasText(json)) {
            try {
                return OBJECT_MAPPER.readValue(json, type);
            } catch (JsonProcessingException e) {
                logger.error("[Object Mapper] Unable to read json {} as type {}.", json, type, e);
                return null;
            }
        }
        return null;
    }

    public static <T> T readValue(JsonNode jsonNode, Class<T> type) {
        if (jsonNode != null) {
            try {
                return OBJECT_MAPPER.readValue(new TreeTraversingParser(jsonNode), type);
            } catch (IOException e) {
                logger.error("[Object Mapper] Unable to read json {} as type {}.", jsonNode, type, e);
                return null;
            }
        }
        return null;
    }

    public static <T> T readValue(String json, TypeReference<T> typeReference) {
        if (StringUtils.hasText(json)) {
            try {
                return OBJECT_MAPPER.readValue(json, typeReference);
            } catch (JsonProcessingException e) {
                logger.error("[Object Mapper] Unable to read json {} as type {}.", json, typeReference, e);
                return null;
            }
        }
        return null;
    }

    public static <T> T readValue(JsonNode jsonNode, TypeReference<T> typeReference) {
        if (jsonNode != null) {
            try {
                return OBJECT_MAPPER.readValue(new TreeTraversingParser(jsonNode), typeReference);
            } catch (IOException e) {
                logger.error("[Object Mapper] Unable to read json {} as type {}.", jsonNode, typeReference, e);
                return null;
            }
        }
        return null;
    }
}
