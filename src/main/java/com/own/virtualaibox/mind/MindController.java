package com.own.virtualaibox.mind;

import com.own.virtualaibox.behaviordsl.BehaviorProgram;
import com.own.virtualaibox.behaviordsl.BehaviorRegistry;
import com.own.virtualaibox.brain.LLMBrain;
import com.own.virtualaibox.domain.agent.Agent;
import com.own.virtualaibox.domain.agent.AgentState;
import com.own.virtualaibox.domain.world.World;
import com.own.virtualaibox.domain.world.WorldState;
import com.own.virtualaibox.effect.Effect;
import com.own.virtualaibox.secd.InstApp;
import com.own.virtualaibox.secd.InstConst;
import com.own.virtualaibox.secd.value.AgentRefValue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 心智控制器（P1）：一个 tick 内驱动所有 Agent 的 SECD 运行时，汇总副作用。
 *
 * <p>取代原决策阶段"每个 tick 直接调 LLMBrain 决定一个动作"的逻辑：
 * 每个 Agent 的心智（AgentRuntime）在 C 栈上持有跨 tick 的行为程序，
 * 每 tick 推进一步并产出 Effect；LLM 仅在计划耗尽时作为 Oracle 被询问方向。</p>
 */
@Component
public class MindController {

    /** onMeet 对话的步数预算（对话程序很小，远小于该值）。 */
    private static final int MAX_HANDLER_STEPS = 200;

    private final LLMBrain llmBrain;
    private final BehaviorRegistry behaviorRegistry;
    private final Map<String, AgentRuntime> runtimes = new ConcurrentHashMap<>();

    public MindController(LLMBrain llmBrain, BehaviorRegistry behaviorRegistry) {
        this.llmBrain = llmBrain;
        this.behaviorRegistry = behaviorRegistry;
    }

    /** 决策阶段：推进所有 Agent 心智一个 tick，返回全部副作用。 */
    public List<Effect> decisionPhase(int tick, World world) {
        WorldState worldState = buildWorldState(tick, world);
        List<Effect> all = new ArrayList<>();
        for (Agent agent : world.getAgents()) {
            AgentRuntime runtime = runtimes.computeIfAbsent(agent.getId(),
                    id -> new AgentRuntime(agent, llmBrain, behaviorRegistry.resolve(agent)));
            all.addAll(runtime.tick(worldState));
        }
        return all;
    }

    /**
     * 相遇处理（P2）：双方各中断当前主计划，执行 onMeet 对话程序，
     * 结束后经 D 栈恢复主计划。
     *
     * @return 对话产生的副作用（SpeakEffect / RememberEffect）
     */
    public List<Effect> onMeet(int tick, Agent a1, Agent a2, World world) {
        List<Effect> effects = new ArrayList<>();
        effects.addAll(interruptWithMeet(a1, a2));
        effects.addAll(interruptWithMeet(a2, a1));
        return effects;
    }

    /** 对 self 中断主计划并执行指向 other 的 onMeet 对话（P5：DSL 定义时用 DSL，否则内置）。 */
    private List<Effect> interruptWithMeet(Agent self, Agent other) {
        AgentRuntime runtime = runtimes.computeIfAbsent(self.getId(),
                id -> new AgentRuntime(self, llmBrain, behaviorRegistry.resolve(self)));
        BehaviorProgram program = runtime.getBehaviorProgram();
        if (program != null && program.onMeet() != null) {
            // apply(onMeet, other)：onMeet 是 DSL 编译的 λ 闭包
            runtime.interrupt(new InstApp(
                    new InstConst(program.onMeet()),
                    new InstConst(new AgentRefValue(other.getId(), other.getName()))));
        } else {
            runtime.interrupt(DefaultPlanCompiler.compileOnMeet(self, other));
        }
        return runtime.runHandler(MAX_HANDLER_STEPS);
    }

    /** P4 可视化：按 Agent ID 取运行时（S/E/C/D 快照）。 */
    public AgentRuntime getRuntime(String agentId) {
        return runtimes.get(agentId);
    }

    public Collection<AgentRuntime> getRuntimes() {
        return runtimes.values();
    }

    private WorldState buildWorldState(int tick, World world) {
        Map<String, AgentState> agentStates = new HashMap<>();
        for (Agent agent : world.getAgents()) {
            agentStates.put(agent.getId(), agent.getState());
        }
        return new WorldState(tick, agentStates);
    }
}
