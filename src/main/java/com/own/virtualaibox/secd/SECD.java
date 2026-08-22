package com.own.virtualaibox.secd;

import com.own.virtualaibox.effect.Effect;
import com.own.virtualaibox.secd.value.ClosureValue;
import com.own.virtualaibox.secd.value.InfiniteValue;
import com.own.virtualaibox.secd.value.Value;
import com.own.virtualaibox.secd.value.VarValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * SECD 抽象机驱动。移植自 lambdaexpr 的 {@code SECDMachine}，并做了两处改造：
 *
 * <ul>
 *   <li>状态与驱动分离：S/E/C/D 收敛进 {@link MachineState}，由调用方持有；</li>
 *   <li>副作用隔离：单步/运行返回 {@link Effect} 列表，机器本身不修改 World。</li>
 * </ul>
 *
 * <p>默认使用 {@link ArithmeticOpEvaluator}（纯算术原语）。
 * P1 起可注入世界原语求值器（move/speak/remember/ask-llm），
 * 此时对应原语以 Effect 形式表达世界副作用。</p>
 */
public class SECD {

    /** 默认最大执行步数，用于防止无限归约。 */
    public static final int DEFAULT_MAX_STEPS = 10_000;

    private final int maxSteps;
    private final OpEvaluator opEvaluator;

    public SECD() {
        this(DEFAULT_MAX_STEPS, new ArithmeticOpEvaluator());
    }

    public SECD(OpEvaluator opEvaluator) {
        this(DEFAULT_MAX_STEPS, opEvaluator);
    }

    public SECD(int maxSteps, OpEvaluator opEvaluator) {
        this.maxSteps = maxSteps;
        this.opEvaluator = opEvaluator;
    }

    /**
     * 单步执行：弹出 C 栈顶指令并按其语义推进状态。
     *
     * @return 本步产生的副作用（纯算术路径为空）
     */
    public List<Effect> step(MachineState state) {
        Instruction inst = state.getC().pop();
        List<Effect> effects = new ArrayList<>();

        // 常量：压入 S
        if (inst instanceof InstConst c) {
            state.getS().push(c.value);
        }
        // 变量：E 有绑定则压绑定，否则压变量本身
        else if (inst instanceof InstVar v) {
            if (state.getE().containsKey(v.name)) {
                state.getS().push(state.getE().get(v.name));
            } else {
                state.getS().push(new VarValue(v.name));
            }
        }
        // 应用：改写 C 顶为 [rator, rand, @]，rator 在顶
        else if (inst instanceof InstApp app) {
            state.getC().push(new InstApply());
            state.getC().push(app.rand);
            state.getC().push(app.rator);
        }
        // λ 抽象：用 (arg, body, 当前 E 的浅拷贝) 构造闭包压入 S
        else if (inst instanceof InstLam lam) {
            state.getS().push(new ClosureValue(lam.arg, lam.body, new HashMap<>(state.getE())));
        }
        // 应用符号 @：弹出 rand、rator 并执行应用
        else if (inst instanceof InstApply) {
            Value rand = state.getS().pop();
            Value rator = state.getS().pop();
            effects.addAll(apply(state, rator, rand));
        }
        // 未知指令
        else {
            throw new IllegalStateException("Unknown instruction: " + inst);
        }

        return effects;
    }

    /**
     * 执行应用 {@code func arg}。
     * 操作符值委托给 {@link OpEvaluator}；闭包则挂起当前状态到 D 栈并开启新求值。
     */
    private List<Effect> apply(MachineState state, Value func, Value arg) {
        // 原语操作符（一元/二元/柯里化中间态）—— 委托给求值器
        if (func instanceof com.own.virtualaibox.secd.value.OpValue
                || func instanceof com.own.virtualaibox.secd.value.PartialOpValue) {
            return opEvaluator.apply(state, func, arg);
        }
        // 闭包：D 栈挂起当前配置，开启闭包 (V, B, E1) 的新求值
        else if (func instanceof ClosureValue cl) {
            Stack<DumpFrame> dump = state.getD();
            // 保存当前配置到 D 栈（S/C/D 浅克隆；E 引用共享，与原实现一致）
            dump.push(new DumpFrame(
                    shallowCloneValues(state.getS()),
                    state.getE(),
                    shallowCloneInstructions(state.getC()),
                    shallowCloneFrames(dump)));
            // 开启新求值：以闭包捕获环境为基，绑定形参
            state.getS().clear();
            state.setE(new HashMap<>(cl.getEnv()));
            state.getE().put(cl.getArg(), arg);
            state.getC().clear();
            state.getC().push(cl.getBody());
            return List.of();
        }
        // 其余类型被当作函数应用：原实现静默跳过，这里显式报错避免静默丢状态
        else {
            throw new IllegalStateException("Cannot apply non-function value '" + func + "' to '" + arg + "'");
        }
    }

    /**
     * 把程序压入 C 栈并运行到求解结束。
     *
     * @return 归约结果（最终值 + 副作用 + 是否发散）
     */
    public Reduction run(MachineState state, Instruction code) {
        state.getC().push(code);
        return run(state);
    }

    /**
     * 从当前状态继续运行，直到 C/D 清空或触发发散检测。
     * 支持"恢复被挂起的计划"（P1/P2 的 resume 语义）。
     */
    public Reduction run(MachineState state) {
        List<Effect> allEffects = new ArrayList<>();
        int stepCount = 0;
        int lastDumpSize = 0;
        int dumpGrowthCount = 0;
        String initialStateSnapshot = state.captureState();

        while (true) {
            // 步数上限 + 发散检测（移植自原 SECDMachine）
            if (stepCount >= maxSteps) {
                boolean infinite = state.getD().size() > maxSteps / 10
                        || (initialStateSnapshot != null && initialStateSnapshot.equals(state.captureState()))
                        || dumpGrowthCount > maxSteps / 20;
                if (infinite) {
                    return new Reduction(
                            new InfiniteValue("infinite reduction detected after " + stepCount
                                    + " steps (D stack size: " + state.getD().size() + ")"),
                            allEffects, true);
                }
                // 非发散：计算复杂但仍在推进，重置计数继续
                stepCount = 0;
                dumpGrowthCount = 0;
            }

            // 监控 D 栈增长
            if (state.getD().size() > lastDumpSize) {
                dumpGrowthCount++;
            }
            lastDumpSize = state.getD().size();

            // 求解结束或从转储帧恢复
            if (state.getC().isEmpty()) {
                if (state.getD().isEmpty()) {
                    break;
                }
                Value retVal = state.getS().pop();
                DumpFrame dump = state.getD().pop();
                state.restore(dump);
                state.getS().push(retVal);
                continue;
            }

            allEffects.addAll(step(state));
            stepCount++;
        }

        return new Reduction(state.getS().isEmpty() ? null : state.getS().peek(), allEffects, false);
    }

    @SuppressWarnings("unchecked")
    private Stack<Value> shallowCloneValues(Stack<Value> src) {
        return (Stack<Value>) src.clone();
    }

    @SuppressWarnings("unchecked")
    private Stack<Instruction> shallowCloneInstructions(Stack<Instruction> src) {
        return (Stack<Instruction>) src.clone();
    }

    @SuppressWarnings("unchecked")
    private Stack<DumpFrame> shallowCloneFrames(Stack<DumpFrame> src) {
        return (Stack<DumpFrame>) src.clone();
    }
}
