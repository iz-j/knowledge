package com.izj.knowledge.web.base.custom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.DefaultCorsProcessor;

/**
 * To avoid duplication of adding 'Access-Control-Allow-Origin' when running on AWS.<br>
 * nginx dues to add response header using 'add_header' directive instead of spring cors filter.<br>
 * <br>
 * FIXME Let program not depends on environment as possible as we can!
 *
 * @author iz-j
 *
 */
@Slf4j
public class CustomCorsProcessor extends DefaultCorsProcessor {

    private final boolean runningOnLocalEnv;

    public CustomCorsProcessor(boolean runningOnLocalEnv) {
        this.runningOnLocalEnv = runningOnLocalEnv;
    }

    @Override
    protected boolean handleInternal(ServerHttpRequest request, ServerHttpResponse response,
            CorsConfiguration config, boolean preFlightRequest) throws IOException {

        if (runningOnLocalEnv) {
            return super.handleInternal(request, response, config, preFlightRequest);
        }

        // Almost copied from DefaultCorsProcessor#handleInternal.

        String requestOrigin = request.getHeaders().getOrigin();
        String allowOrigin = checkOrigin(config, requestOrigin);

        HttpMethod requestMethod = getMethodToUse(request, preFlightRequest);
        List<HttpMethod> allowMethods = checkMethods(config, requestMethod);

        List<String> requestHeaders = getHeadersToUse(request, preFlightRequest);
        List<String> allowHeaders = checkHeaders(config, requestHeaders);

        if (allowOrigin == null || allowMethods == null || (preFlightRequest && allowHeaders == null)) {
            log.warn("Reject request! requestOrigin = {}", requestOrigin);
            rejectRequest(response);
            return false;
        }

        HttpHeaders responseHeaders = response.getHeaders();
        responseHeaders.add(HttpHeaders.VARY, HttpHeaders.ORIGIN);

        response.flush();
        return true;
    }

    private HttpMethod getMethodToUse(ServerHttpRequest request, boolean isPreFlight) {
        return (isPreFlight ? request.getHeaders().getAccessControlRequestMethod() : request.getMethod());
    }

    private List<String> getHeadersToUse(ServerHttpRequest request, boolean isPreFlight) {
        HttpHeaders headers = request.getHeaders();
        return (isPreFlight ? headers.getAccessControlRequestHeaders() : new ArrayList<String>(headers.keySet()));
    }
}
