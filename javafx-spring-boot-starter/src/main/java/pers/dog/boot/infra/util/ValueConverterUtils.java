package pers.dog.boot.infra.util;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310DateTimeDeserializerBase;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author 废柴 2020/10/26 10:42
 */
public class ValueConverterUtils {
    /**
     * 参数类型读取
     */
    @FunctionalInterface
    interface ParameterValueReader {

        /**
         * 读取参数值
         *
         * @param value 参数值
         * @return 目标类型的参数值
         */
        Object read(Object value);
    }

    /**
     * 参数类型写
     */
    @FunctionalInterface
    interface ParameterValueWriter {

        /**
         * 写出参数值
         *
         * @param value 参数值
         * @return 参数值
         */
        Object write(Object value);
    }

    private static final Logger logger = LoggerFactory.getLogger(ValueConverterUtils.class);
    private static final String ZONE_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_ZONE = "GMT+8";
    private static final Map<Class<?>, ParameterValueReader> SIMPLE_TYPE_PARAMETER_READER_HOLDER = new ConcurrentHashMap<>(16);
    private static final Map<Class<?>, ParameterValueWriter> SIMPLE_TYPE_PARAMETER_WRITER_HOLDER = new ConcurrentHashMap<>(16);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 8 种基本类型
        ParameterValueReader byteReader = value -> value instanceof Byte ? value : Byte.parseByte(String.valueOf(value));
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(byte.class, byteReader);
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(Byte.class, byteReader);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(byte.class, value -> value);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(Byte.class, value -> value);

        ParameterValueReader shortReader = value -> value instanceof Short ? value : Short.parseShort(String.valueOf(value));
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(short.class, shortReader);
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(Short.class, shortReader);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(short.class, value -> value);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(Short.class, value -> value);

        ParameterValueReader intReader = value -> value instanceof Integer ? value : Integer.parseInt(String.valueOf(value));
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(int.class, intReader);
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(Integer.class, intReader);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(int.class, value -> value);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(Integer.class, value -> value);

        ParameterValueReader longReader = value -> value instanceof Long ? value : Long.parseLong(String.valueOf(value));
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(long.class, longReader);
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(Long.class, longReader);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(long.class, value -> value);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(Long.class, value -> value);

        ParameterValueReader floatReader = value -> value instanceof Float ? value : Float.parseFloat(String.valueOf(value));
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(float.class, floatReader);
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(Float.class, floatReader);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(float.class, value -> value);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(Float.class, value -> value);

        ParameterValueReader doubleReader = value -> value instanceof Double ? value : Double.parseDouble(String.valueOf(value));
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(double.class, doubleReader);
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(Double.class, doubleReader);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(double.class, value -> value);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(Double.class, value -> value);

        ParameterValueReader booleanReader = value -> value instanceof Boolean ? value : Boolean.parseBoolean(String.valueOf(value));
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(boolean.class, booleanReader);
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(Boolean.class, booleanReader);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(boolean.class, value -> value);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(Boolean.class, value -> value);

        ParameterValueReader charReader = value -> value instanceof Character ? value : String.valueOf(value).charAt(0);
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(char.class, charReader);
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(Character.class, charReader);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(char.class, value -> value);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(Character.class, value -> value);
        // 字符串
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(String.class, String::valueOf);
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(String.class, value -> value);
        // 时间
        TimeZone timeZone = TimeZone.getTimeZone(TIME_ZONE);
        FastDateFormat fastDateFormat = FastDateFormat.getInstance(ZONE_DATE_TIME_FORMAT, timeZone);
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(Date.class, value -> {
            try {
                return fastDateFormat.parse(String.valueOf(value));
            } catch (ParseException e) {
                String exceptionMessage = String.format("[Converter] Date.class type parameter conversion error : format[%s] value[%s]", ZONE_DATE_TIME_FORMAT, value);
                logger.error(exceptionMessage, e);
                throw new IllegalArgumentException("Value parser failed.");
            }
        });
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(Date.class, fastDateFormat::format);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(LocalDate.class, value -> {
            try {
                return LocalDate.parse(String.valueOf(value), dateFormatter);
            } catch (DateTimeParseException e) {
                String exceptionMessage = String.format("[Converter] LocalDate.class type parameter conversion error : format[%s] value[%s]", DATE_FORMAT, value);
                logger.error(exceptionMessage, e);
                throw new IllegalArgumentException("Value parser failed.");
            }
        });
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(LocalDate.class, value -> dateFormatter.format((LocalDate) value));

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withLocale(Locale.CHINA);
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(LocalDateTime.class, value -> {
            try {
                return LocalDateTime.parse(String.valueOf(value), dateTimeFormatter);
            } catch (DateTimeParseException e) {
                String exceptionMessage = String.format("[Converter] LocalDateTime.class type parameter conversion error : format[%s] value[%s]", DATE_TIME_FORMAT, value);
                logger.error(exceptionMessage, e);
                throw new IllegalArgumentException("Value parser failed.");
            }
        });
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(LocalDateTime.class, value -> dateTimeFormatter.format((LocalDateTime) value));

