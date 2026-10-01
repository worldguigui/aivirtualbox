import { useEffect, useState, type FormEvent } from 'react'
import type { Agent, AgentCreateRequest, PersonalityProfile } from '../types'
import { BilingualText } from './i18n'

interface AgentEditorModalProps {
  agent: Agent | null
  onClose: () => void
  onSave: (agentId: string, input: AgentCreateRequest) => Promise<void>
}

const createBlankProfile = (): PersonalityProfile => ({
  role: '',
  temperament: '',
  speakingStyle: '',
  motivations: '',
  values: '',
  knowledgeBoundary: '',
})

function buildDefaultProfile(name: string): PersonalityProfile {
  const trimmed = name.trim() || 'Resident'
  const lower = trimmed.toLowerCase()

  if (lower === 'alice') {
    return {
      role: 'Explorer',
      temperament: 'Curious and cautious',
      speakingStyle: 'Gentle, observant, and thoughtful',
      motivations: 'Learn the town and find new places to investigate',
      values: 'Facts, safety, and helping others',
      knowledgeBoundary: 'Only knows what has been sensed, remembered, or verified',
    }
  }

  if (lower === 'bob') {
    return {
      role: 'Community resident',
      temperament: 'Warm and proactive',
      speakingStyle: 'Direct, friendly, and open to conversation',
      motivations: 'Meet people and build social connections',
      values: 'Trust, promises, and community care',
      knowledgeBoundary: 'Only knows what has been perceived or remembered',
    }
  }

  return {
    role: 'Resident',
    temperament: 'Calm and steady',
    speakingStyle: 'Clear, polite, and concise',
    motivations: 'Live in the town and understand the surrounding world',
    values: 'Safety, honesty, and real experience',
    knowledgeBoundary: 'Only knows what has been sensed, remembered, or directly verified',
  }
}

function normalizeProfile(name: string, profile: PersonalityProfile): PersonalityProfile {
  const fallback = buildDefaultProfile(name)
  return {
    role: profile.role?.trim() || fallback.role,
    temperament: profile.temperament?.trim() || fallback.temperament,
    speakingStyle: profile.speakingStyle?.trim() || fallback.speakingStyle,
    motivations: profile.motivations?.trim() || fallback.motivations,
    values: profile.values?.trim() || fallback.values,
    knowledgeBoundary: profile.knowledgeBoundary?.trim() || fallback.knowledgeBoundary,
  }
}

export function AgentEditorModal({ agent, onClose, onSave }: AgentEditorModalProps) {
  const [name, setName] = useState('')
  const [x, setX] = useState('')
  const [y, setY] = useState('')
  const [profile, setProfile] = useState<PersonalityProfile>(createBlankProfile())
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!agent) return
    setName(agent.name ?? '')
    setX(String(agent.x ?? ''))
    setY(String(agent.y ?? ''))
    setProfile(agent.personality ?? createBlankProfile())
  }, [agent])

  if (!agent) return null

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitting(true)
    setError('')

    try {
      await onSave(agent.id, {
        name: name.trim() || undefined,
        x: x === '' ? undefined : Number(x),
        y: y === '' ? undefined : Number(y),
        personality: Object.values(profile).some((value) => value && value.trim())
          ? normalizeProfile(name, profile)
          : undefined,
      })
      onClose()
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : '更新居民失败')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="resident-modal-overlay" onClick={onClose}>
      <div className="resident-modal" onClick={(event) => event.stopPropagation()}>
        <div className="panel-header">
          <BilingualText primary="Edit Resident" secondary="修改居民" className="panel-title" />
          <button className="ghost" type="button" onClick={onClose}>关闭</button>
        </div>

        <form className="resident-form" onSubmit={submit}>
          <label className="full-width">
            <BilingualText primary="Name" secondary="姓名" />
            <input value={name} onChange={(event) => setName(event.target.value)} />
          </label>

          <label>
            <BilingualText primary="X" secondary="横坐标" />
            <input type="number" min="0" max="36" value={x} onChange={(event) => setX(event.target.value)} />
          </label>

          <label>
            <BilingualText primary="Y" secondary="纵坐标" />
            <input type="number" min="0" max="36" value={y} onChange={(event) => setY(event.target.value)} />
          </label>

          <label className="full-width">
            <BilingualText primary="Role" secondary="角色" />
            <input value={profile.role ?? ''} onChange={(event) => setProfile((current) => ({ ...current, role: event.target.value }))} />
          </label>

          <label className="full-width">
            <BilingualText primary="Temperament" secondary="性情" />
            <input value={profile.temperament ?? ''} onChange={(event) => setProfile((current) => ({ ...current, temperament: event.target.value }))} />
          </label>

          <label className="full-width">
            <BilingualText primary="Speaking Style" secondary="表达方式" />
            <textarea value={profile.speakingStyle ?? ''} onChange={(event) => setProfile((current) => ({ ...current, speakingStyle: event.target.value }))} />
          </label>

          <label className="full-width">
            <BilingualText primary="Motivations" secondary="动机" />
            <textarea value={profile.motivations ?? ''} onChange={(event) => setProfile((current) => ({ ...current, motivations: event.target.value }))} />
          </label>

          <label className="full-width">
            <BilingualText primary="Values" secondary="价值" />
            <textarea value={profile.values ?? ''} onChange={(event) => setProfile((current) => ({ ...current, values: event.target.value }))} />
          </label>

          <label className="full-width">
            <BilingualText primary="Knowledge Boundary" secondary="知识边界" />
            <textarea value={profile.knowledgeBoundary ?? ''} onChange={(event) => setProfile((current) => ({ ...current, knowledgeBoundary: event.target.value }))} />
          </label>

          <div className="modal-actions full-width">
            <button className="ghost" type="button" onClick={onClose}>
              <BilingualText primary="Cancel" secondary="取消" />
            </button>
            <button className="primary" type="submit" disabled={submitting}>
              <BilingualText primary={submitting ? 'Saving' : 'Save'} secondary={submitting ? '保存中' : '保存修改'} />
            </button>
          </div>
        </form>

        {error ? <div className="form-error">{error}</div> : null}
      </div>
    </div>
  )
}
