import type { ReactNode } from 'react'
import {
  CalendarOutlined,
  CodeOutlined,
  DashboardOutlined,
  GlobalOutlined,
  RadarChartOutlined,
  TeamOutlined,
} from '@ant-design/icons'

export interface NavigationItem {
  key: string
  label: string
  description: string
  path: string
  icon: ReactNode
}

/** 应用主导航配置，新增页面时在此注册导航入口。 */
export const navigationItems: NavigationItem[] = [
  { key: 'overview', label: 'Overview', description: '小镇运行概况', path: '/', icon: <DashboardOutlined /> },
  { key: 'world', label: 'World Map', description: '观察空间与移动', path: '/world', icon: <GlobalOutlined /> },
  { key: 'agents', label: 'Residents', description: '查看居民与记忆', path: '/agents', icon: <TeamOutlined /> },
  { key: 'events', label: 'Events', description: '追踪系统发生了什么', path: '/events', icon: <CalendarOutlined /> },
  { key: 'runtime', label: 'Runtime', description: '检查 SECD 与收敛状态', path: '/runtime', icon: <CodeOutlined /> },
]

export const navigationIcon = <RadarChartOutlined />
