package com.izj.knowledge.service.base.util.json;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class ObjectMapperFactory {
    private static final JavaTimeModule JAVA_TIME_MODULE = createJavaTimeModule();

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = create();

    private ObjectMapperFactory() {
    }

    /**
     * Returns Jackson ObjectMapper with some configurations.
     *
     * @return configured mapper
     */
    public static ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(JAVA_TIME_MODULE);
        mapper.setTimeZone(TimeZone.getDefault());
        return mapper;
    }

    /**
     * DON'T change returned object mapper.
     * 
     * @return default setting object mapper
     */
    public static ObjectMapper getDefault() {
        return DEFAULT_OBJECT_MAPPER;
    }

    private static JavaTimeModule createJavaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(ZonedDateTime.class, new JsonSerializer<ZonedDateTime>() {
            @Override
            public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException, JsonProcessingException {
                gen.writeString(value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            }
        });
        javaTimeModule.addDeserializer(ZonedDateTime.class, new JsonDeserializer<ZonedDateTime>() {
            @Override
            public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                return ZonedDateTime.parse(p.getValueAsString(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
            }
        });
        return javaTimeModule;
    }
}
