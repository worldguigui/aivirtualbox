package com.own.virtualaibox.brain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.virtualaibox.domain.action.MoveAction;
import com.own.virtualaibox.domain.agent.Agent;
import com.own.virtualaibox.domain.agent.AgentState;
import com.own.virtualaibox.domain.world.WorldState;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component
@Slf4j
public class LLMBrain {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final Random random;

    public LLMBrain(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.objectMapper = new ObjectMapper();
        this.random = new Random();
    }

    public MoveAction decideAction(Agent agent, WorldState worldState) {
        log.info("LLMBrain: Deciding action for agent: {}", agent.getName());
        
        try {
            String prompt = buildPrompt(agent, worldState);
            String response = callLLM(prompt);
            MoveAction action = parseResponse(response, agent.getId());
            
            log.info("LLMBrain: Decision made - move ({}, {}), reason: {}", 
                    action.getDeltaX(), action.getDeltaY(), action.getReason());
            return action;
            
        } catch (Exception e) {
            log.error("LLMBrain: Error calling LLM, falling back to random decision", e);
            return randomDecision(agent.getId());
        }
    }

    private String buildPrompt(Agent agent, WorldState worldState) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("你是一个AI智能体，正在一个100x100的网格世界中行动。\n\n");
        sb.append("当前状态：\n");
        sb.append("- 当前时间tick：").append(worldState.getTick()).append("\n");
        sb.append("- 你的名字：").append(agent.getName()).append("\n");
        sb.append("- 你的位置：(").append(agent.getState().getX()).append(", ")
                .append(agent.getState().getY()).append(")\n\n");
        
        sb.append("世界中其他智能体：\n");
        for (Map.Entry<String, AgentState> entry : worldState.getAgentStates().entrySet()) {
            if (!entry.getKey().equals(agent.getId())) {
                sb.append("- ").append(entry.getValue().getName())
                        .append(" 位于 (").append(entry.getValue().getX())
                        .append(", ").append(entry.getValue().getY()).append(")\n");
            }
        }
        
        sb.append("\n请决定你的下一步行动。你可以向x和y方向各移动-1、0或+1格。\n");
        sb.append("请以JSON格式返回，格式如下：\n");
        sb.append("{\n");
        sb.append("  \"deltaX\": -1到1之间的整数,\n");
        sb.append("  \"deltaY\": -1到1之间的整数,\n");
        sb.append("  \"reason\": \"简短的决策理由\"\n");
        sb.append("}\n");
        sb.append("只返回JSON，不要其他文字。");
        
        return sb.toString();
    }

    private String callLLM(String userPrompt) {
        return chatModel.chat(userPrompt);
    }

    private MoveAction parseResponse(String response, String agentId) {
        try {
            String jsonStr = extractJson(response);
            JsonNode json = objectMapper.readTree(jsonStr);
            
            MoveAction action = new MoveAction();
            action.setAgentId(agentId);
            action.setDeltaX(clamp(json.get("deltaX").asInt()));
            action.setDeltaY(clamp(json.get("deltaY").asInt()));
            action.setReason(json.has("reason") ? json.get("reason").asText() : "LLM决策");
            
            return action;
            
        } catch (Exception e) {
            log.warn("LLMBrain: Failed to parse LLM response, using random fallback", e);
            return randomDecision(agentId);
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }

    private int clamp(int value) {
        return Math.max(-1, Math.min(1, value));
    }

    private MoveAction randomDecision(String agentId) {
        MoveAction action = new MoveAction();
        action.setAgentId(agentId);
        action.setDeltaX(random.nextInt(3) - 1);
        action.setDeltaY(random.nextInt(3) - 1);
        action.setReason("Fallback: Random movement");
        return action;
    }
}
