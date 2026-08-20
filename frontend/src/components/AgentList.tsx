import type { Agent } from '../types'

interface AgentListProps {
  agents: Agent[]
  tick: number
  selectedAgentId: string | null
  onSelectAgent: (id: string) => void
}

/** Agents & Memory 面板:Agent 卡片(位置 / 记忆统计 / 记忆摘要 / 最近记忆),点击选中高亮 */
export function AgentList({ agents, tick, selectedAgentId, onSelectAgent }: AgentListProps) {
  return (
    <div className="panel">
      <div className="panel-header">
        <div>
          <div className="panel-title">Agents & Memory</div>
          <div className="panel-subtitle">查看位置、状态、记忆摘要和最近记忆</div>
        </div>
        <div className="badge">{agents.length} items</div>
      </div>
      <div className="panel-body section-scroll">
        {agents.length === 0 ? (
          <div className="empty">当前没有 Agent 数据。</div>
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
                      <div className="agent-name">{agent.name || 'Unknown'}</div>
                      <div className="subtle">{agent.id}</div>
                    </div>
                    <div className="badge">{agent.active ? 'Active' : 'Inactive'}</div>
                  </div>
                  <div className="kv">
                    <div className="item"><div className="k">Position</div><div className="v">({agent.x}, {agent.y})</div></div>
                    <div className="item"><div className="k">Event History</div><div className="v">{agent.eventHistorySize ?? 0}</div></div>
                  </div>
                  <div className="kv" style={{ marginTop: 10 }}>
                    <div className="item"><div className="k">Memory Entries</div><div className="v">{agent.memoryStats?.totalCount ?? 0}</div></div>
                    <div className="item"><div className="k">Tick</div><div className="v">{tick}</div></div>
                  </div>
                  <div style={{ marginTop: 12 }}>
                    <div className="subtle" style={{ marginBottom: 8 }}>Memory Summary</div>
                    <div className="pill pre-wrap" style={{ background: 'rgba(255,255,255,0.03)' }}>
                      {memorySummary || '暂无摘要'}
                    </div>
                  </div>
                  <div style={{ marginTop: 12 }}>
                    <div className="subtle" style={{ marginBottom: 8 }}>Recent Memories</div>
                    {recentMemories.length === 0 ? (
                      <div className="empty">暂无最近记忆</div>
                    ) : (
                      <div className="timeline">
                        {recentMemories.map((item) => (
                          <div key={item.id} className="timeline-item">
                            <span>Tick {item.tick}</span>
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
