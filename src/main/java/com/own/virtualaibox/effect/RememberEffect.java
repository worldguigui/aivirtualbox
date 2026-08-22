package com.own.virtualaibox.effect;

/**
 * 记忆副作用：Agent 把一段内容写入自己的记忆（Memory ≠ E）。
 * 由 SECD 的 remember 原语产生。
 */
public record RememberEffect(String agentId, String key, String content) implements Effect {
    @Override
    public String describe() {
        return "remember[" + key + "]: " + content;
    }
}
