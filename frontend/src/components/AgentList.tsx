import type { Agent } from '../types'
import { BilingualText } from './i18n'

/** Agent 列表面板的输入属性。 */
interface AgentListProps {
  agents: Agent[]
  tick: number
  selectedAgentId: string | null
  onSelectAgent: (id: string) => void
}

/** 渲染 Agent 列表、位置、记忆统计和最近记忆，并处理选中状态。 */
export function AgentList({ agents, tick, selectedAgentId, onSelectAgent }: AgentListProps) {
  return (
    <div className="panel">
      <div className="panel-header">
        <div>
          <BilingualText primary="Residents & Memory" secondary="居民与记忆" className="panel-title" />
          <div className="panel-subtitle">查看位置、状态、记忆摘要和最近记忆</div>
        </div>
        <div className="badge"><BilingualText primary={`${agents.length} items`} secondary={`${agents.length} 位`} /></div>
      </div>
      <div className="panel-body section-scroll">
        {agents.length === 0 ? (
          <div className="empty">当前没有居民数据。</div>
        ) : (
          <div className="list-grid">
            {agents.map((agent) => {
              const memorySummary = (agent.memorySummary || '').trim()
              const recentMemories = agent.recentMemories ?? []
              return (
                <article
                  key={agent.id}
                  className={`agent-card${agent.id === selectedAgentId ? ' active' : ''}`}
                  onClick={() => onSelectAgent(agent.id)}
                >
                  <div className="card-top">
                    <div>
                      <div className="agent-name">{agent.name || '未知居民'}</div>
                      <div className="subtle">{agent.id}</div>
                    </div>
                    <div className="badge"><BilingualText primary={agent.active ? 'Active' : 'Inactive'} secondary={agent.active ? '活跃' : '休眠'} /></div>
                  </div>
                  <div className="kv">
                    <div className="item"><BilingualText primary="Position" secondary="所在位置" className="field-label" /><div className="v">({agent.x}, {agent.y})</div></div>
                    <div className="item"><BilingualText primary="Events" secondary="事件数量" className="field-label" /><div className="v">{agent.eventHistorySize ?? 0}</div></div>
                  </div>
                  <div className="kv" style={{ marginTop: 10 }}>
                    <div className="item"><BilingualText primary="Memories" secondary="记忆数量" className="field-label" /><div className="v">{agent.memoryStats?.totalCount ?? 0}</div></div>
                    <div className="item"><BilingualText primary="Step" secondary="当前刻度" className="field-label" /><div className="v">{tick}</div></div>
                  </div>
                  <div style={{ marginTop: 12 }}>
                    <BilingualText primary="Memory Summary" secondary="记忆摘要" className="section-label" />
                    <div className="pill pre-wrap" style={{ background: 'rgba(255,255,255,0.03)' }}>
                      {memorySummary || '暂无摘要'}
                    </div>
                  </div>
                  <div style={{ marginTop: 12 }}>
                    <BilingualText primary="Recent Memories" secondary="最近记忆" className="section-label" />
                    {recentMemories.length === 0 ? (
                      <div className="empty">暂无最近记忆</div>
                    ) : (
                      <div className="timeline">
                        {recentMemories.map((item) => (
                          <div key={item.id} className="timeline-item">
                            <span>第 {item.tick} 刻</span>
                            <span>{item.content}</span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </article>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
