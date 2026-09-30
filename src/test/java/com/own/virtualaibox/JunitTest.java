package com.own.virtualaibox;

import com.own.virtualaibox.grid.GridTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.lang.Thread.sleep;

/** 验证网格邻近查询的基础测试。 */
public class JunitTest {
    /** 执行网格邻近查询并输出查询结果。 */
    @Test
    public void test() {
        GridTest gridTest = new GridTest(1);

        List<String> ids = gridTest.getNearby("1");

        for (String id : ids) {
            System.out.println(id);
        }
        System.out.println("DEEPSEEK_API_KEY = " + System.getenv("DEEPSEEK_API_KEY"));
    }

}
