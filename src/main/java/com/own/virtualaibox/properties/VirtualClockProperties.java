package com.own.virtualaibox.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@ConfigurationProperties(prefix = "spring.virtual-clock")
public class VirtualClockProperties {

    private int tick;

    private int interval;

    private Instant metaInstant;

}
