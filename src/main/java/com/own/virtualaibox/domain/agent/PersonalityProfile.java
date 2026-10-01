package com.own.virtualaibox.domain.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 描述居民稳定的人格、动机、表达方式和知识边界。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalityProfile {
    private String role;
    private String temperament;
    private String speakingStyle;
    private String motivations;
    private String values;
    private String knowledgeBoundary;

    /** 根据居民名称生成可重复的基础人格，保证默认居民拥有稳定差异。 */
    public static PersonalityProfile defaultFor(String name) {
        if ("Alice".equalsIgnoreCase(name)) {
            return new PersonalityProfile(
                    "探索者",
                    "好奇、谨慎",
                    "温和，喜欢先观察再表达",
                    "了解小镇并寻找新地点",
                    "重视事实和互相帮助",
                    "只知道已经感知到或记忆中的世界信息");
        }
        if ("Bob".equalsIgnoreCase(name)) {
            return new PersonalityProfile(
                    "社区居民",
                    "热情、主动",
                    "直接、友好，愿意发起对话",
                    "认识其他居民并建立联系",
                    "重视承诺和社区关系",
                    "只知道已经感知到或记忆中的世界信息");
        }
        return new PersonalityProfile(
                "居民",
                "平和、谨慎",
                "简洁、礼貌",
                "在小镇中生活并理解周围环境",
                "重视安全和真实经历",
                "只知道已经感知到或记忆中的世界信息");
    }

    /** 将人格转换为稳定的决策上下文，供 Oracle 读取。 */
    public String toPromptContext() {
        return String.format(
                "身份：%s\n性情：%s\n表达方式：%s\n当前动机：%s\n重视的价值：%s\n知识边界：%s",
                role, temperament, speakingStyle, motivations, values, knowledgeBoundary);
    }

    /** 返回一个稳定的有效人格配置，确保空字段仍可回落到默认人格。 */
    public static PersonalityProfile normalizeFor(String name, PersonalityProfile profile) {
        PersonalityProfile fallback = defaultFor(name);
        if (profile == null) {
            return fallback;
        }
        return new PersonalityProfile(
                profile.getRole() == null || profile.getRole().isBlank() ? fallback.getRole() : profile.getRole().trim(),
                profile.getTemperament() == null || profile.getTemperament().isBlank() ? fallback.getTemperament() : profile.getTemperament().trim(),
                profile.getSpeakingStyle() == null || profile.getSpeakingStyle().isBlank() ? fallback.getSpeakingStyle() : profile.getSpeakingStyle().trim(),
                profile.getMotivations() == null || profile.getMotivations().isBlank() ? fallback.getMotivations() : profile.getMotivations().trim(),
                profile.getValues() == null || profile.getValues().isBlank() ? fallback.getValues() : profile.getValues().trim(),
                profile.getKnowledgeBoundary() == null || profile.getKnowledgeBoundary().isBlank() ? fallback.getKnowledgeBoundary() : profile.getKnowledgeBoundary().trim());
    }
}
