package com.own.virtualaibox.core;

import com.own.virtualaibox.config.VirtualClockConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class VirtualClock {

    private final VirtualClockConfig clockConfig;

    // 虚拟时间的元时间
    private Instant metaInstant;
    // 当前时间刻度
    private int tick;
    // 当前时间步长
    // 时间步长由配置文件决定，不对外提供修改接口
    // 单位是秒
    // 跟现实世界的时间流速。
    private int interval;

    // 构造器注入
    public VirtualClock(VirtualClockConfig clockConfig) {
        this.clockConfig = clockConfig;
        this.metaInstant = clockConfig.getMetaInstant();
        log.info("VirtualClock initialized: " + metaInstant);
        this.tick = clockConfig.getTick();
        this.interval = clockConfig.getInterval();
    }

    public void stepForward() {
        this.tick++;
        log.info("VirtualClock tick: " + tick);
    }

    public void stepBackward() {
        this.tick--;
        log.info("VirtualClock tick: " + tick);
    }


    public Instant getCurrentTime() {
        log.info("getCurrentTime: " + interval * tick);
        return metaInstant.plusSeconds(interval * tick);
    }

    public int getTick() {
        return tick;
    }

}
