/** 与后端 /api/dashboard 返回结构对应的类型定义 */

export interface WorldInfo {
  width: number
  height: number
  agentCount: number
}

export interface MemoryStats {
  agentId: string
  totalCount: number
  observationCount: number
  eventCount: number
  relationshipCount: number
  locationCount: number
  selfCount: number
  learningCount: number
}

export interface MemoryEntry {
  id: string
  type: string
  content: string
  tags: string[]
  importance: number
  createdAt: string
  lastAccessedAt: string
  accessCount: number
  tick: number
  relatedAgentId: string | null
}

export interface Agent {
  id: string
  name: string
  x: number
  y: number
  active: boolean
  eventHistorySize: number
  memoryStats: MemoryStats | null
  memorySummary: string
  recentMemories: MemoryEntry[]
}

/** 事件 detail 为多态字段,不同事件类型结构不同 */
export interface DashboardEvent {
  eventId: string
  eventType: string
  description: string
  tick: number
  timestamp: string
  priority: number
  sourceSystem: string
  processed: boolean
  detail: Record<string, unknown> | null
}

export interface Metrics {
  eventSubscriberCount: number
  eventHistorySize: number
  agentCount: number
  currentTick: number
  serverTime: string
}

export interface DashboardData {
  tick: number
  world: WorldInfo
  agents: Agent[]
  events: DashboardEvent[]
  metrics: Metrics
  capabilities: string[]
}
