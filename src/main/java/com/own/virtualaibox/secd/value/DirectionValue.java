package com.own.virtualaibox.secd.value;

/**
 * 方向常量值：{@code north/east/south/west}。
 *
 * <p>方向→delta 的映射在 {@code WorldOpEvaluator}（DSL 原语层）完成，
 * 统一产出 {@code MoveEffect(deltaX, deltaY)}，Effect 层不新增方向重载（docs §17.4）。</p>
 */

public class DirectionValue implements Value {
    public final String dir;

    public DirectionValue(String dir) {
        this.dir = dir;
    }

    @Override
    public String toString() {
        return dir;
    }
}
