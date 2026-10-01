package com.own.virtualaibox.domain.world;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 世界运行配置：尺寸、默认居民与初始化参数。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "virtual-ai-box.world")
public class WorldConfig {
    private String name = "VirtualAIBox Town";
    private int width = 37;
    private int height = 37;
    private Map<String, Boolean> rules = new LinkedHashMap<>(Map.of(
            "movement", true,
            "socialInteraction", true,
            "itemSpawning", true,
            "combat", false,
            "trading", false));
    private List<WorldItemDefinition> items = List.of();
    private List<DefaultResident> defaultResidents = List.of(
            new DefaultResident("Alice", 6, 2),
            new DefaultResident("Bob", 18, 17));

    public static WorldConfig defaultConfig() {
        return new WorldConfig("VirtualAIBox Town", 37, 37, new LinkedHashMap<>(Map.of(
                "movement", true,
                "socialInteraction", true,
                "itemSpawning", true,
                "combat", false,
                "trading", false)), List.of(), List.of(
                new DefaultResident("Alice", 6, 2),
                new DefaultResident("Bob", 18, 17)));
    }

    public void validate() {
        if (width < 1 || width > 1000 || height < 1 || height > 1000) {
            throw new IllegalArgumentException("world dimensions must be between 1 and 1000");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("world name must not be blank");
        }
        if (rules == null) {
            rules = new LinkedHashMap<>();
        }
        if (items == null) {
            items = List.of();
        }
        items.forEach(item -> {
            if (item == null || item.getId() == null || item.getId().isBlank()
                    || item.getName() == null || item.getName().isBlank()) {
                throw new IllegalArgumentException("world items require non-blank id and name");
            }
            if (item.getMaxCount() < 0 || item.getSpawnWeight() < 0) {
                throw new IllegalArgumentException("world item counts and weights must not be negative");
            }
        });
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefaultResident {
        private String name;
        private int x;
        private int y;
    }
}
