import {type FormEvent, useState } from 'react'
import type { AgentCreateRequest } from '../types'
import { BilingualText } from './i18n'

interface AgentCreatePanelProps {
  onCreate: (input: AgentCreateRequest) => Promise<void>
}

/** 提供居民创建入口，并把成功后的世界刷新交给父级查询层。 */
export function AgentCreatePanel({ onCreate }: AgentCreatePanelProps) {
  const [name, setName] = useState('')
  const [x, setX] = useState('')
  const [y, setY] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      await onCreate({
        name: name.trim() || undefined,
        x: x === '' ? undefined : Number(x),
        y: y === '' ? undefined : Number(y),
      })
      setName('')
      setX('')
      setY('')
    } catch (createError) {
      setError(createError instanceof Error ? createError.message : '创建居民失败')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="panel create-resident-panel">
      <div className="panel-header">
        <BilingualText primary="Add Resident" secondary="新增居民" className="panel-title" />
      </div>
      <form className="create-resident-form" onSubmit={submit}>
        <label>
          <BilingualText primary="Name" secondary="姓名" />
          <input value={name} onChange={(event) => setName(event.target.value)} placeholder="留空使用默认名称" />
        </label>
        <label>
          <BilingualText primary="X" secondary="横坐标" />
          <input type="number" min="0" max="36" value={x} onChange={(event) => setX(event.target.value)} placeholder="自动分配" />
        </label>
        <label>
          <BilingualText primary="Y" secondary="纵坐标" />
          <input type="number" min="0" max="36" value={y} onChange={(event) => setY(event.target.value)} placeholder="自动分配" />
        </label>
        <button className="primary" type="submit" disabled={submitting}>
          <BilingualText primary={submitting ? 'Creating' : 'Create'} secondary={submitting ? '创建中' : '创建居民'} />
        </button>
      </form>
      {error ? <div className="form-error">{error}</div> : null}
    </section>
  )
}
