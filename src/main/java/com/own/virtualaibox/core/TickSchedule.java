package com.own.virtualaibox.core;

import com.own.virtualaibox.domain.agent.Agent;
import com.own.virtualaibox.domain.event.EventBus;
import com.own.virtualaibox.domain.event.events.AgentMetEvent;
import com.own.virtualaibox.domain.event.events.TickEndedEvent;
import com.own.virtualaibox.domain.event.events.TickStartedEvent;
import com.own.virtualaibox.domain.world.World;
import com.own.virtualaibox.effect.Effect;
import com.own.virtualaibox.executor.EffectExecutor;
import com.own.virtualaibox.mind.MindController;
import com.own.virtualaibox.monitor.ConvergenceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tick 调度器（5 阶段骨架）。
 *
 * <p>P1 起决策阶段不再直接调用 LLMBrain，而是由 {@link MindController}
 * 驱动每个 Agent 的 SECD 运行时（跨 tick 的行为程序）产出 {@link Effect}；
 * 执行阶段由 {@link EffectExecutor} 把副作用落地到 World。
 * SECD=计算、Effect=意图、EffectExecutor=执行副作用，三者隔离。</p>
 */
@Component
@Slf4j
public class TickSchedule {

    private final MindController mindController;
    private final EffectExecutor effectExecutor;
    private final EventBus eventBus;
    private final ConvergenceMonitor convergenceMonitor;

    /** 当前已处于相遇状态的 pair（id1|id2 排序键），用于"首次相遇才对话"去抖。 */
    private final Set<String> activeMeetings = ConcurrentHashMap.newKeySet();

    public TickSchedule(MindController mindController, EffectExecutor effectExecutor,
                        EventBus eventBus, ConvergenceMonitor convergenceMonitor) {
        this.mindController = mindController;
        this.effectExecutor = effectExecutor;
        this.eventBus = eventBus;
        this.convergenceMonitor = convergenceMonitor;
    }

    public void processTick(int tick, World world) {
        long startTime = System.currentTimeMillis();
        log.info("TickSchedule: Processing tick {}", tick);

        try {
            // Phase 1: 发布Tick开始事件
            publishTickStarted(tick);

            // Phase 2: 决策阶段 - 心智（SECD）推进所有Agent，产出副作用
            List<Effect> effects = decisionPhase(tick, world);

            // Phase 3: 执行阶段 - 落地全部副作用
            executionPhase(tick, effects, world);

            // Phase 4: 交互检测阶段 - 检测Agent相遇
            interactionPhase(tick, world);

            // Phase 4.5: 收敛检测阶段（P3） - 不动点/卡死/周期振荡/无限归约
            convergencePhase(tick, world);

            // Phase 5: 发布Tick结束事件
            long executionTime = System.currentTimeMillis() - startTime;
            publishTickEnded(tick, executionTime);

            log.info("TickSchedule: Tick {} completed in {}ms", tick, executionTime);

        } catch (Exception e) {
            log.error("TickSchedule: Error processing tick {}", tick, e);
        }
    }

    /**
     * Phase 1: 发布Tick开始事件
     */
    private void publishTickStarted(int tick) {
        TickStartedEvent event = new TickStartedEvent();
        event.setEventId("tick_start_" + tick);
        event.setTick(tick);
        event.setTimestamp(Instant.now());
        event.setSourceSystem("tick-schedule");
        event.setPriority(10);  // 最高优先级

        eventBus.publish(event);
    }

    /**
     * Phase 2: 决策阶段
     */
    private List<Effect> decisionPhase(int tick, World world) {
        log.info("TickSchedule: Entering decision phase, current tick: {}", tick);
        return mindController.decisionPhase(tick, world);
    }

    /**
     * Phase 3: 执行阶段
     */
    private void executionPhase(int tick, List<Effect> effects, World world) {
        log.info("TickSchedule: Entering execution phase, current tick: {}", tick);
        effectExecutor.execute(effects, world);
    }

