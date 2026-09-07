import {Badge, ConfigProvider, Space, Tag, Timeline, Typography, Button, theme, Collapse} from 'antd'
import { FullscreenOutlined, FullscreenExitOutlined } from '@ant-design/icons'
import { useRef, useState, useEffect } from 'react'
import type { DashboardEvent } from '../types'
import { formatTime } from '../lib/format'

const { Text } = Typography

/** 事件类型 → Tag 预设色（与后端 9 种 eventType 一一对应） */
const EVENT_TAG_COLOR: Record<string, string> = {
  'tick.started': 'blue',
  'tick.ended': 'geekblue',
  'agent.moved': 'green',
  'agent.met': 'gold',
  'agent.decided': 'purple',
  'agent.spoke': 'cyan',
  'agent.stuck': 'red',
  'agent.loop': 'orange',
  'world.converged': 'magenta',
}

/** 事件类型 → Timeline 节点色（与 Tag 色一致，保持视觉同源） */
const EVENT_DOT_COLOR: Record<string, string> = {
  'tick.started': 'blue',
  'tick.ended': 'geekblue',
  'agent.moved': 'green',
  'agent.met': 'gold',
  'agent.decided': 'purple',
  'agent.spoke': 'cyan',
  'agent.stuck': 'red',
  'agent.loop': 'orange',
  'world.converged': 'magenta',
}

/** 尺寸配置：常态 / 全屏 */
const SIZES = {
  normal: {
    containerHeight: 'auto',
    containerPadding: 0,
    bodyMaxHeight: 420,
    tickFontSize: 24,
    tagFontSize: 18,
    descFontSize: 20,
    metaFontSize: 18,
    detailFontSize: 22,
  },
  fullscreen: {
    containerHeight: '100vh',
    containerPadding: 16,
    bodyMaxHeight: 'calc(100vh - 120px)',
    tickFontSize: 28,
    tagFontSize: 22,
    descFontSize: 24,
    metaFontSize: 22,
    detailFontSize: 26,
  },
}

/** 将 detail 多态对象压成可读的多行文本；detail 为 null/非对象时原样返回 */
function renderDetail(event: DashboardEvent): string | null {
  const detail = event.detail
  if (detail == null) return null
  if (typeof detail !== 'object') return String(detail)
  return Object.entries(detail)
      .map(([key, value]) => `${key}: ${typeof value === 'object' ? JSON.stringify(value) : String(value)}`)
      .join('\n')
}

/** 按 tick 分组，保持事件原始顺序 */
function groupByTick(events: DashboardEvent[]): { tick: number; events: DashboardEvent[] }[] {
  const groups = new Map<number, DashboardEvent[]>()
  for (const e of events) {
    const arr = groups.get(e.tick) ?? []
    arr.push(e)
    groups.set(e.tick, arr)
  }
  return [...groups.entries()]
      .sort((a, b) => a[0] - b[0])
      .map(([tick, events]) => ({ tick, events }))
}

interface EventListProps {
  events: DashboardEvent[]
}

/** Event Stream 面板：按 tick 分组的时间线展示事件流，支持全屏放大 */
export function EventList({ events }: EventListProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const [isFullscreen, setIsFullscreen] = useState(false)

  const s = isFullscreen ? SIZES.fullscreen : SIZES.normal

  const groups = groupByTick(events)

  const toggleFullscreen = () => {
    const el = containerRef.current
    if (!el) return

    if (!isFullscreen) {
      el.requestFullscreen?.()
    } else {
      document.exitFullscreen?.()
    }
  }

  useEffect(() => {
    const handler = () => setIsFullscreen(!!document.fullscreenElement)
    document.addEventListener('fullscreenchange', handler)
    return () => document.removeEventListener('fullscreenchange', handler)
  }, [])

  // 扁平化：tick 分隔线作为 Timeline 的一个 item
  const items = groups.flatMap(group => [
    {
      key: `tick-${group.tick}`,
      color: 'gray',
      dot: <Badge status="processing" />,
      children: (
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
            <Text strong style={{ fontSize: s.tickFontSize, color: '#fff' }}>
              Tick {group.tick}
            </Text>
            <div style={{ flex: 1, borderTop: '1px dashed #444' }} />
            <Text type="secondary" style={{ fontSize: s.metaFontSize }}>
              {group.events.length} events
            </Text>
          </div>
      ),
    },
    ...group.events.map(event => {
      const detailText = renderDetail(event)

      return {
        key: event.eventId,
        color: EVENT_DOT_COLOR[event.eventType] ?? 'gray',
        children: (
            <Space direction="vertical" size={2} style={{ width: '100%' }}>
              <Space size={8} wrap>
                <Tag
                    color={EVENT_TAG_COLOR[event.eventType] ?? 'default'}
                    style={{ marginInlineEnd: 0, fontSize: s.tagFontSize }}
                >
                  {event.eventType || 'event'}
                </Tag>
                <Text style={{ fontSize: s.descFontSize }}>{event.description}</Text>
              </Space>
              <Text type="secondary" style={{ fontSize: s.metaFontSize }}>
                {event.sourceSystem || '-'} · {formatTime(event.timestamp)} ·{' '}
                {event.processed ? 'Processed' : 'Pending'}
              </Text>
              {detailText ? (
                  <Collapse
                      ghost
                      size="small"
                      items={[
                        {
                          key: 'detail',
                          label: <Text type="secondary" style={{ fontSize: s.metaFontSize }}>detail</Text>,
                          children: (
                              <pre
                                  style={{
                                    margin: 0,
                                    fontSize: s.detailFontSize,
                                    whiteSpace: 'pre-wrap',
                                    wordBreak: 'break-all',
                                  }}
                              >
                                {detailText}
                              </pre>
                          ),
                        },
                      ]}
                  />
              ) : null}
            </Space>
        ),
      }
    }),
  ])

  return (
      <div
          ref={containerRef}
          style={{
            background: isFullscreen ? '#141414' : 'transparent',
            padding: s.containerPadding,
            height: s.containerHeight,
            overflowY: isFullscreen ? 'auto' : 'visible',
            boxSizing: 'border-box',
          }}
      >
        <div className="panel">
          <div className="panel-header">
            <div>
              <div className="panel-title">Event Stream</div>
              <div className="panel-subtitle">按时间展示 Tick、移动、相遇、决策等事件</div>
            </div>
            <Space size={8}>
              <span className="badge">{events.length} items</span>
              <Button
                  size="small"
                  type="text"
                  icon={isFullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
                  onClick={toggleFullscreen}
                  style={{ color: '#fff' }}
              />
            </Space>
          </div>
          <div className="panel-body section-scroll" style={{ maxHeight: s.bodyMaxHeight }}>
            {events.length === 0 ? (
                <div className="empty">当前没有事件记录。</div>
            ) : (
                <ConfigProvider theme={{ algorithm: theme.darkAlgorithm }}>
                  <Timeline items={items} />
                </ConfigProvider>
            )}
          </div>
        </div>
      </div>
  )
}