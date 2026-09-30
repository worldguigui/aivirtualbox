import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import type { DashboardData } from '../types'
import { navigationIcon, navigationItems } from '../navigation'
import { BilingualText } from './i18n'

interface AppShellProps {
  children: ReactNode
  data?: DashboardData
  status: string
  onRefresh: () => void
}

/** 提供全局品牌、主导航、运行状态和页面内容插槽。 */
export function AppShell({ children, data, status, onRefresh }: AppShellProps) {
  return (
    <div className="app-frame">
      <aside className="app-sidebar">
        <div className="sidebar-brand">
          <div className="brand-mark">{navigationIcon}</div>
          <div>
            <BilingualText primary="Virtual AI Town" secondary="智能世界观察台" />
          </div>
        </div>

        <nav className="main-nav" aria-label="主导航">
          <div className="nav-caption">小镇控制台</div>
          {navigationItems.map((item) => (
            <NavLink
              key={item.key}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
            >
              <span className="nav-icon">{item.icon}</span>
              <span className="nav-copy">
                <BilingualText primary={item.label} secondary={item.description} />
              </span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <span className={`status-dot ${status === '离线' ? 'offline' : 'live'}`} />
          <div>
            <strong>{status}</strong>
            <small>第 {data?.tick ?? 0} tick · {data?.agents.length ?? 0} 位居民</small>
          </div>
        </div>
      </aside>

      <main className="app-main">
        <header className="topbar">
          <div>
            <span className="topbar-kicker">小镇运行控制台</span>
            <span className="topbar-title">Virtual AI Town</span>
          </div>
          <div className="topbar-actions">
            <span className="connection-status"><span className="status-dot live" /> {status}</span>
            <button className="topbar-refresh" type="button" onClick={onRefresh} title="刷新当前数据">
              刷新数据
            </button>
          </div>
        </header>
        <div className="page-content">{children}</div>
      </main>
    </div>
  )
}
