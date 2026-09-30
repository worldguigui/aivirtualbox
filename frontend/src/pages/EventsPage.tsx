import { EventList } from '../components/EventList'
import type { PageDataProps } from './pageTypes'
import { BilingualText } from '../components/i18n'

/** 展示按 tick 聚合的事件时间线，作为小镇运行审计入口。 */
export function EventsPage({ data, visibleEvents }: PageDataProps) {
  return (
    <div className="page-stack">
      <section className="page-heading">
        <div>
          <span className="section-kicker">观察与时间线</span>
          <h1><BilingualText primary="Events" secondary="事件流" /></h1>
          <p>从刻度生命周期到居民交互，追踪系统真实发生的每一件事。</p>
        </div>
        <div className="heading-stat"><strong>{visibleEvents.length}</strong><BilingualText primary="visible events" secondary="当前可见事件" /></div>
      </section>
      <section className="single-stage event-stage"><EventList events={visibleEvents} /></section>
      <div className="page-note">当前 tick：{data?.tick ?? 0} · 数据由 EventBus 统一提供</div>
    </div>
  )
}
