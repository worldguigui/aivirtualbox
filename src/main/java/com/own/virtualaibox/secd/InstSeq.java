package com.own.virtualaibox.secd;

import java.util.List;

/**
 * 指令序列：执行时按顺序把内部指令压入 C 栈（先执行的压在上方）。
 * P1 新增，用于表达"行为程序 = 一组动作的编排"（跨 tick 驻留于 C 栈）。
 */
public class InstSeq implements Instruction {
    public final List<Instruction> instructions;

    public InstSeq(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(seq");
        for (Instruction inst : instructions) {
            sb.append(" ").append(inst);
        }
        return sb.append(")").toString();
    }
}
