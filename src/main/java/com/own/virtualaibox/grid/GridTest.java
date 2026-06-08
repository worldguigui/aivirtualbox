package com.own.virtualaibox.grid;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class GridTest {
    private static final int width = 10;
    private static final int height = 10;
    private Map<String,Integer> id2Block;
    private Map<Integer,List<String>> block2Ids;
    private List<String> ids;
    private static final int[][] directions = new int[][]{
            {-1,-1},{-1,0},{-1,1},
            {0,-1},{0,0},{1,-1},
            {1,0},{1,0},{1,1}
    };

    /**
     * 0 - 默认初始化
     * 1 - 添加三个人物
     * others - 默认初始化
     * @param option
     */
    public GridTest(int option) {
        id2Block = new HashMap<String,Integer>();
        ids = new ArrayList<String>();
        block2Ids = new HashMap<>();

        switch(option) {
            case 0:
                log.info("网格初始化成功，当前网格共有0人");
                break;
                case 1:
                    for(int i = 0; i < 3; i++) {
                        ids.add(String.valueOf(i));
                    }
                    id2Block.put("1",25);
                    id2Block.put("2",25);
                    id2Block.put("3",50);

                    block2Ids.put(25,List.of("1","2"));
                    block2Ids.put(50,List.of("3"));
                    break;
                    default:
                        log.info("网格初始化成功，当前网格共有0人");
        }
    }

    public GridTest() {
        id2Block = new HashMap<String,Integer>();
        ids = new ArrayList<String>();
        block2Ids = new HashMap<>();
    }

    /**
     * 返回所有人物
     * @return
     */
    public List<String> getIds() {
        return ids;
    }

    /**
     * 获取当前id附近的人物
     * @param id
     * @return
     */
    public List<String> getNearby(String id) {
        int curPos = id2Block.getOrDefault(id,-1);

        if (curPos == -1) {
            return null;
        }

        List<String> nearbyIds = new ArrayList<>();
        for(int i = 0; i < directions.length; i++) {
            int nearby = curPos + width * directions[i][0] + directions[i][1];

            if(nearby < 0 || nearby >= width * height) {
                continue;
            }

            // 检索临近格子nearby里的所有人物id
            if(block2Ids.containsKey(nearby)) {
                for(String nearbyId : block2Ids.get(nearby) ) {
                    nearbyIds.add(nearbyId);
                }
            }

        }

        return nearbyIds;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for(Integer block : block2Ids.keySet()) {
            sb.append(block + ": ");
            for(String id : block2Ids.get(block)) {
                sb.append(id + " ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
