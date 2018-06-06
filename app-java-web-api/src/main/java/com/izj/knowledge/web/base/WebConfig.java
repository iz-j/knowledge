package com.izj.knowledge.web.base;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.MultipartConfigElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.izj.knowledge.service.ServiceConfig;
import com.izj.knowledge.web.base.interceptor.ControllerLoggingInterceptor;
import com.izj.knowledge.web.base.interceptor.UserAuthenticationInterceptor;
import com.izj.knowledge.web.base.mapping.CustomRequestMappingHandlerMapping;

@Configuration
@Import({ ServiceConfig.class })
public class WebConfig extends WebMvcConfigurationSupport {

    @Bean
    public ControllerLoggingInterceptor controllerLoggingInterceptor() {
        return new ControllerLoggingInterceptor();
    }

    @Bean
    public UserAuthenticationInterceptor userAuthenticationInterceptor() {
        return new UserAuthenticationInterceptor();
    }

    @Value("${app.maxFileSize:2MB}")
    private String maxFileSize;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(maxFileSize);
        factory.setMaxRequestSize(maxFileSize);
        return factory.createMultipartConfig();
    }

    @Override
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping mapping = new CustomRequestMappingHandlerMapping();

        final String errorPattern = "/error";// for BasicErrorController
        final String externalPattern = "/v*/**";
        mapping
            .setInterceptors(
                    new MappedInterceptor(null, controllerLoggingInterceptor()),
                    new MappedInterceptor(null, new String[] { errorPattern, externalPattern },
                            userAuthenticationInterceptor()));

        return mapping;
    }

    @Bean
    @Autowired
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Application runs behind of CloudFront - ELB, so can allow all origins.
        config.addAllowedOrigin("*");

        Arrays
            .stream(new String[] { "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS" })
            .forEach(method -> config.addAllowedMethod(method));

        Arrays
            .stream(new String[] { "Content-Type", "Authorization", "X-TenantId" })
            .forEach(header -> config.addAllowedHeader(header));

        config.setAllowCredentials(false);

        source.registerCorsConfiguration("/**", config);

        CorsFilter corsFilter = new CorsFilter(source);
        // corsFilter.setCorsProcessor(new CustomCorsProcessor(env.get() == Environments.LOCAL));

        FilterRegistrationBean bean = new FilterRegistrationBean(corsFilter);
        bean.setOrder(0);
        return bean;
    }

    /**
     * なぜ、ZonedDateTimeとLocalDateをシリアライズ、デシリアライズしているのか<br>
     * - ZonedDateTimeはそのままfrontに渡すとnew Date()できない形で渡るから<br>
     * - LocalDateはdisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)のせいで <br>
     * シリアライズするとyyyy-MM-ddの形となり、デシリアライズは[yyyy, MM, dd]の形しか受け付けないというちぐはぐな感じになった<br>
     * これを解消するためにyyyy-MM-ddにした
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        final DateTimeFormatter localDateformatter = DateTimeFormatter.ISO_DATE;
        final DateTimeFormatter localTimeformatter = DateTimeFormatter.ofPattern("HH:mm");
        final ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ZonedDateTime.class, new JsonSerializer<ZonedDateTime>() {
            @Override
            public void serialize(ZonedDateTime zonedDateTime, JsonGenerator jsonGenerator,
                    SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                jsonGenerator
                    .writeString(formatter.format(zonedDateTime));
            }
        });
        module.addDeserializer(ZonedDateTime.class, new JsonDeserializer<ZonedDateTime>() {
            @Override
            public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(p.getValueAsString(), formatter);
                return zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
            }
        });
        module.addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public void serialize(LocalDate localDate, JsonGenerator jsonGenerator,
                    SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                jsonGenerator
                    .writeString(localDateformatter.format(localDate));
            }
        });
        module.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                return LocalDate.parse(p.getValueAsString(), localDateformatter);
            }
        });
        module.addSerializer(LocalTime.class, new JsonSerializer<LocalTime>() {
            @Override
            public void serialize(LocalTime localTime, JsonGenerator jsonGenerator,
                    SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                jsonGenerator
                    .writeString(localTimeformatter.format(localTime));
            }
        });
        module.addDeserializer(LocalTime.class, new JsonDeserializer<LocalTime>() {
            @Override
            public LocalTime deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                return LocalTime.parse(p.getValueAsString(), localTimeformatter);
            }
        });
        objectMapper.registerModules(new JavaTimeModule(), module);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        converters.stream().forEach(converter -> {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                ((MappingJackson2HttpMessageConverter)converter).setObjectMapper(objectMapper);
            }
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter)converter).setDefaultCharset(Charset.forName("UTF-8"));
            }
        });
    }

}
