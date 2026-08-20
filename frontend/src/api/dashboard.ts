import { request } from './client'
import type { DashboardData } from '../types'

/** 拉取仪表盘主数据 */
export function fetchDashboard(eventLimit: number, memoryLimit: number) {
  const query = new URLSearchParams({ eventLimit: String(eventLimit), memoryLimit: String(memoryLimit) })
  return request<DashboardData>(`/api/dashboard?${query}`)
}

/** 推进世界一步,返回更新后的 tick 与 Agent 位置 */
export interface StepResult {
  tick: number
  agents: Array<{ id: string; name: string; x: number; y: number }>
}

export function stepTick() {
  return request<StepResult>('/step')
}
