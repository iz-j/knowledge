package com.izj.knowledge.service;

import org.springframework.context.annotation.Import;

import com.izj.knowledge.service.base.BaseConfig;
import com.izj.knowledge.service.common.CommonConfig;
import com.izj.knowledge.service.system.SystemConfig;

@Import({
        BaseConfig.class,
        SystemConfig.class,
        CommonConfig.class, })
public class ServiceConfig {

}
