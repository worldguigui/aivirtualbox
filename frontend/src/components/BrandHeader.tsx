interface BrandHeaderProps {
  tick: number
  agentCount: number
  eventCount: number
}

/** Hero 品牌区:标题 + 三个核心指标 */
export function BrandHeader({ tick, agentCount, eventCount }: BrandHeaderProps) {
  return (
    <div className="brand">
      <div className="eyebrow">Virtual AI Box · Sandbox Control Center</div>
      <h1>让 Agent 记住世界,也让世界记住事件。</h1>
      <p className="lead">
        这是一个可演示、可扩展的 AI 沙盒前端:它同步显示世界状态、Agent 记忆、事件流和执行过程,
        为后续接入经营、社交、任务、战斗等系统保留了统一入口。
      </p>

      <div className="hero-meta">
        <div className="metric">
          <div className="label">Current Tick</div>
          <div className="value">{tick}</div>
          <div className="hint">虚拟时钟推进中的当前刻度</div>
        </div>
        <div className="metric">
          <div className="label">Active Agents</div>
          <div className="value">{agentCount}</div>
          <div className="hint">正在参与决策的智能体数量</div>
        </div>
        <div className="metric">
          <div className="label">Event History</div>
          <div className="value">{eventCount}</div>
          <div className="hint">事件总线已记录的历史事件数</div>
        </div>
      </div>
    </div>
  )
}
