import { request } from './client'
import type { Agent, AgentCreateRequest, DashboardData, StepResult, WorldInfo } from '../types'

/** 拉取仪表盘主数据。 */
export function fetchDashboard(eventLimit: number, memoryLimit: number) {
  const query = new URLSearchParams({ eventLimit: String(eventLimit), memoryLimit: String(memoryLimit) })
  return request<DashboardData>(`/api/dashboard?${query}`)
}

/** 请求服务端推进一个世界 tick。 */
export function stepTick() {
  return request<StepResult>('/api/dashboard/step')
}

/** 创建居民并返回创建后的世界与居民列表。 */
export interface CreateAgentResult {
  tick: number
  agent: Agent
  world: WorldInfo
  agents: Agent[]
}

export function createAgent(input: AgentCreateRequest) {
  return request<CreateAgentResult>('/api/dashboard/addAgent', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input),
  })
}

export function updateAgent(agentId: string, input: AgentCreateRequest) {
  return request<CreateAgentResult>(`/api/dashboard/agents/${encodeURIComponent(agentId)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input),
  })
}
