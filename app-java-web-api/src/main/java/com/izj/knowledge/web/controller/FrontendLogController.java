package com.izj.knowledge.web.controller;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.izj.knowledge.web.base.interceptor.annotation.NoAuthencitation;

@Slf4j
@RestController
@RequestMapping("/log")
public class FrontendLogController {

    @NoAuthencitation
    @RequestMapping(value = "/error", method = RequestMethod.POST)
    public void error(@RequestBody String clientLog, HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        log.error(new StringBuilder()
            .append("Frontend error log received!")
            .append(System.lineSeparator())
            .append(" [User-Agent] ")
            .append(ua)
            .append(System.lineSeparator())
            .append(" [Log] ")
            .append(clientLog)
            .toString());
    }
}
