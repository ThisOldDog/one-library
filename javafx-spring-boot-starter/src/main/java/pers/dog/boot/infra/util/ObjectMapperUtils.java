package pers.dog.boot.infra.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author 废柴 2021/6/21 20:25
 */
@SuppressWarnings("DuplicatedCode")
public class ObjectMapperUtils {
    private static final Logger logger = LoggerFactory.getLogger(ObjectMapperUtils.class);
    private static final ObjectMapper OBJECT_MAPPER = ValueConverterUtils.OBJECT_MAPPER;


    public static String writeAsString(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            String exceptionMessage = String.format("[Object Mapper] Unable to write %s as json string.", value);
            logger.error(exceptionMessage, e);
            return null;
        }
    }

    public static <T> T readValue(String json, Class<T> type) {
        if (StringUtils.hasText(json)) {
            try {
                return OBJECT_MAPPER.readValue(json, type);
            } catch (JsonProcessingException e) {
                String exceptionMessage = String.format("[Object Mapper] Unable to read json %s as type %s.", json, type);
                logger.error(exceptionMessage, e);
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
                String exceptionMessage = String.format("[Object Mapper] Unable to read json %s as type %s.", jsonNode, type);
                logger.error(exceptionMessage, e);
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
                String exceptionMessage = String.format("[Object Mapper] Unable to read json %s as type %s.", json, typeReference);
                logger.error(exceptionMessage, e);
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
                String exceptionMessage = String.format("[Object Mapper] Unable to read json %s as type %s.", jsonNode, typeReference);
                logger.error(exceptionMessage, e);
                return null;
            }
        }
        return null;
    }
}
