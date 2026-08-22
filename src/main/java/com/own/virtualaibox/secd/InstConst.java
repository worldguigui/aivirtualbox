package com.own.virtualaibox.secd;

import com.own.virtualaibox.secd.value.Value;

/**
 * 常量指令：执行时把 {@code value} 压入 S 栈。
 * 移植自 lambdaexpr 的 {@code InstConst}。
 */
public class InstConst implements Instruction {
    public final Value value;

    public InstConst(Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
