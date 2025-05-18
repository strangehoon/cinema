package com.example.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import java.util.List;

@Configuration
public class LuaScriptConfig {

    @Bean
    public DefaultRedisScript<List> getScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("luascript/per_get.lua")));
        script.setResultType(List.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<Long> setScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("luascript/per_set.lua")));
        script.setResultType(Long.class);
        return script;
    }
}
