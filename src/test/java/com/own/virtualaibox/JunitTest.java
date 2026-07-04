package com.own.virtualaibox;

import com.own.virtualaibox.grid.GridTest;
import com.own.virtualaibox.grid.VirtualClock;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static java.lang.Thread.sleep;

public class JunitTest {
    @Test
    public void test() {
        GridTest gridTest = new GridTest(1);

        List<String> ids = gridTest.getNearby("1");

        for (String id : ids) {
            System.out.println(id);
        }
    }

}
