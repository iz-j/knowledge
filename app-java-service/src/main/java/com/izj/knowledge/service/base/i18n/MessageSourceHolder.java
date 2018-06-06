package com.izj.knowledge.service.base.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author ~~~~
 *
 */
@Slf4j
public final class MessageSourceHolder {

    private static MessageSourceHolder instance = new MessageSourceHolder();

    private MessageSource ms;

    private MessageSourceHolder() {
        log.debug("Preparing message source...");
        ReloadableResourceBundleMessageSource rrbms = new ReloadableResourceBundleMessageSource();
        rrbms.addBasenames("classpath:i18n/base");
        rrbms.addBasenames("classpath:i18n/common");
        rrbms.addBasenames("classpath:i18n/supplier");
        rrbms.addBasenames("classpath:i18n/buyer");
        rrbms.setDefaultEncoding("UTF-8");
        this.ms = rrbms;
    }

    public static MessageSource get() {
        return instance.ms;
    }
}