        DateTimeFormatter zoneDateTimeFormatter = DateTimeFormatter.ofPattern(ZONE_DATE_TIME_FORMAT).withZone(ZoneId.of(TIME_ZONE));
        SIMPLE_TYPE_PARAMETER_READER_HOLDER.put(ZonedDateTime.class, value -> {
            try {
                return ZonedDateTime.parse(String.valueOf(value), zoneDateTimeFormatter);
            } catch (DateTimeParseException e) {
                String exceptionMessage = String.format("[Converter] ZonedDateTime.class type parameter conversion error : format[%s] value[%s]", ZONE_DATE_TIME_FORMAT, value);
                logger.error(exceptionMessage, e);
                throw new IllegalArgumentException("Value parser failed.");
            }
        });
        SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.put(ZonedDateTime.class, value -> zoneDateTimeFormatter.format((ZonedDateTime) value));
        // ObjectMapper
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        StdDateFormat stdDateFormat = new StdDateFormat().withTimeZone(timeZone);
        javaTimeModule.addSerializer(Date.class, new DateSerializer(null, stdDateFormat));
        javaTimeModule.addDeserializer(Date.class, new DateDeserializers.DateDeserializer(new DateDeserializers.DateDeserializer(), stdDateFormat, StdDateFormat.DATE_FORMAT_STR_ISO8601));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        javaTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(zoneDateTimeFormatter));
        javaTimeModule.addDeserializer(ZonedDateTime.class, new JSR310DateTimeDeserializerBase<ZonedDateTime>(ZonedDateTime.class, zoneDateTimeFormatter) {
            @Override
            public ZonedDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                String dateTimeString = parser.getValueAsString();
                return StringUtils.hasText(dateTimeString) ? ZonedDateTime.parse(dateTimeString, _formatter) : null;
            }

            @Override
            protected JSR310DateTimeDeserializerBase<ZonedDateTime> withDateFormat(DateTimeFormatter dtf) {
                return this;
            }

            @Override
            protected JSR310DateTimeDeserializerBase<ZonedDateTime> withLeniency(Boolean leniency) {
                return this;
            }

            @Override
            protected JSR310DateTimeDeserializerBase<ZonedDateTime> withShape(JsonFormat.Shape shape) {
                return this;
            }
        });
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        OBJECT_MAPPER.disable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
        OBJECT_MAPPER.registerModules(javaTimeModule);
    }


    public static <T> T read(String value, Class<T> type) {
        // 是否为 null
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        // Simple type
        ParameterValueReader parameterValueReader = SIMPLE_TYPE_PARAMETER_READER_HOLDER.get(type);
        if (parameterValueReader != null) {
            return (T) parameterValueReader.read(value);
        }
        // 按照 JSON 处理
        try {
            return OBJECT_MAPPER.readValue(value, type);
        } catch (IOException e) {
            String exceptionMessage = String.format("[Converter] JSON parameter conversion is an error : %s", value);
            logger.error(exceptionMessage, e);
            throw new IllegalArgumentException("Value parser failed.");
        }
    }

    public static <T> T read(String value, TypeReference<T> type) {
        // 是否为 null
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        // 按照 JSON 处理
        try {
            return OBJECT_MAPPER.readValue(value, type);
        } catch (IOException e) {
            String exceptionMessage = String.format("[Converter] JSON parameter conversion is an error : %s", value);
            logger.error(exceptionMessage, e);
            throw new IllegalArgumentException("Value parser failed.");
        }
    }

    /**
     * 写出参数
     *
     * @param value 参数值
     * @return 字面值
     */
    public static String write(Object value) {
        // 是否为 null
        if (value == null) {
            return null;
        }
        return write(value, value.getClass());
    }

    /**
     * 写出参数
     *
     * @param value 参数值
     * @return 字面值
     */
    public static String write(Object value, Class<?> parameterType) {
        // 是否为 null
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        // Simple type
        ParameterValueWriter parameterValueWriter = SIMPLE_TYPE_PARAMETER_WRITER_HOLDER.get(parameterType);
        if (parameterValueWriter != null) {
            return String.valueOf(parameterValueWriter.write(value));
        }
        // 按照 JSON 处理
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (IOException e) {
            String exceptionMessage = String.format("[Converter] An error occurred while converting the parameter to a JSON string : %s %s", parameterType, value);
            logger.error(exceptionMessage, e);
            throw new IllegalArgumentException("Value parser failed.");
        }
    }
}
