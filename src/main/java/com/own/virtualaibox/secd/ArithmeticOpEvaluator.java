package com.own.virtualaibox.secd;

import com.own.virtualaibox.effect.Effect;
import com.own.virtualaibox.secd.value.AgentRefValue;
import com.own.virtualaibox.secd.value.BoolValue;
import com.own.virtualaibox.secd.value.ClosureValue;
import com.own.virtualaibox.secd.value.DirectionValue;
import com.own.virtualaibox.secd.value.IntValue;
import com.own.virtualaibox.secd.value.OpValue;
import com.own.virtualaibox.secd.value.PartialOpValue;
import com.own.virtualaibox.secd.value.StringValue;
import com.own.virtualaibox.secd.value.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 纯算术操作符求值器，移植自 lambdaexpr {@code SECDMachine.apply()} 中的
 * OpValue / PartialOpValue 分支。默认实现，无任何世界副作用。
 *
 * <p>支持的纯原语：一元 {@code sqr}、{@code succ}；二元 {@code add}、{@code mul}。
 * 当实参不是整数时，构造延迟闭包以便与丘奇数等纯 λ 表达式交互。</p>
 *
 * <p>支持比较原语：二元 {@code eq/lt/gt/le/ge} → {@link BoolValue}；
 * {@code eq} 支持 Int/String/AgentRef（按 id），类型不符的其余比较 → {@code #f}
 * （docs §17.4）。比较仍走柯里化二元通道，可被 WorldOpEvaluator 委托（纯、无世界依赖）。</p>
 */
public class ArithmeticOpEvaluator implements OpEvaluator {

    private static final String[] UNARY_OPS = {"sqr", "succ"};

    private static final Set<String> COMPARE_OPS = Set.of("eq", "lt", "gt", "le", "ge");

    @Override
    public List<Effect> apply(MachineState state, Value func, Value arg) {
        if (func instanceof OpValue op) {
            if (isUnary(op.op)) {
                state.getS().push(computeUnary(state, op.op, arg));
            } else {
                state.getS().push(new PartialOpValue(op.op, arg));
            }
        } else if (func instanceof PartialOpValue partial) {
            state.getS().push(computeBinary(state, partial.op, partial.firstArg, arg));
        } else {
            throw new IllegalStateException("Unsupported operator value: " + func);
        }
        return List.of();
    }

    private boolean isUnary(String op) {
        for (String name : UNARY_OPS) {
            if (name.equals(op)) {
                return true;
            }
        }
        return false;
    }

    private Value computeBinary(MachineState state, String op, Value v1, Value v2) {
        // 比较原语优先处理，避免落入"非整数 → 延迟闭包"分支
        if (COMPARE_OPS.contains(op)) {
            return compare(op, v1, v2);
        }
        if (v1 instanceof IntValue i1 && v2 instanceof IntValue i2) {
            if ("add".equals(op)) return new IntValue(i1.val + i2.val);
            if ("mul".equals(op)) return new IntValue(i1.val * i2.val);
        } else {
            Instruction body = new InstApp(
                    new InstApp(new InstConst(new OpValue(op)), new InstVar("x")),
                    new InstVar("y"));
            return new ClosureValue("y", body, new HashMap<>(state.getE()));
        }
        throw new IllegalStateException("Binary Type error: " + op);
    }

    /** 比较原语：eq 支持 Int/String/AgentRef；lt/gt/le/ge 仅 Int；类型不符 → #f。 */
    private Value compare(String op, Value v1, Value v2) {
        if (v1 instanceof IntValue i1 && v2 instanceof IntValue i2) {
            boolean r = switch (op) {
                case "eq" -> i1.val == i2.val;
                case "lt" -> i1.val < i2.val;
                case "gt" -> i1.val > i2.val;
                case "le" -> i1.val <= i2.val;
                case "ge" -> i1.val >= i2.val;
                default -> false;
            };
            return new BoolValue(r);
        }
        if ("eq".equals(op)) {
            if (v1 instanceof StringValue s1 && v2 instanceof StringValue s2) {
                return new BoolValue(s1.text.equals(s2.text));
            }
            if (v1 instanceof AgentRefValue a1 && v2 instanceof AgentRefValue a2) {
                return new BoolValue(a1.agentId.equals(a2.agentId));
            }
            if (v1 instanceof DirectionValue d1 && v2 instanceof DirectionValue d2) {
                return new BoolValue(d1.dir.equals(d2.dir));
            }
        }
        // 类型不符（含跨类型 eq、非 Int 的 lt/gt/le/ge）→ #f
        return new BoolValue(false);
    }

    private Value computeUnary(MachineState state, String op, Value v) {
        if (v instanceof IntValue i) {
            if ("sqr".equals(op)) return new IntValue(i.val * i.val);
            if ("succ".equals(op)) return new IntValue(i.val + 1);
        } else {
            Instruction body = new InstApp(
                    new InstConst(new OpValue(op)),
                    new InstVar("x"));
            return new ClosureValue("x", body, new HashMap<>(state.getE()));
        }
        throw new IllegalStateException("Unary Type error: " + op);
    }
}
