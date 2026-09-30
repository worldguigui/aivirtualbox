import { BrandHeader } from '../components/BrandHeader'
import { EventList } from '../components/EventList'
import { MindViewer } from '../components/MindViewer'
import { SimulationControls } from '../components/SimulationControls'
import { StatusPanel } from '../components/StatusPanel'
import { WorldMap } from '../components/WorldMap'
import { AgentList } from '../components/AgentList'
import type { SimulationPageProps } from './pageTypes'

/** 展示虚拟小镇的核心运行态，并串联地图、Agent、心智和事件概览。 */
export function OverviewPage(props: SimulationPageProps) {
  const { data, visibleAgents, visibleEvents, focusedAgent, selectedAgentId, onSelectAgent } = props
  const memoryCoverage = `${visibleAgents.filter((agent) => agent.memoryStats).length}/${visibleAgents.length || 1} agents`

  return (
    <div className="page-stack overview-page">
      <section className="overview-hero">
        <BrandHeader tick={data?.tick ?? 0} agentCount={visibleAgents.length} eventCount={visibleEvents.length} />
        <StatusPanel world={data?.world} metrics={data?.metrics} memoryCoverage={memoryCoverage} convergence={data?.convergence} />
      </section>
      <section className="panel">
        <SimulationControls {...props} />
      </section>
      <section className="overview-grid">
        <WorldMap agents={visibleAgents} tick={data?.tick ?? 0} world={data?.world} selectedAgentId={selectedAgentId} status="运行中" />
        <AgentList agents={visibleAgents} tick={data?.tick ?? 0} selectedAgentId={selectedAgentId} onSelectAgent={onSelectAgent} />
        <div className="overview-side-stack">
          <MindViewer agentName={focusedAgent?.name ?? null} agentId={focusedAgent?.id ?? null} mind={focusedAgent?.mind ?? null} tick={data?.tick ?? 0} />
          <EventList events={visibleEvents} />
        </div>
      </section>
    </div>
  )
}
