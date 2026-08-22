package com.own.virtualaibox.mind;

import com.own.virtualaibox.brain.LLMBrain;
import com.own.virtualaibox.domain.agent.Agent;
import com.own.virtualaibox.effect.Effect;
import com.own.virtualaibox.effect.LLMRequestEffect;
import com.own.virtualaibox.effect.MoveEffect;
import com.own.virtualaibox.effect.RememberEffect;
import com.own.virtualaibox.effect.SpeakEffect;
import com.own.virtualaibox.secd.ArithmeticOpEvaluator;
import com.own.virtualaibox.secd.MachineState;
import com.own.virtualaibox.secd.OpEvaluator;
import com.own.virtualaibox.secd.value.AgentRefValue;
import com.own.virtualaibox.secd.value.IntValue;
import com.own.virtualaibox.secd.value.OpValue;
import com.own.virtualaibox.secd.value.PartialOpValue;
import com.own.virtualaibox.secd.value.StringValue;
import com.own.virtualaibox.secd.value.Value;
import com.own.virtualaibox.secd.value.VoidValue;

import java.util.List;

/**
 * 世界原语求值器（P1）：在纯算术之上注入世界副作用原语。
 *
 * <p>原语语义（对应 docs/secd-fusion-design.md §9 DSL 草案，这里以 λ 操作符实现）：</p>
 * <ul>
 *   <li>{@code move dx dy} —— 二元，产生 {@link MoveEffect}</li>
 *   <li>{@code speak target content} —— 二元，产生 {@link SpeakEffect}</li>
 *   <li>{@code remember key content} —— 二元，产生 {@link RememberEffect}</li>
 *   <li>{@code ask-llm prompt} —— 一元，同步调用预言机，把结果文本压入 S</li>
 * </ul>
 *
 * <p>每个 Agent 持有一份独立实例（绑定自身上下文）。纯算术原语委托给
 * {@link ArithmeticOpEvaluator}；move/speak/remember 的第一次应用由算术求值器
 * 压成 {@link PartialOpValue}，第二次应用在这里完成并产出副作用。</p>
 */
public class WorldOpEvaluator implements OpEvaluator {

    private final Agent agent;
    private final LLMBrain llmBrain;
    private final ArithmeticOpEvaluator arithmetic = new ArithmeticOpEvaluator();

    public WorldOpEvaluator(Agent agent, LLMBrain llmBrain) {
        this.agent = agent;
        this.llmBrain = llmBrain;
    }

    @Override
    public List<Effect> apply(MachineState state, Value func, Value arg) {
        // ask-llm 是一元原语，直接拦截执行
        if (func instanceof OpValue op && "ask-llm".equals(op.op)) {
            return askLlm(state, arg);
        }
        // 柯里化二元原语的完成态：move/speak/remember 的第二次应用
        if (func instanceof PartialOpValue partial) {
            switch (partial.op) {
                case "move" -> {
                    return move(state, partial.firstArg, arg);
                }
                case "speak" -> {
                    return speak(state, partial.firstArg, arg);
                }
                case "remember" -> {
                    return remember(state, partial.firstArg, arg);
                }
                default -> {
                    return arithmetic.apply(state, func, arg);
                }
            }
        }
        // 其余（含 move/speak/remember 的第一次应用）委托给纯算术：
        // 非一元 OpValue 会被压成 PartialOpValue，完成柯里化
        return arithmetic.apply(state, func, arg);
    }

    private List<Effect> move(MachineState state, Value dx, Value dy) {
        state.getS().push(new VoidValue());
        if (dx instanceof IntValue ix && dy instanceof IntValue iy) {
            return List.of(new MoveEffect(agent.getId(), ix.val, iy.val, "SECD move"));
        }
        return List.of();
    }

    private List<Effect> speak(MachineState state, Value target, Value content) {
        state.getS().push(new VoidValue());
        String targetId = target instanceof AgentRefValue ref ? ref.agentId : null;
        String text = content instanceof StringValue s ? s.text : content.toString();
        return List.of(new SpeakEffect(agent.getId(), targetId, text));
    }

    private List<Effect> remember(MachineState state, Value key, Value content) {
        state.getS().push(new VoidValue());
        String k = key instanceof StringValue s ? s.text : key.toString();
        String c = content instanceof StringValue s ? s.text : content.toString();
        return List.of(new RememberEffect(agent.getId(), k, c));
    }

    private List<Effect> askLlm(MachineState state, Value prompt) {
        String p = prompt instanceof StringValue s ? s.text : prompt.toString();
        String result = llmBrain.chat(p);
        state.getS().push(new StringValue(result));
        return List.of(new LLMRequestEffect(agent.getId(), p, result));
    }
}
