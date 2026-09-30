import type { ConvergenceInfo, Metrics, WorldInfo } from '../types'

/** 系统状态面板的输入属性。 */
interface StatusPanelProps {
  world?: WorldInfo
  metrics?: Metrics
  memoryCoverage: string
  convergence?: ConvergenceInfo
}

/** 渲染世界尺寸、订阅者数、记忆覆盖率和收敛状态。 */
export function StatusPanel({ world, metrics, memoryCoverage, convergence }: StatusPanelProps) {
  const stuckCount = convergence?.stuckAgents.length ?? 0
  const loopCount = convergence?.loopAgents.length ?? 0
  const dStackCount = convergence?.dStackOverflowAgents.length ?? 0

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
          <div className="name">Execution Core</div>
          <div className="data">SECD + LLM Oracle</div>
        </div>
      </div>

      <div className="conv-strip" title="由 ConvergenceMonitor 检测（P3）">
        <span className={`conv-chip ${convergence?.worldConverged ? 'conv-warn' : 'conv-ok'}`}>
          世界 {convergence?.worldConverged ? `已收敛(稳定${convergence.stableTicks}tick)` : '运行中'}
        </span>
        <span className={`conv-chip ${stuckCount > 0 ? 'conv-warn' : 'conv-ok'}`}>
          {stuckCount > 0 ? `卡死: ${convergence?.stuckAgents.join(', ')}` : '无卡死'}
        </span>
        <span className={`conv-chip ${loopCount > 0 ? 'conv-warn' : 'conv-ok'}`}>
          {loopCount > 0 ? `循环: ${convergence?.loopAgents.join(', ')}` : '无循环'}
        </span>
        {dStackCount > 0 ? (
          <span className="conv-chip conv-warn">
            D栈过深: {convergence?.dStackOverflowAgents.join(', ')}
          </span>
        ) : null}
      </div>

      <div className="timeline">
        <div className="timeline-item"><span>执行内核</span><span>SECD 抽象机（S/E/C/D）驱动行为</span></div>
        <div className="timeline-item"><span>决策链路</span><span>Plan → SECD → Effect → World</span></div>
        <div className="timeline-item"><span>收敛检测</span><span>不动点 / 卡死 / 周期循环 / 无限归约</span></div>
      </div>
    </div>
  )
}
