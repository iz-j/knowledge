package com.izj.knowledge.service.test;

import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;

import com.izj.knowledge.service.system.TenantHolder;
import com.izj.knowledge.service.system.tenant.Tenant;

/**
 * Provide some environmental basic beans for testing.
 * <ul>
 * <li>EnvironmentHolder</li>
 * <li>Tenants & TenantHolder</li>
 * </ul>
 *
 * @author iz-j
 *
 */
public class TestBaseConfig {

    @PostConstruct
    public void postConstruct() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.ENGLISH);
    }

    private static final Tenant t = Tenant
        .builder()
        .id("ishikawa-denko")
        .serviceName("Ishikawa Electric Group WEB-EDI")
        .adminName("Ishikawa Electric Group")
        .email("~~~~")
        .siteUrl("http://localhost:4200")
        .build();

    @Bean
    public TenantHolder tenantHolder() {
        return new TenantHolder() {

            @Override
            public Tenant get() {
                return t;
            }
        };
    }

}
