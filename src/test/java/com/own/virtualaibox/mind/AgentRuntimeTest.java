package com.own.virtualaibox.mind;

import com.own.virtualaibox.brain.LLMBrain;
import com.own.virtualaibox.domain.action.MoveAction;
import com.own.virtualaibox.domain.agent.Agent;
import com.own.virtualaibox.domain.agent.AgentState;
import com.own.virtualaibox.domain.world.WorldState;
import com.own.virtualaibox.effect.Effect;
import com.own.virtualaibox.effect.LLMRequestEffect;
import com.own.virtualaibox.effect.MoveEffect;
import com.own.virtualaibox.effect.RememberEffect;
import com.own.virtualaibox.effect.SpeakEffect;
import com.own.virtualaibox.secd.MachineState;
import com.own.virtualaibox.secd.value.StringValue;
import com.own.virtualaibox.secd.value.Value;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P1 融合验证：SECD 心智运行时产出 Effect，世界副作用与计算隔离。
 *
 * <p>验证点（docs/secd-fusion-design.md §P1 验收）：</p>
 * <ul>
 *   <li>心智驱动：AgentRuntime 每 tick 产出移动副作用，不再由 LLM 直接决定每个动作；</li>
 *   <li>多 tick 计划：行为程序驻留 C 栈，每 tick 只推进一步动作，耗尽后自动重编译；</li>
 *   <li>副作用隔离：WorldOpEvaluator 只返回 Effect，不修改任何世界状态。</li>
 * </ul>
 *
 * <p>用 FakeBrain（固定方向）替代真实 LLM，使测试确定性可重复。</p>
 */
class AgentRuntimeTest {

    /** 固定返回向东 (1,0) 的假预言机。 */
    private static class FakeBrain extends LLMBrain {
        FakeBrain() {
            super(null); // 不接真实 ChatModel；所有方法被重写，不会触发网络调用
        }

        @Override
        public MoveAction decideAction(Agent agent, WorldState worldState) {
            MoveAction a = new MoveAction();
            a.setAgentId(agent.getId());
            a.setDeltaX(1);
            a.setDeltaY(0);
            a.setReason("fake: head east");
            return a;
        }

        @Override
        public String chat(String prompt) {
            return "fake-llm-reply";
        }
    }

    private final FakeBrain fakeBrain = new FakeBrain();

    private AgentRuntime newRuntime(Agent agent) {
        return new AgentRuntime(agent, fakeBrain);
    }

    private Agent newAgent(String id, String name, int x, int y) {
        Agent agent = new Agent();
        agent.setId(id);
        agent.setName(name);
        agent.setState(new AgentState(x, y, name));
        return agent;
    }

    private WorldState worldState(Agent agent, int tick) {
        return new WorldState(tick, Map.of(agent.getId(), agent.getState()));
    }

    @Test
    void firstTickProducesRememberThenMove() {
        Agent agent = newAgent("a1", "Alice", 10, 10);
        AgentRuntime runtime = newRuntime(agent);

        List<Effect> effects = runtime.tick(worldState(agent, 1));

        // 计划开头是记忆写入，随后是一步移动（P1 默认行为程序）
        assertTrue(effects.stream().anyMatch(e -> e instanceof RememberEffect),
                "计划开头应产出记忆副作用");
        MoveEffect move = (MoveEffect) effects.stream()
                .filter(e -> e instanceof MoveEffect).findFirst().orElseThrow();
        assertEquals(1, move.deltaX());
        assertEquals(0, move.deltaY());

        // 一步动作后计划未耗尽：C 栈仍驻留剩余路径
        assertFalse(runtime.isIdle(), "首 tick 后应还有剩余计划步");
    }

    @Test
    void planPersistsAcrossTicksAndReloads() {
        Agent agent = newAgent("a1", "Alice", 10, 10);
        AgentRuntime runtime = newRuntime(agent);

        // tick1~3：每 tick 恰好一个移动动作（PATH_LENGTH=3）
        for (int t = 1; t <= 3; t++) {
            List<Effect> effects = runtime.tick(worldState(agent, t));
            long moves = effects.stream().filter(e -> e instanceof MoveEffect).count();
            assertEquals(1, moves, "tick " + t + " 应恰好一步移动");
        }
        // 计划耗尽
        assertTrue(runtime.isIdle(), "3 步路径走完后应处于空闲（计划耗尽）");

        // tick4：空闲 → 自动重编译新计划，继续有动作
        List<Effect> effects = runtime.tick(worldState(agent, 4));
        assertTrue(effects.stream().anyMatch(e -> e instanceof MoveEffect),
                "空闲后应自动重编译新计划并产出移动副作用");
        assertFalse(runtime.isIdle(), "重编译后不应再空闲");
    }

