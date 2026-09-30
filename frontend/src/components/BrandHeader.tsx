import { BilingualText } from './i18n'

/** 品牌头部及运行指标的输入属性。 */
interface BrandHeaderProps {
  tick: number
  agentCount: number
  eventCount: number
}

/** 渲染品牌标题和三个核心运行指标。 */
export function BrandHeader({ tick, agentCount, eventCount }: BrandHeaderProps) {
  return (
    <div className="brand">
      <div className="eyebrow"><BilingualText primary="Virtual AI Box" secondary="虚拟 AI 小镇 · 世界观察台" /></div>
      <h1><BilingualText primary="A town that remembers" secondary="一个会记住世界的虚拟小镇" /></h1>
      <p className="lead">
        这里是一个由 SECD 抽象机驱动行为、由语言模型辅助决策的多居民沙盒：
        世界状态、居民记忆、事件流和每位居民的 S/E/C/D 心智状态都集中呈现。
      </p>

      <div className="hero-meta">
        <div className="metric">
          <BilingualText primary="Step" secondary="当前刻度" className="metric-label" />
          <div className="value">{tick}</div>
          <div className="hint">虚拟时钟推进中的当前刻度</div>
        </div>
        <div className="metric">
          <BilingualText primary="Residents" secondary="活跃居民" className="metric-label" />
          <div className="value">{agentCount}</div>
          <div className="hint">正在参与决策的智能体数量</div>
        </div>
        <div className="metric">
          <BilingualText primary="Events" secondary="事件记录" className="metric-label" />
          <div className="value">{eventCount}</div>
          <div className="hint">事件总线已记录的历史事件数</div>
        </div>
      </div>
    </div>
  )
}
