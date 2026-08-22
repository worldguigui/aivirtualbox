package com.own.virtualaibox.effect;

/**
 * 副作用描述（意图），SECD 计算与 World 副作用之间的隔离边界。
 *
 * <p>设计定位（见 docs/secd-fusion-design.md §7）：</p>
 * <pre>
 *   SECD = 计算           → 产生 Effect（意图）
 *   EffectExecutor = 真正执行副作用 → 修改 World
 * </pre>
 *
 * <p>P0 阶段仅定义接口契约，具体副作用类型（MoveEffect/SpeakEffect/…）在 P1 引入。</p>
 */
public interface Effect {

    /**
     * 人类可读描述，用于事件日志与前端展示。
     */
    String describe();
}
