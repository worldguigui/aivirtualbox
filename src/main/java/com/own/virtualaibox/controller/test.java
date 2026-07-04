package com.own.virtualaibox.controller;

import com.own.virtualaibox.grid.VirtualClock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.Instant;

import static java.lang.Thread.sleep;

@Controller
public class test {
    @Autowired
    private VirtualClock virtualClock;

    @GetMapping("/test")
    public String test() {
        Instant now = virtualClock.getCurrentTime();
        System.out.println(now);

        virtualClock.stepForward();
        virtualClock.stepForward();
        virtualClock.stepForward();
        virtualClock.stepForward();


        System.out.println(virtualClock.getCurrentTime());
        try {
            sleep(1000);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(virtualClock.getCurrentTime());
        return "OK";
    }
}
