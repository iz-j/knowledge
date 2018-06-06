package com.izj.knowledge.web.base.mapping;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.izj.knowledge.web.base.mapping.ApiVersionRequestCondition.VersionRange;

public class CustomRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    private static final String VERSION_PREFIX = "v";

    @Override
    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        ApiVersion typeAnnotation = AnnotationUtils.findAnnotation(
                handlerType, ApiVersion.class);
        return createCondition(typeAnnotation);
    }

    @Override
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        ApiVersion methodAnnotation = AnnotationUtils.findAnnotation(
                method, ApiVersion.class);
        return createCondition(methodAnnotation);
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
        if (AnnotationUtils.findAnnotation(method, ApiVersion.class) != null) {
            RequestCondition<?> methodCondition = this.getCustomMethodCondition(method);
            return this.createApiVersionInfo(methodCondition).combine(info);
        }
        return info;
    }

    private RequestMappingInfo createApiVersionInfo(RequestCondition<?> customCondition) {
        String pathPattern = "/" + VERSION_PREFIX + "{version:\\d+}/";
        PatternsRequestCondition patternCondtion = new PatternsRequestCondition(
                new String[] { pathPattern },
                this.getUrlPathHelper(),
                this.getPathMatcher(),
                this.useSuffixPatternMatch(),
                this.useTrailingSlashMatch(),
                this.getFileExtensions());
        return new RequestMappingInfo(patternCondtion, null, null, null, null, null, customCondition);
    }

    private RequestCondition<?> createCondition(ApiVersion apiVersion) {
        if (apiVersion == null) {
            return null;
        }
        int version = apiVersion.value();
        if (version > 0) {
            return new ApiVersionRequestCondition(VERSION_PREFIX, version);
        }
        int from = apiVersion.from();
        int to = apiVersion.to();
        if (from > 0 && to > 0) {
            return new ApiVersionRequestCondition(VERSION_PREFIX, from, to);
        } else if (from > 0) {
            return new ApiVersionRequestCondition(VERSION_PREFIX, VersionRange.FROM, from);
        } else if (to > 0) {
            return new ApiVersionRequestCondition(VERSION_PREFIX, VersionRange.TO, to);
        }
        return null;
    }

}
