import type { Metrics, WorldInfo } from '../types'

interface StatusPanelProps {
  world?: WorldInfo
  metrics?: Metrics
  memoryCoverage: string
}

/** System Status 面板:世界尺寸、订阅者数、记忆覆盖率、运行模式 + 架构时间线 */
export function StatusPanel({ world, metrics, memoryCoverage }: StatusPanelProps) {
  return (
    <div className="panel status-panel">
      <div>
        <div className="panel-title">System Status</div>
        <div className="panel-subtitle">快速查看核心运行指标和系统能力</div>
      </div>
      <div className="status-grid">
        <div className="pill">
          <div className="name">World Size</div>
          <div className="data">{world ? `${world.width} × ${world.height}` : '-'}</div>
        </div>
        <div className="pill">
          <div className="name">Event Subscribers</div>
          <div className="data">{metrics?.eventSubscriberCount ?? '-'}</div>
        </div>
        <div className="pill">
          <div className="name">Memory Coverage</div>
          <div className="data">{memoryCoverage}</div>
        </div>
        <div className="pill">
          <div className="name">Mode</div>
          <div className="data">Event + Memory Driven</div>
        </div>
      </div>
      <div className="timeline">
        <div className="timeline-item"><span>架构</span><span>WorldEngine → TickSchedule → EventBus</span></div>
        <div className="timeline-item"><span>决策</span><span>LLMBrain + AgentMemory</span></div>
        <div className="timeline-item"><span>扩展方向</span><span>经营 / 社交 / 战斗 / 任务 / 资源</span></div>
      </div>
    </div>
  )
}
