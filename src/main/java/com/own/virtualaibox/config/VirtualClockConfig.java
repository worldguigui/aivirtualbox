package com.own.virtualaibox.config;

import com.own.virtualaibox.properties.VirtualClockProperties;
import lombok.Data;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Data
@Configuration
@EnableConfigurationProperties(VirtualClockProperties.class)
public class VirtualClockConfig {

    private final int tick;
    private final Instant metaInstant;
    private final int interval;

    public VirtualClockConfig(VirtualClockProperties properties) {
        this.tick = properties.getTick();
        this.metaInstant = properties.getMetaInstant();
        this.interval = properties.getInterval();
    }
}
