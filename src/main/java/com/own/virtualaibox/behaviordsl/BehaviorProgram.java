package com.own.virtualaibox.behaviordsl;

import com.own.virtualaibox.secd.Instruction;
import com.own.virtualaibox.secd.value.Value;

import java.util.Map;

/**
 * P5 行为程序：一个 {@code *.lambda} 文件编译后的结果。
 *
 * <p>契约（docs/secd-fusion-design.md §10 / §P5）：</p>
 * <ul>
 *   <li>{@link #plan()}：入口程序（Instruction），直接内联引用自由变量
 *       {@code dx}/{@code dy}；运行时把 E 种子为 {@code defs + {dx,dy}} 后执行；</li>
 *   <li>{@link #defs()}：顶层 def 环境（λ 行为求值出的闭包值），运行时作为 E 基底；</li>
 *   <li>{@link #onMeet()}：{@code onMeet} def 的闭包值（相遇对话行为），无则 {@code null}，
 *       回退到内置的 {@link com.own.virtualaibox.mind.DefaultPlanCompiler#compileOnMeet}。</li>
 * </ul>
 */
public record BehaviorProgram(
        Instruction plan,
        Map<String, Value> defs,
        Value onMeet
) {
}
