package com.own.virtualaibox.secd;

import com.own.virtualaibox.effect.Effect;
import com.own.virtualaibox.secd.value.ClosureValue;
import com.own.virtualaibox.secd.value.IntValue;
import com.own.virtualaibox.secd.value.OpValue;
import com.own.virtualaibox.secd.value.PartialOpValue;
import com.own.virtualaibox.secd.value.Value;

import java.util.HashMap;
import java.util.List;

/**
 * 纯算术操作符求值器，移植自 lambdaexpr {@code SECDMachine.apply()} 中的
 * OpValue / PartialOpValue 分支。P0 的默认实现，无任何世界副作用。
 *
 * <p>支持的纯原语：一元 {@code sqr}、{@code succ}；二元 {@code add}、{@code mul}。
 * 当实参不是整数时，构造延迟闭包以便与丘奇数等纯 λ 表达式交互。</p>
 */
public class ArithmeticOpEvaluator implements OpEvaluator {

    private static final String[] UNARY_OPS = {"sqr", "succ"};

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
