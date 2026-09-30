package com.own.virtualaibox.mind;

import com.own.virtualaibox.secd.value.AgentRefValue;

/**
 * 感知：Agent 行为程序可查询的只读世界视图。
 *
 * <p>对应 Agent 组成中的 Perception 输入通道（docs §4）。感知是"读"——把值压入
 * SECD S 栈，**不产生 Effect、不触碰世界**。Java 层允许返回 {@code null} 表达
 * "无"，但 null 不出机器边界：{@code WorldOpEvaluator} 在返回处把 null 统一转成
 * {@code #f}（docs §17.2）。</p>
 *
 * <p>快照语义：每个 tick 由 {@code AgentRuntime} 注入一次，同一 tick 内所有 Agent
 * 看到同一个 {@code WorldState} 快照 → 确定性、无顺序假象。</p>
 */
public interface Perception {

    /** 自身引用（恒存在）。 */
    AgentRefValue self();

    /**
     * 到另一 Agent 的曼哈顿距离。目标不存在或超出感知范围 → {@link Integer#MAX_VALUE}
     * 哨兵值（由求值器转 {@code #f}）。
     */
    int distTo(AgentRefValue other);

    /** 对方名称；未知 → {@code "?"}。 */
    String nameOf(AgentRefValue other);

    /** 感知范围内最近的其他 Agent；范围内无其他 Agent → {@code null}。 */
    AgentRefValue closestOther();

    /**
     * 另一 Agent 相对本 Agent 的方向（{@code north/east/south/west}，按主轴判断）。
     * 不在范围、同格或未知 → {@code null}。
     */
    String directionOf(AgentRefValue other);
}
