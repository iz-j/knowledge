package com.izj.dynamodb.internal.util;

import static com.amazonaws.util.BinaryUtils.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.izj.dynamodb.internal.metadata.EntityMetadata.AttributeType;

/**
 *
 * @author ~~~~
 *
 */
public final class DynamodbInternalUtils {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    private static final ObjectMapper MAPPER = createCustomizedObjectMapper();

    private DynamodbInternalUtils() {
    }

    /**
     * 目的はdynamoDBに入れたり取り出せることであり、<br>
     * frontに渡ってきたDateをnewDate()しないのでdisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)を書いてない
     */
    public static ObjectMapper createCustomizedObjectMapper() {
        return new ObjectMapper() {
            private static final long serialVersionUID = 1L;
            {
                setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
                setSerializationInclusion(JsonInclude.Include.NON_NULL);
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                JavaTimeModule javaTimeModule = new JavaTimeModule();
                javaTimeModule.addSerializer(ZonedDateTime.class, new JsonSerializer<ZonedDateTime>() {
                    @Override
                    public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers)
                            throws IOException, JsonProcessingException {
                        gen.writeString(value.format(DATE_TIME_FORMATTER));
                    }
                });
                javaTimeModule.addDeserializer(ZonedDateTime.class, new JsonDeserializer<ZonedDateTime>() {
                    @Override
                    public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt)
                            throws IOException, JsonProcessingException {
                        return ZonedDateTime.parse(p.getValueAsString(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
                    }
                });
                registerModule(javaTimeModule);
                setTimeZone(TimeZone.getDefault());
            }
        };
    }

