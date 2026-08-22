package com.own.virtualaibox.secd.value;

/**
 * 文本值（字符串）。P1 新增，用于 LLM 返回结果、speech 内容、记忆内容等世界原语。
 */
public class StringValue implements Value {
    public final String text;

    public StringValue(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "\"" + text + "\"";
    }
}
