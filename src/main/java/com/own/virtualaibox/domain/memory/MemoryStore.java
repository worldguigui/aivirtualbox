package com.own.virtualaibox.domain.memory;

import java.util.List;

/**
 * 记忆存储接口
 * 支持多种记忆存储实现（内存、Redis、数据库等）
 */
public interface MemoryStore {
    
    /**
     * 存储记忆
     */
    void store(MemoryEntry entry);
    
    /**
     * 批量存储记忆
     */
    void storeBatch(List<MemoryEntry> entries);
    
    /**
     * 检索记忆
     */
    MemoryEntry retrieve(String memoryId);
    
    /**
     * 查询特定Agent的所有记忆
     */
    List<MemoryEntry> queryByAgent(String agentId);
    
    /**
     * 按标签查询记忆
     */
    List<MemoryEntry> queryByTag(String agentId, String tag);
    
    /**
     * 按类型查询记忆
     */
    List<MemoryEntry> queryByType(String agentId, MemoryEntry.MemoryType type);
    
    /**
     * 按重要程度查询，返回最重要的N条记忆
     */
    List<MemoryEntry> queryTopByImportance(String agentId, int limit);
    
    /**
     * 按时间范围查询
     */
    List<MemoryEntry> queryByTickRange(String agentId, int startTick, int endTick);
    
    /**
     * 查询最近的N条记忆
     */
    List<MemoryEntry> queryRecent(String agentId, int limit);
    
    /**
     * 删除记忆
     */
    void delete(String memoryId);
    
    /**
     * 清空特定Agent的所有记忆
     */
    void clearAgent(String agentId);
    
    /**
     * 获取记忆统计信息
     */
    MemoryStats getStats(String agentId);
    
    /**
     * 记忆统计信息
     */
    record MemoryStats(
        String agentId,
        int totalCount,
        int observationCount,
        int eventCount,
        int relationshipCount,
        int locationCount,
        int selfCount,
        int learningCount
    ) {}
}
