package com.izj.knowledge.web.base.interceptor;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.izj.knowledge.web.base.interceptor.annotation.HealthCheck;
import com.izj.knowledge.web.base.interceptor.annotation.NoAuthencitation;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author iz-j
 *
 */
@Slf4j
public class UserAuthenticationInterceptor implements HandlerInterceptor, Ordered {

    private final ThreadLocal<String> originalThreadName = new ThreadLocal<>();

    @Override
    public int getOrder() {
        return InterceptorOrder.USER_AUTHENTICATION.value();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;// Mainly ignore preflight request.
        }

        originalThreadName.set(Thread.currentThread().getName());

        Method method = ((HandlerMethod)handler).getMethod();

        // Check whether authentication is required or not.
        NoAuthencitation noAuthencitation = AnnotationUtils.findAnnotation(method, NoAuthencitation.class);
        HealthCheck healthCheck = AnnotationUtils.findAnnotation(method, HealthCheck.class);
        if (noAuthencitation != null || healthCheck != null) {
            log.trace("Skip authentication verification for {}.", request.getRequestURI());
            return true;
        }

        String token = extractToken(request);
        /*
         * try { // Set authenticated user account with selected locale. Account account = service.verifyUser(token);
         * context.setAccount(account); LocaleContextHolder.setLocale(account.getLocale());
         * log.trace("This request is authorized. Account -> {}", account.getAccountId());
         * 
         * // Set thread name for debugging. Thread.currentThread().setName( StringUtils.join("Account:",
         * account.getCompanyId(), ";", account.getAccountId()));
         * 
         * } catch (AuthenticationException e) { log.error(e.getMessage());
         * 
         * @SuppressWarnings("resource") ServletServerHttpResponse serverResponse = new
         * ServletServerHttpResponse(response); serverResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
         * serverResponse.getBody().write(e.getMessage().getBytes(Charset.forName("UTF-8"))); return false; }
         */

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        Thread.currentThread().setName(originalThreadName.get());
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("authorization");
        if (StringUtils.isEmpty(authorization)) {
            log.warn("'Authorization' was not found in request header!");
            return null;
        } else {
            return authorization.replace("Bearer", "").trim();
        }
    }
}