    /**
     * Phase 4: 交互检测阶段
     *
     * <p>检测相遇 → 发布 AgentMetEvent → <b>首次相遇</b>触发 onMeet 对话
     * （双方各中断当前 SECD 主计划、执行对话程序、经 D 栈恢复），
     * 对话副作用本 tick 落地（P2）。</p>
     */
    private void interactionPhase(int tick, World world) {
        log.info("TickSchedule: Entering interaction detection phase, current tick: {}", tick);

        List<Agent> agents = world.getAgents();
        List<Effect> interactionEffects = new ArrayList<>();
        Set<String> currentMeets = new HashSet<>();

        // 检测所有Agent对的相遇
        for (int i = 0; i < agents.size(); i++) {
            for (int j = i + 1; j < agents.size(); j++) {
                Agent agent1 = agents.get(i);
                Agent agent2 = agents.get(j);

                double distance = calculateDistance(agent1, agent2);

                // 如果距离 <= 1.5 格，视为相遇
                if (distance <= 1.5) {
                    String pairKey = pairKey(agent1, agent2);
                    currentMeets.add(pairKey);
                    publishAgentMet(tick, agent1, agent2, distance);

                    // 首次相遇才触发对话，避免同 pair 每个 tick 都打招呼
                    if (!activeMeetings.contains(pairKey)) {
                        interactionEffects.addAll(mindController.onMeet(tick, agent1, agent2, world));
                    }
                }
            }
        }

        // 落地对话副作用（SECD 计算 → 意图 → 本 tick 执行）
        if (!interactionEffects.isEmpty()) {
            effectExecutor.execute(interactionEffects, world);
        }

        activeMeetings.clear();
        activeMeetings.addAll(currentMeets);
    }

    /**
     * Phase 4.5: 收敛检测阶段（P3）
     *
     * <p>观察执行后的世界状态，检测不动点（WorldConverged）、Agent 卡死（AgentStuck）、
     * 位置周期（AgentLoop）与 SECD D 栈膨胀（无限归约启发式），发布对应事件。
     * 检测在 interactionPhase 之后、publishTickEnded 之前进行，
     * 使收敛事件计入本 tick 的事件统计。</p>
     */
    private void convergencePhase(int tick, World world) {
        log.info("TickSchedule: Entering convergence detection phase, current tick: {}", tick);
        convergenceMonitor.monitor(tick, world);
    }

    /** 相遇 pair 的排序键（与顺序无关）。 */
    private String pairKey(Agent a1, Agent a2) {
        return a1.getId().compareTo(a2.getId()) <= 0
                ? a1.getId() + "|" + a2.getId()
                : a2.getId() + "|" + a1.getId();
    }

    /**
     * Phase 5: 发布Tick结束事件
     */
    private void publishTickEnded(int tick, long executionTime) {
        TickEndedEvent event = new TickEndedEvent();
        event.setEventId("tick_end_" + tick);
        event.setTick(tick);
        event.setTimestamp(Instant.now());
        event.setSourceSystem("tick-schedule");
        event.setPriority(1);  // 最低优先级

        // 获取事件历史中本Tick的事件数
        int eventCount = (int) eventBus.getEventHistory(1000).stream()
                .filter(e -> e.getTick() == tick)
                .count();

        event.setEventCount(eventCount);
        event.setExecutionTime(executionTime);

        eventBus.publish(event);
    }

    private void publishAgentMet(int tick, Agent agent1, Agent agent2, double distance) {
        AgentMetEvent event = new AgentMetEvent();
        event.setEventId(agent1.getId() + "_met_" + agent2.getId() + "_" + tick);
        event.setTick(tick);
        event.setTimestamp(Instant.now());
        event.setSourceSystem("interaction-detector");
        event.setPriority(8);  // 高优先级
        event.setAgentId1(agent1.getId());
        event.setAgentName1(agent1.getName());
        event.setAgentId2(agent2.getId());
        event.setAgentName2(agent2.getName());
        event.setMeetX((agent1.getState().getX() + agent2.getState().getX()) / 2);
        event.setMeetY((agent1.getState().getY() + agent2.getState().getY()) / 2);
        event.setDistance(distance);

        eventBus.publish(event);
        log.info("TickSchedule: Detected agents meeting - {} and {}", agent1.getName(), agent2.getName());
    }

    private double calculateDistance(Agent agent1, Agent agent2) {
        int dx = agent1.getState().getX() - agent2.getState().getX();
        int dy = agent1.getState().getY() - agent2.getState().getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
