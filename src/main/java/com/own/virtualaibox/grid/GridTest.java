package com.own.virtualaibox.grid;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class GridTest {
    private static final int width = 10;
    private static final int height = 10;
    private Map<String,Integer> id2Block;
    private List<Integer> ids;

    /**
     * 0 - 默认初始化
     * 1 - 添加三个人物
     * others - 默认初始化
     * @param option
     */
    public GridTest(int option) {
        id2Block = new HashMap<String,Integer>();
        ids = new ArrayList<Integer>();

        switch(option) {
            case 0:
                log.info("网格初始化成功，当前网格共有0人");
                break;
                case 1:
                    for(int i = 0; i < 3; i++) {
                        ids.add(i);
                    }
                    id2Block.put("1",25);
                    id2Block.put("2",25);
                    id2Block.put("3",50);
                    break;
                    default:
                        log.info("网格初始化成功，当前网格共有0人");
        }
    }

    public GridTest() {
        id2Block = new HashMap<String,Integer>();
        ids = new ArrayList<Integer>();
    }

    public List<Integer> getIds() {
        return ids;
    }
}
