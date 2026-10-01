package com.own.virtualaibox.domain.world;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldItemDefinition {
    private String id;
    private String name;
    private String type = "resource";
    private int maxCount = 0;
    private int spawnWeight = 0;
    private String description = "";
}
