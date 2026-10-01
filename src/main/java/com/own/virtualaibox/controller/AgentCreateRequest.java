package com.own.virtualaibox.controller;

/** 新居民创建请求的输入参数。 */
public record AgentCreateRequest(String name, Integer x, Integer y) {
}
