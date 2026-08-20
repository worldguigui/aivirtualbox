/** 通用格式化工具(由原 index.html 内联逻辑迁移) */

/** 格式化时间戳:非法/空值返回 '-' */
export function formatTime(value: string | number | null | undefined): string {
  if (!value) return '-'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString()
}

/**
 * 文本过滤匹配:对每一项取若干字段拼接后做大小写不敏感的子串匹配。
 * 找不到 matcher(空过滤)时返回原数组。
 */
export function applyFilter<T>(items: T[], matcher?: (item: T) => boolean): T[] {
  if (!matcher) return items
  return items.filter(matcher)
}

/** 按过滤文本匹配 Agent:名字 / id / 记忆摘要 / 最近记忆内容 */
export function agentMatchesFilter(agent: { name: string; id: string; memorySummary: string; recentMemories: Array<{ content: string }> }, text: string): boolean {
  if (!text) return true
  const target = [agent.name, agent.id, agent.memorySummary, JSON.stringify(agent.recentMemories ?? [])]
    .join(' ')
    .toLowerCase()
  return target.includes(text)
}

/** 按过滤文本匹配事件:类型 / 描述 / 来源系统 / detail */
export function eventMatchesFilter(event: { eventType: string; description: string; sourceSystem: string; detail?: Record<string, unknown> | null }, text: string): boolean {
  if (!text) return true
  const target = [event.eventType, event.description, event.sourceSystem, JSON.stringify(event.detail ?? {})]
    .join(' ')
    .toLowerCase()
  return target.includes(text)
}
