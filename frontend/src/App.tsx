import { useCallback, useEffect, useMemo, useState } from 'react'
import { useDashboard } from './hooks/useDashboard'
import { applyFilter, agentMatchesFilter, eventMatchesFilter, formatTime } from './lib/format'
import { BrandHeader } from './components/BrandHeader'
import { StatusPanel } from './components/StatusPanel'
import { SimulationControls } from './components/SimulationControls'
import { WorldMap } from './components/WorldMap'
import { AgentList } from './components/AgentList'
import { EventList } from './components/EventList'

/** 渲染状态徽标(由原 render-status badge 的 Idle/Syncing/Live/Stepping/Offline 语义迁移) */
function deriveStatus(stepping: boolean, isError: boolean, isLoading: boolean, isFetching: boolean, hasData: boolean) {
  if (stepping) return 'Stepping'
  if (isError) return 'Offline'
  if (isLoading) return 'Loading'
  if (isFetching) return 'Syncing'
  if (hasData) return 'Live'
  return 'Idle'
}

export default function App() {
  // 面板控制状态(原全局 state 对象)
  const [autoRunning, setAutoRunning] = useState(false)
  const [autoSpeed, setAutoSpeed] = useState(650)
  const [eventLimit, setEventLimit] = useState(40)
  const [memoryLimit, setMemoryLimit] = useState(8)
  const [filterText, setFilterText] = useState('')
  const [selectedAgentId, setSelectedAgentId] = useState<string | null>(null)

  const onAutoRunError = useCallback(() => setAutoRunning(false), [])

  const { data, isFetching, isLoading, isError, stepping, step, refresh } = useDashboard({
    eventLimit,
    memoryLimit,
    autoRunning,
    autoSpeed,
    onAutoRunError,
  })

  const toggleAutoRun = useCallback(() => setAutoRunning((running) => !running), [])
  const stopAutoRun = useCallback(() => setAutoRunning(false), [])

  // 文本过滤(与旧前端一致:名字 / id / 记忆摘要 / 最近记忆,事件类型 / 描述 / 来源 / detail)
  const text = filterText.trim().toLowerCase()
  const visibleAgents = useMemo(
    () => applyFilter(data?.agents ?? [], (agent) => agentMatchesFilter(agent, text)),
    [data, text],
  )
  const visibleEvents = useMemo(
    () => applyFilter(data?.events ?? [], (event) => eventMatchesFilter(event, text)),
    [data, text],
  )

  // 键盘快捷键:Space = 单步,A = 自动运行,R = 刷新
  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.code === 'Space') {
        event.preventDefault()
        void step()
      } else if (event.code === 'KeyA') {
        toggleAutoRun()
      } else if (event.code === 'KeyR') {
        void refresh()
      }
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [step, toggleAutoRun, refresh])

  const memoryCoverage = useMemo(() => {
    const withMemory = visibleAgents.filter((agent) => agent.memoryStats).length
    return `${withMemory}/${visibleAgents.length || 1} agents`
  }, [visibleAgents])

  const lastSync = data ? `Last sync: ${formatTime(data.metrics.serverTime)}` : 'Last sync: -'
  const status = deriveStatus(stepping, isError, isLoading, isFetching, Boolean(data))

  return (
    <div className="shell">
      <section className="hero">
        <BrandHeader
          tick={data?.tick ?? 0}
          agentCount={visibleAgents.length}
          eventCount={visibleEvents.length}
        />
        <StatusPanel
          world={data?.world}
          metrics={data?.metrics}
          memoryCoverage={memoryCoverage}
        />
      </section>

      <section className="panel">
        <SimulationControls
          autoRunning={autoRunning}
          autoSpeed={autoSpeed}
          eventLimit={eventLimit}
          memoryLimit={memoryLimit}
          filterText={filterText}
          lastSync={lastSync}
          onStep={() => void step()}
          onToggleAuto={toggleAutoRun}
          onRefresh={() => void refresh()}
          onStop={stopAutoRun}
          onAutoSpeedChange={setAutoSpeed}
          onEventLimitChange={setEventLimit}
          onMemoryLimitChange={setMemoryLimit}
          onFilterTextChange={setFilterText}
        />
      </section>

      <section className="workspace">
        <WorldMap
          agents={visibleAgents}
          tick={data?.tick ?? 0}
          world={data?.world}
          selectedAgentId={selectedAgentId}
          status={status}
        />
        <div className="split">
          <AgentList
            agents={visibleAgents}
            tick={data?.tick ?? 0}
            selectedAgentId={selectedAgentId}
            onSelectAgent={setSelectedAgentId}
          />
          <EventList events={visibleEvents} />
        </div>
      </section>
    </div>
  )
}
