package com.own.virtualaibox.secd.value;

/**
 * 单位/空值。副作用型原语（move/speak/remember）完成时压入 S，
 * 使指令流在语义上"有返回值"，避免后续 @ 从空栈弹错。
 */
public class VoidValue implements Value {
    @Override
    public String toString() {
        return "void";
    }
}
