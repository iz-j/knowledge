package com.izj.knowledge.service.system;

import org.springframework.context.annotation.Import;

import com.izj.knowledge.service.system.counter.ItemCounterConfig;

@Import({
        ItemCounterConfig.class,

})
public class SystemConfig {

}
