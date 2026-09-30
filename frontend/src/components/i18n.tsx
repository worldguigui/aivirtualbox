import type { ReactNode } from 'react'

interface BilingualTextProps {
  primary: ReactNode
  secondary: ReactNode
  className?: string
}

/** 将英文主标签和中文解释垂直排列在同一视觉位置。 */
export function BilingualText({ primary, secondary, className = '' }: BilingualTextProps) {
  return (
    <span className={`bilingual-text ${className}`.trim()}>
      <span className="bilingual-primary">{primary}</span>
      <span className="bilingual-secondary">{secondary}</span>
    </span>
  )
}
