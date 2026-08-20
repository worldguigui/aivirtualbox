import { useEffect, useRef } from 'react'
import type { Agent, WorldInfo } from '../types'

interface WorldMapProps {
  agents: Agent[]
  tick: number
  world?: WorldInfo
  selectedAgentId: string | null
  status: string
}

/** 画布基准尺寸:网格在 960x960 内等比缩放 */
const CANVAS_BASE = 960
const AGENT_COLORS = ['#5eead4', '#7c8cff', '#ffb86b', '#ff6b88', '#9ef7ea', '#9bffd9']

interface DrawOptions {
  agents: Agent[]
  tick: number
  selectedAgentId: string | null
  width: number
  height: number
  cellSize: number
}

/** 世界地图绘制逻辑(由原 index.html drawWorld 迁移,改为显式传参的纯函数) */
function drawWorld(ctx: CanvasRenderingContext2D, { agents, tick, selectedAgentId, width, height, cellSize }: DrawOptions) {
  ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)

  const gridColor = 'rgba(124, 140, 255, 0.12)'
  const majorGridColor = 'rgba(124, 140, 255, 0.22)'

  for (let x = 0; x <= width; x++) {
    ctx.beginPath()
    ctx.moveTo(x * cellSize + 0.5, 0)
    ctx.lineTo(x * cellSize + 0.5, ctx.canvas.height)
    ctx.strokeStyle = x % 5 === 0 ? majorGridColor : gridColor
    ctx.lineWidth = x % 5 === 0 ? 1.2 : 0.7
    ctx.stroke()
  }

  for (let y = 0; y <= height; y++) {
    ctx.beginPath()
    ctx.moveTo(0, y * cellSize + 0.5)
    ctx.lineTo(ctx.canvas.width, y * cellSize + 0.5)
    ctx.strokeStyle = y % 5 === 0 ? majorGridColor : gridColor
    ctx.lineWidth = y % 5 === 0 ? 1.2 : 0.7
    ctx.stroke()
  }

  const occupied = new Map<string, number>()
  agents.forEach((agent) => {
    const key = `${agent.x},${agent.y}`
    occupied.set(key, (occupied.get(key) ?? 0) + 1)
  })

  agents.forEach((agent, index) => {
    const color = AGENT_COLORS[index % AGENT_COLORS.length]
    const x = agent.x * cellSize + cellSize / 2
    const y = agent.y * cellSize + cellSize / 2
    const stack = occupied.get(`${agent.x},${agent.y}`) ?? 1
    const radius = stack > 1 ? 8 : 7

    // 光晕
    ctx.beginPath()
    ctx.arc(x, y, radius * 2.1, 0, Math.PI * 2)
    ctx.fillStyle = `${color}22`
    ctx.fill()

    // 主体
    ctx.beginPath()
    ctx.arc(x, y, radius, 0, Math.PI * 2)
    ctx.fillStyle = color
    ctx.fill()

    ctx.strokeStyle = 'rgba(255,255,255,0.7)'
    ctx.lineWidth = 1.2
    ctx.stroke()

    // 选中高亮圈
    if (agent.id === selectedAgentId) {
      ctx.beginPath()
      ctx.arc(x, y, radius + 4, 0, Math.PI * 2)
      ctx.strokeStyle = '#ffffff'
      ctx.lineWidth = 1.5
      ctx.stroke()
    }

    // 名字标签
    ctx.fillStyle = '#f7fbff'
    ctx.font = '11px Inter, sans-serif'
    ctx.textAlign = 'center'
    ctx.fillText(agent.name, x, y - 13)

    // 堆叠计数角标
    if (stack > 1) {
      ctx.fillStyle = '#ffffff'
      ctx.beginPath()
      ctx.arc(x + 14, y - 14, 9, 0, Math.PI * 2)
      ctx.fill()
      ctx.fillStyle = '#07111f'
      ctx.font = '10px Inter, sans-serif'
      ctx.fillText(String(stack), x + 14, y - 11)
    }
  })

  // tick 水印
  ctx.fillStyle = 'rgba(255,255,255,0.55)'
  ctx.font = '12px Inter, sans-serif'
  ctx.textAlign = 'left'
  ctx.fillText(`tick ${tick}`, 14, 20)
}

/** World Map 面板:Canvas 世界地图 + overlay 标签 + 渲染状态 */
export function WorldMap({ agents, tick, world, selectedAgentId, status }: WorldMapProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    const width = world?.width ?? 37
    const height = world?.height ?? 37
    const cellSize = Math.floor(Math.min(CANVAS_BASE / width, CANVAS_BASE / height))
    canvas.width = width * cellSize
    canvas.height = height * cellSize

    drawWorld(ctx, { agents, tick, selectedAgentId, width, height, cellSize })
  }, [agents, tick, world, selectedAgentId])

  return (
    <div className="panel">
      <div className="panel-header">
        <div>
          <div className="panel-title">World Map</div>
          <div className="panel-subtitle">37x37 网格世界的可视化运行态</div>
        </div>
        <div className="badge">{status}</div>
      </div>
      <div className="canvas-shell">
        <div className="canvas-wrap">
          <div className="canvas-overlay">
            <span className="overlay-tag">Tick {tick}</span>
            <span className="overlay-tag">World {world?.width ?? 37}×{world?.height ?? 37}</span>
            <span className="overlay-tag">Agents {agents.length}</span>
          </div>
          <canvas ref={canvasRef} width={CANVAS_BASE} height={CANVAS_BASE} />
        </div>
        <div className="footer-line">
          <span>网格颜色、Agent 光晕和轨迹都用于强调可读性,后续可切换到资源地图、地形图或视野图层。</span>
        </div>
      </div>
    </div>
  )
}
