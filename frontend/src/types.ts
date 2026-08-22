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
  /** P4：该 Agent 的 SECD 行为执行状态（无运行时为 null） */
  mind: MindState | null
}

/** SECD 四寄存器状态摘要（P4 可视化，对应 AgentRuntime.mindSummary） */
export interface MindState {
  status: 'executing' | 'suspended' | 'idle'
  terminated: boolean
  sSize: number
  eSize: number
  cSize: number
  dSize: number
  topValue: string
  currentInstruction: string
  sTop: string[]
  eTop: Array<{ key: string; value: string }>
  cTop: string[]
  /** 上次编译的行为程序（C 栈快照，机器 idle 时仍保留） */
  program: string[]
  /** 上次 tick 产出的副作用摘要 */
  lastActions: string[]
}

/** 收敛/活锁状态快照（P3 → P4 收敛指示器，对应 ConvergenceMonitor.summary） */
export interface ConvergenceInfo {
  worldConverged: boolean
  stableTicks: number
  stuckAgents: string[]
  loopAgents: string[]
  dStackOverflowAgents: string[]
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
  convergence: ConvergenceInfo
}
