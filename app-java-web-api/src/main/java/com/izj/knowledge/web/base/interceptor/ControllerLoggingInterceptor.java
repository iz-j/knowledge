package com.izj.knowledge.web.base.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.core.Ordered;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author iz-j
 *
 */
@Slf4j
public class ControllerLoggingInterceptor implements HandlerInterceptor, Ordered {

    @Override
    public int getOrder() {
        return InterceptorOrder.LOGGING.value();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!shouldLog(handler)) {
            return true;
        }
        StopWatch sw = new StopWatch();
        sw.start();
        request.setAttribute(ControllerLoggingInterceptor.class.getName(), sw);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        if (!shouldLog(handler)) {
            return;
        }
        StopWatch sw = (StopWatch)request.getAttribute(ControllerLoggingInterceptor.class.getName());
        sw.stop();
        log.debug("API process completed. {}:{}, time -> {}ms",
                request.getMethod(), request.getRequestURI(), sw.getTime());
    }

    private boolean shouldLog(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;// Mainly ignore preflight request.
        }
        if (((HandlerMethod)handler).getBean() instanceof BasicErrorController) {
            return false;// No log for Spring BasicErrorController.
        }
        return true;
    }
}
