interface SimulationControlsProps {
  autoRunning: boolean
  autoSpeed: number
  eventLimit: number
  memoryLimit: number
  filterText: string
  lastSync: string
  onStep: () => void
  onToggleAuto: () => void
  onRefresh: () => void
  onStop: () => void
  onAutoSpeedChange: (value: number) => void
  onEventLimitChange: (value: number) => void
  onMemoryLimitChange: (value: number) => void
  onFilterTextChange: (value: string) => void
}

/** 模拟控制面板:单步 / 自动 / 刷新 / 停止 + 滑杆与过滤 */
export function SimulationControls({
  autoRunning,
  autoSpeed,
  eventLimit,
  memoryLimit,
  filterText,
  lastSync,
  onStep,
  onToggleAuto,
  onRefresh,
  onStop,
  onAutoSpeedChange,
  onEventLimitChange,
  onMemoryLimitChange,
  onFilterTextChange,
}: SimulationControlsProps) {
  return (
    <>
      <div className="panel-header">
        <div>
          <div className="panel-title">Simulation Controls</div>
          <div className="panel-subtitle">单步、自动播放、刷新、过滤和节流</div>
        </div>
        <div className="control-row">
          <button className="primary" onClick={onStep}>Step Tick</button>
          <button
            className="secondary"
            onClick={onToggleAuto}
            style={{ opacity: autoRunning ? 0.88 : 1 }}
          >
            {autoRunning ? 'Auto Running' : 'Auto Run'}
          </button>
          <button className="ghost" onClick={onRefresh}>Refresh</button>
          <button className="danger" onClick={onStop}>Stop</button>
        </div>
      </div>
      <div className="panel-body">
        <div className="controls">
          <div className="control-box">
            <label htmlFor="speed-range">Auto Speed: <span>{autoSpeed}ms</span></label>
            <input
              id="speed-range"
              type="range"
              min="120"
              max="2000"
              step="10"
              value={autoSpeed}
              onChange={(e) => onAutoSpeedChange(Number(e.target.value))}
            />
          </div>
          <div className="control-box">
            <label htmlFor="event-limit">Event Limit</label>
            <input
              id="event-limit"
              type="range"
              min="10"
              max="120"
              step="5"
              value={eventLimit}
              onChange={(e) => onEventLimitChange(Number(e.target.value))}
            />
          </div>
          <div className="control-box">
            <label htmlFor="memory-limit">Memory Limit</label>
            <input
              id="memory-limit"
              type="range"
              min="3"
              max="20"
              step="1"
              value={memoryLimit}
              onChange={(e) => onMemoryLimitChange(Number(e.target.value))}
            />
          </div>
          <div className="control-box">
            <label htmlFor="agent-filter">Filter Agent / Event</label>
            <input
              id="agent-filter"
              type="search"
              placeholder="输入名字、事件类型或关键字"
              value={filterText}
              onChange={(e) => onFilterTextChange(e.target.value)}
            />
          </div>
        </div>
        <div className="footer-line">
          <span>快捷键:Space = 单步,A = 自动运行,R = 刷新</span>
          <span>{lastSync}</span>
        </div>
      </div>
    </>
  )
}