    @Test
    void interruptResumesMainPlanViaDStack() {
        Agent agent = newAgent("a1", "Alice", 10, 10);
        Agent other = newAgent("b2", "Bob", 10, 11);
        AgentRuntime runtime = newRuntime(agent);

        // 主计划走一步（remember + move1），C 栈仍驻留剩余路径
        runtime.tick(worldState(agent, 1));
        assertTrue(runtime.getState().getC().size() > 0, "主计划应驻留于 C 栈");

        // 相遇 → 中断主计划，现场压入 D 栈
        runtime.interrupt(DefaultPlanCompiler.compileOnMeet(agent, other));
        assertEquals(1, runtime.getState().getD().size(), "中断后 D 栈应有中断帧");

        // 处理中断：跑完 onMeet 对话（λ 闭包应用会再占一帧），随后解开全部续体恢复主计划
        List<Effect> handlerEffects = runtime.runHandler(200);
        assertTrue(handlerEffects.stream().anyMatch(e -> e instanceof SpeakEffect),
                "onMeet 对话应产出说话副作用");
        assertTrue(handlerEffects.stream().anyMatch(e -> e instanceof RememberEffect),
                "onMeet 对话应产出记忆副作用");
        assertTrue(runtime.getState().getD().isEmpty(), "对话结束后 D 栈应清空（中断帧已解开）");

        // 主计划已恢复：下一 tick 继续剩余路径（方向不变，不再重新决策）
        List<Effect> resumed = runtime.tick(worldState(agent, 2));
        MoveEffect move = (MoveEffect) resumed.stream()
                .filter(e -> e instanceof MoveEffect).findFirst().orElseThrow();
        assertEquals(1, move.deltaX());
        assertEquals(0, move.deltaY());
    }

    @Test
    void worldOpEvaluatorProducesEffectsNotWorldMutation() {
        Agent agent = newAgent("a1", "Alice", 10, 10);
        WorldOpEvaluator evaluator = new WorldOpEvaluator(agent, fakeBrain);
        MachineState state = new MachineState();

        // move 原语：((move 1) 0) -> MoveEffect
        List<Effect> moveEffects = evaluator.apply(state,
                new com.own.virtualaibox.secd.value.PartialOpValue("move",
                        new com.own.virtualaibox.secd.value.IntValue(1)),
                new com.own.virtualaibox.secd.value.IntValue(0));
        assertInstanceOf(MoveEffect.class, moveEffects.get(0));

        // speak 原语：(speak target) content -> SpeakEffect
        List<Effect> speakEffects = evaluator.apply(state,
                new com.own.virtualaibox.secd.value.PartialOpValue("speak",
                        new com.own.virtualaibox.secd.value.AgentRefValue("b2", "Bob")),
                new StringValue("hello"));
        assertInstanceOf(SpeakEffect.class, speakEffects.get(0));

        // remember 原语：(remember key) content -> RememberEffect
        List<Effect> rememberEffects = evaluator.apply(state,
                new com.own.virtualaibox.secd.value.PartialOpValue("remember",
                        new StringValue("goal")),
                new StringValue("head-east"));
        assertInstanceOf(RememberEffect.class, rememberEffects.get(0));

        // ask-llm 原语：同步咨询预言机，把结果压入 S，返回信息性 Effect
        List<Effect> llmEffects = evaluator.apply(state,
                new com.own.virtualaibox.secd.value.OpValue("ask-llm"),
                new StringValue("what now?"));
        assertInstanceOf(LLMRequestEffect.class, llmEffects.get(0));
        Value top = state.getS().peek();
        assertTrue(top instanceof StringValue);
        assertEquals("fake-llm-reply", ((StringValue) top).text);

        // 全程未触碰 Agent 位置（副作用隔离）
        assertEquals(10, agent.getState().getX());
        assertEquals(10, agent.getState().getY());
    }
}
