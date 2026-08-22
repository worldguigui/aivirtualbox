import type { MindState } from '../types'

interface MindViewerProps {
  agentName: string | null
  agentId: string | null
  mind: MindState | null
  tick: number
}

/** SECD 状态机徽标（对应 AgentRuntime 的 idle / executing / suspended）。 */
function statusBadge(status: MindState['status'] | undefined) {
  switch (status) {
    case 'executing':
      return { label: 'Executing', className: 'mind-status-exec', hint: 'C 栈有指令，正在执行行为程序' }
    case 'suspended':
      return { label: 'Suspended', className: 'mind-status-susp', hint: 'D 栈有挂起帧：被中断/闭包续体' }
    case 'idle':
      return { label: 'Idle', className: 'mind-status-idle', hint: '计划耗尽，等待重编译' }
    default:
      return { label: '—', className: '', hint: '' }
  }
}

/** SECD Mind 面板：展示选中 Agent 的 S/E/C/D 四寄存器状态（P4）。 */
export function MindViewer({ agentName, agentId, mind, tick }: MindViewerProps) {
  const badge = statusBadge(mind?.status)

  return (
      <div className="panel">
          <div className="panel-header">
              <div>
                  <div className="panel-title">SECD Mind{agentName ? ` · ${agentName}` : ''}</div>
                  <div
                      className="panel-subtitle">{agentId ? `${agentId} — S/E/C/D 行为执行状态` : '选择 Agent 查看其 SECD 心智'}</div>
              </div>
              <div style={{display: 'flex', gap: 8, alignItems: 'center'}}>
                  <span className="badge">Tick {tick}</span>
                  {mind ? (
                      <span className={`badge ${badge.className}`} title={badge.hint}>{badge.label}</span>
                  ) : null}
              </div>
          </div>
          <div className="panel-body">
              {!mind ? (
                  <div className="empty">
                      暂无 SECD 运行时状态。
                      <br/>
                      <span className="subtle">Agent 尚未产生心智，或点击左侧 Agent 卡片查看其行为执行状态。</span>
                  </div>
              ) : (
                  <>
                      <div className="mind-registers">
                          <div className="mind-register">
                              <div className="register-head">
                                  <span className="register-name">S</span>
                                  <span className="subtle">值栈 · {mind.sSize}</span>
                              </div>
                              <div className="register-value mono">{mind.topValue || '∅'}</div>
                              {mind.sTop.length > 0 ? (
                                  <ol className="register-list mono">
                                      {mind.sTop.map((v, i) => <li key={i}>{v}</li>)}
                                  </ol>
                              ) : <div className="subtle">空栈</div>}
                          </div>

                          <div className="mind-register">
                              <div className="register-head">
                                  <span className="register-name">E</span>
                                  <span className="subtle">环境 · {mind.eSize}</span>
                              </div>
                              {mind.eTop.length > 0 ? (
                                  <ol className="register-list mono">
                                      {mind.eTop.map((b, i) => (
                                          <li key={i}><span className="subtle">{b.key}</span> = {b.value}</li>
                                      ))}
                                  </ol>
                              ) : <div className="subtle">空环境</div>}
                          </div>

                          <div className="mind-register">
                              <div className="register-head">
                                  <span className="register-name">C</span>
                                  <span className="subtle">控制栈 · {mind.cSize}</span>
                              </div>
                              <div className="register-value mono">{mind.currentInstruction || '∅'}</div>
                              {mind.cTop.length > 0 ? (
                                  <ol className="register-list mono">
                                      {mind.cTop.map((inst, i) => <li key={i}>{inst}</li>)}
                                  </ol>
                              ) : mind.program.length > 0 ? (
                                  <>
                                      <div className="subtle" style={{marginBottom: 4}}>上次行为程序（已执行完）</div>
                                      <ol className="register-list mono">
                                          {mind.program.map((inst, i) => <li key={i}>{inst}</li>)}
                                      </ol>
                                  </>
                              ) : <div className="subtle">空控制栈</div>}
                          </div>

                          <div className="mind-register">
                              <div className="register-head">
                                  <span className="register-name">D</span>
                                  <span className="subtle">续体 · {mind.dSize}</span>
                              </div>
                              <div className="d-bar">
                                  <span className="d-bar-fill"
                                        style={{width: `${Math.min(100, (mind.dSize / 50) * 100)}%`}}/>
                              </div>
                              <div className="subtle">
                                  {mind.dSize === 0
                                      ? '无挂起帧（未被中断）'
                                      : `${mind.dSize} 个挂起帧（中断/闭包续体，>=50 触发无限归约告警）`}
                              </div>
                          </div>
                      </div>

                      {mind.lastActions.length > 0 ? (
                          <div style={{marginTop: 14}}>
                              <div className="subtle" style={{marginBottom: 6}}>本 tick 副作用</div>
                              <div className="timeline">
                                  {mind.lastActions.map((action, i) => (
                                      <div key={i} className="timeline-item">
                                          <span>{i + 1}</span>
                                          <span className="mono">{action}</span>
                                      </div>
                                  ))}
                              </div>
                          </div>
                      ) : null}
                  </>
              )}
          </div>
      </div>
  )
}
