package com.izj.knowledge.service.base.i18n;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.izj.knowledge.service.base.i18n.MLString.MLStringDeserializer;
import com.izj.knowledge.service.base.i18n.MLString.MLStringSerializer;

/**
 * Hold multiple texts with locale.
 *
 * @author iz-j
 *
 */
@JsonSerialize(using = MLStringSerializer.class)
@JsonDeserialize(using = MLStringDeserializer.class)
public final class MLString {
    private final Map<Locale, String> values = new HashMap<>();

    /**
     * Construct empty instance.
     */
    public MLString() {
    }

    /**
     * Construct with other source and String converter.
     *
     * @param entrySet
     */
    public MLString(MLString src, BiFunction<Locale, String, String> converter) {
        if (Objects.isNull(src)) {
            return;
        }

        src
            .entrySet()
            .stream()
            .forEach(e -> {
                Locale locale = e.getKey();
                String srcString = e.getValue();
                String converted = converter.apply(locale, srcString);
                values.put(locale, converted);
            });

        src.entrySet();
    }

    /**
     * Construct with entrySet.
     *
     * @param entrySet
     */
    public MLString(Set<Map.Entry<Locale, String>> entrySet) {
        if (entrySet != null) {
            entrySet.forEach(e -> values.put(e.getKey(), e.getValue()));
        }
    }

    /**
     * Construct with the current locale and given value.
     */
    public MLString(String value) {
        this.set(LocaleContextHolder.getLocale(), value);
    }

    /**
     * Construct with given locale and value.
     *
     * @param locale
     * @param value
     */
    public MLString(Locale locale, String value) {
        this.set(locale, value);
    }

    /**
     * Return value associated with the current locale.<br>
     * If no value found, return prior value instead.<br>
     * If found empty String "" return it.
     *
     * @return value
     */
    public String get() {
        String res = this.get(LocaleContextHolder.getLocale());
        if (res != null) {
            return res;
        }

        for (Locale locale : SupportedLocale.getCandidates()) {
            res = this.get(locale);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    /**
     * @param locale
     * @return value
     */
    public String get(Locale locale) {
        return values.get(locale);
    }

    /**
     * @param locale
     * @param defaultValue
     * @return value
     */
    public String getOrDefault(Locale locale, String defaultValue) {
        String value = values.getOrDefault(locale, defaultValue);
        return value != null ? value : defaultValue;
    }

    /**
     * @param value
     * @return this instance
     */
    public MLString set(String value) {
        values.put(LocaleContextHolder.getLocale(), value);
        return this;
    }

    /**
     * @param locale
     * @param value
     * @return this instance
     */
    public MLString set(Locale locale, String value) {
        values.put(locale, value);
        return this;
    }

    /**
     * @return entrySet (unmodifiable)
     */
    public Set<Entry<Locale, String>> entrySet() {
        return Collections.unmodifiableSet(values.entrySet());
    }

    /**
     * @return true if no values
     */
    public boolean isEmpty() {
        for (SupportedLocale locale : SupportedLocale.values()) {
            if (StringUtils.isNotEmpty(this.get(locale.get()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override
    public int hashCode() {
        return (values == null) ? 0 : values.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MLString other = (MLString)obj;
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }

    public static class MLStringSerializer extends JsonSerializer<MLString> {

        private final ObjectWriter writer = new ObjectMapper().writerFor(new TypeReference<Map<Locale, String>>() {
        });

        @Override
        public void serialize(MLString value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException, JsonProcessingException {
            gen.writeRawValue(writer.writeValueAsString(value.values));
        }
    }

    public static class MLStringDeserializer extends JsonDeserializer<MLString> {

        private final ObjectReader reader = new ObjectMapper().readerFor(new TypeReference<Map<Locale, String>>() {
        });

        @Override
        public MLString deserialize(JsonParser p, DeserializationContext ctxt) throws IOException,
                JsonProcessingException {
            MLString res = new MLString();
            Map<Locale, String> values = reader.readValue(p);
            res.values.putAll(values);
            return res;
        }
    }
}
