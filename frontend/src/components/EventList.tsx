import type { DashboardEvent } from '../types'
import { formatTime } from '../lib/format'

interface EventListProps {
  events: DashboardEvent[]
}

/** Event Stream 面板:事件卡片(类型 / 描述 / 来源 / 优先级 / detail JSON) */
export function EventList({ events }: EventListProps) {
  return (
    <div className="panel">
      <div className="panel-header">
        <div>
          <div className="panel-title">Event Stream</div>
          <div className="panel-subtitle">按时间展示 Tick、移动、相遇、决策等事件</div>
        </div>
        <div className="badge">{events.length} items</div>
      </div>
      <div className="panel-body section-scroll">
        {events.length === 0 ? (
          <div className="empty">当前没有事件记录。</div>
        ) : (
          <div className="list-grid">
            {events.map((event) => (
              <article key={event.eventId} className="event-card">
                <div className="card-top">
                  <div>
                    <div className="event-type">{event.eventType || 'event'}</div>
                    <div className="subtle">{event.description}</div>
                  </div>
                  <div className="badge">Tick {event.tick ?? '-'}</div>
                </div>
                <div className="kv">
                  <div className="item"><div className="k">Source</div><div className="v">{event.sourceSystem || '-'}</div></div>
                  <div className="item"><div className="k">Priority</div><div className="v">{event.priority ?? '-'}</div></div>
                </div>
                <div className="footer-line" style={{ marginTop: 12 }}>
                  <span>{formatTime(event.timestamp)}</span>
                  <span>{event.processed ? 'Processed' : 'Pending'}</span>
                </div>
                {event.detail ? (
                  <pre className="detail-json" style={{ marginTop: 12, background: 'rgba(255,255,255,0.03)' }}>
                    {JSON.stringify(event.detail, null, 2)}
                  </pre>
                ) : null}
              </article>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
