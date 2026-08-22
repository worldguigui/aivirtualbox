package com.own.virtualaibox.effect;

/**
 * 说话副作用：Agent 向目标（null=广播）表达内容。
 * 由 SECD 的 speak 原语产生。
 */
public record SpeakEffect(String agentId, String targetId, String content) implements Effect {
    @Override
    public String describe() {
        return "speak->" + (targetId == null ? "*" : targetId) + ": " + content;
    }
}
