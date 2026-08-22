package com.own.virtualaibox.effect;

/**
 * 移动副作用：Agent 意图向 (deltaX, deltaY) 移动一格。
 * 由 SECD 的 move 原语产生，EffectExecutor 负责落地到 World。
 */
public record MoveEffect(String agentId, int deltaX, int deltaY, String reason) implements Effect {
    @Override
    public String describe() {
        return "move(" + deltaX + "," + deltaY + ")" + (reason == null ? "" : " [" + reason + "]");
    }
}