    public static Object toAttributeValue(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime)value).format(DATE_TIME_FORMATTER);
        } else if (value instanceof UUID) {
            return value.toString();
        } else if (value.getClass().isEnum()) {
            return enumToString(value, value.getClass());
        }
        return value;
    }

    public static List<?> toAttributeValueList(List<?> values) {
        if (CollectionUtils.isEmpty(values)) {
            return values;
        }

        return values
            .stream()
            .map(value -> DynamodbInternalUtils.toAttributeValue(value))
            .collect(Collectors.toList());
    }

    public static Object toValueObject(Object value, Class<?> type) {
        if (value == null) {
            return null;
        } else if (type == ZonedDateTime.class) {
            return ZonedDateTime.parse(value.toString(), DATE_TIME_FORMATTER);
        } else if (type == UUID.class) {
            return UUID.fromString(value.toString());
        } else if (type.isEnum()) {
            return stringToEnum(value.toString(), type);
        } else if (ClassUtils.isPrimitiveOrWrapper(type)) {
            Class<?> wrapper = ClassUtils.primitiveToWrapper(type);
            if (wrapper == Integer.class) {
                return Integer.valueOf(value.toString());
            } else if (wrapper == Long.class) {
                return Long.valueOf(value.toString());
            } else if (wrapper == Double.class) {
                return Double.valueOf(value.toString());
            } else if (wrapper == Float.class) {
                return Float.valueOf(value.toString());
            } else if (wrapper == Boolean.class) {
                return Boolean.valueOf(value.toString());
            }
        } else if (List.class.isAssignableFrom(type) && !(value instanceof List)) {
            return Arrays.asList(value);
        } else if (Set.class.isAssignableFrom(type) && !(value instanceof Set)) {
            return new HashSet<>(Arrays.asList(value));
        }
        return value;

    }

    public static <T> String toJson(T value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(Class<T> type, String json) {
        return fromJson(json, new TypeReference<T>() {
            @Override
            public Type getType() {
                return type;
            }
        });
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        ObjectReader reader = MAPPER.readerFor(typeReference);
        try {
            return reader.readValue(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static AttributeType toAttributeType(Class<?> type) {
        if (type == String.class || type == UUID.class || type == ZonedDateTime.class || type.isEnum()) {
            return AttributeType.S;
        } else if (type == BigDecimal.class) {
            return AttributeType.N;
        } else if (ClassUtils.isPrimitiveOrWrapper(type)) {
            Class<?> wrapper = ClassUtils.primitiveToWrapper(type);
            if (wrapper == Integer.class
                    || wrapper == Long.class
                    || wrapper == Double.class
                    || wrapper == Float.class
                    || wrapper == Boolean.class) {
                return AttributeType.N;
            }
        }
        throw new UnsupportedOperationException("Not supported: " + type);
    }

    public static <T> Object toSimpleObject(String value, Class<T> type) {
        if (type == String.class) {
            return value;
        } else if (type == UUID.class) {
            return UUID.fromString(value);
        } else if (type == ZonedDateTime.class) {
            return ZonedDateTime.parse(value, DATE_TIME_FORMATTER);
        } else if (type == BigDecimal.class) {
            return new BigDecimal(value);
        } else if (type.isEnum()) {
            return stringToEnum(value, type);
        } else if (ClassUtils.isPrimitiveOrWrapper(type)) {
            Class<?> wrapper = ClassUtils.primitiveToWrapper(type);
            if (wrapper == Boolean.class) {
                return Boolean.valueOf(value);
            } else if (wrapper == Integer.class) {
                return Integer.valueOf(value);
            } else if (wrapper == Long.class) {
                return Long.valueOf(value);
            } else if (wrapper == Double.class) {
                return Double.valueOf(value);
            } else if (wrapper == Float.class) {
                return Float.valueOf(value);
            }
        }
        throw new UnsupportedOperationException("Not supported: " + type);
    }

    public static <T> T toSimpleValue(AttributeValue value) {
        if (value == null) {
            return null;
        }
        if (Boolean.TRUE.equals(value.getNULL())) {
            return null;
        } else if (Boolean.FALSE.equals(value.getNULL())) {
            throw new UnsupportedOperationException("False-NULL is not supported in DynamoDB");
        } else if (value.getBOOL() != null) {
            @SuppressWarnings("unchecked")
            T t = (T)value.getBOOL();
            return t;
        } else if (value.getS() != null) {
            @SuppressWarnings("unchecked")
            T t = (T)value.getS();
            return t;
        } else if (value.getN() != null) {
            @SuppressWarnings("unchecked")
            T t = (T)new BigDecimal(value.getN());
            return t;
        } else if (value.getB() != null) {
            @SuppressWarnings("unchecked")
            T t = (T)copyAllBytesFrom(value.getB());
            return t;
        } else if (value.getSS() != null) {
            @SuppressWarnings("unchecked")
            T t = (T)new LinkedHashSet<String>(value.getSS());
            return t;
        } else if (value.getNS() != null) {
            Set<BigDecimal> set = new LinkedHashSet<BigDecimal>(value.getNS().size());
            for (String s : value.getNS()) {
                set.add(new BigDecimal(s));
            }
            @SuppressWarnings("unchecked")
            T t = (T)set;
            return t;
        } else if (value.getBS() != null) {
            Set<byte[]> set = new LinkedHashSet<byte[]>(value.getBS().size());
            for (ByteBuffer bb : value.getBS()) {
                set.add(copyAllBytesFrom(bb));
            }
            @SuppressWarnings("unchecked")
            T t = (T)set;
            return t;
        } else if (value.getL() != null) {
            @SuppressWarnings("unchecked")
            T t = (T)InternalUtils.toSimpleList(value.getL());
            return t;
        } else if (value.getM() != null) {
            @SuppressWarnings("unchecked")
            T t = (T)InternalUtils.toSimpleMapValue(value.getM());
            return t;
        } else {
            throw new IllegalArgumentException(
                    "Attribute value must not be empty: " + value);
        }
    }

    public static String toMarkerValue(Object value) {
        return value == null ? null : StringUtils.substring(value.toString(), 0, 25);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T extends Enum<T>> T stringToEnum(String value, Class<?> type) {
        return (T)Enum.valueOf((Class<? extends Enum>)type, value);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> String enumToString(Object value, Class<?> type) {
        return ((T)value).name();
    }
}
