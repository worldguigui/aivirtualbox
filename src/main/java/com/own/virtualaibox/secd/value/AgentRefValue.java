package com.own.virtualaibox.secd.value;

/**
 * Agent 引用值：指向世界中另一个 Agent 的句柄。
 * P1 新增，用于 speak/observe 等"以 Agent 为实参"的原语。
 */
public class AgentRefValue implements Value {
    public final String agentId;
    public final String agentName;

    public AgentRefValue(String agentId, String agentName) {
        this.agentId = agentId;
        this.agentName = agentName;
    }

    @Override
    public String toString() {
        return "<agent:" + agentName + ">";
    }
}
