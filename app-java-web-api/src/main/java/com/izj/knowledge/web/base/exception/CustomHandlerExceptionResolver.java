package com.izj.knowledge.web.base.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.izj.dynamodb.exception.ConditionalUpdateFailedException;
import com.izj.dynamodb.exception.ThroughputExceedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomHandlerExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        /*
         * if (ex instanceof ServiceException) { log.warn("Request failed.", ex); // ServiceException is within
         * expectations so WARN level.
         * 
         * // ServiceException should be mapped to 400. ServiceException e = (ServiceException)ex; ModelAndView mv =
         * createJsonViewWithStatus( e.getErrorCode() == null ? HttpStatus.BAD_REQUEST : e.getErrorCode().httpStatus);
         * mv.addObject("message", e.getMessage()); if (e.getErrorCode() != null) { mv.addObject("code",
         * e.getErrorCode().toString()); } if (e.getValidationError() != null) { mv.addObject("error",
         * e.getValidationError().expose()); } return mv; }
         */

        // Just logging.
        log.error("Request failed.", ex);
        if (ex instanceof ConditionalUpdateFailedException) {
            // Failure of updating resources caused by exclusive control should be mapped to 409.
            ModelAndView mv = createJsonViewWithStatus(HttpStatus.CONFLICT);
            return mv;
        } else if (ex instanceof ThroughputExceedException) {
            log.info("Caught ThroughputExceedException, return status#408(Request timeout).");
            ModelAndView mv = createJsonViewWithStatus(HttpStatus.TOO_MANY_REQUESTS);
            return mv;
        } else {
            return null;
        }
    }

    private ModelAndView createJsonViewWithStatus(HttpStatus status) {
        ModelAndView mv = new ModelAndView();
        mv.setView(new MappingJackson2JsonView());
        mv.setStatus(status);
        return mv;
    }

}
