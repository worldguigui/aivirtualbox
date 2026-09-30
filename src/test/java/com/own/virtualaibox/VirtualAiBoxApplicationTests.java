package com.own.virtualaibox;

import com.own.virtualaibox.grid.GridTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

@Slf4j
@SpringBootTest
/** 验证应用上下文能够启动并执行网格查询。 */
class VirtualAiBoxApplicationTests {

    /** 加载应用上下文并执行基础网格查询。 */
    @Test
    void contextLoads() {
        GridTest gridTest = new GridTest(1);

        List<String> ids = gridTest.getNearby("1");

        for (String id : ids) {
            log.info(id);
        }

    }

}
