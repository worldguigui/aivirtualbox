package com.own.virtualaibox;

import com.own.virtualaibox.grid.GridTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

@Slf4j
@SpringBootTest
class VirtualAiBoxApplicationTests {

    @Test
    void contextLoads() {
        GridTest gridTest = new GridTest(1);

        List<String> ids = gridTest.getNearby("1");

        for (String id : ids) {
            log.info(id);
        }

    }

}
