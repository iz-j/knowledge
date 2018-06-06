package com.izj.knowledge.web.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.izj.knowledge.service.base.time.SystemClock;
import com.izj.knowledge.web.base.interceptor.annotation.HealthCheck;

@RestController
public class HealthCheckController {

    @HealthCheck
    @RequestMapping(value = { "", "/", }, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> home() {
        return new ResponseEntity<String>(
                "Welcome to ~~~~-web-api. I'm healthy!<br>"
                        + SystemClock.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
                HttpStatus.OK);
    }
}
