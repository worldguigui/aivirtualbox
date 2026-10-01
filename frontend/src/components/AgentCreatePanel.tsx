import { type FormEvent, useState } from 'react'
import type { AgentCreateRequest, PersonalityProfile } from '../types'
import { BilingualText } from './i18n'

interface AgentCreatePanelProps {
  onCreate: (input: AgentCreateRequest) => Promise<void>
}

const createBlankProfile = (): PersonalityProfile => ({
  role: '',
  temperament: '',
  speakingStyle: '',
  motivations: '',
  values: '',
  knowledgeBoundary: '',
})

function normalizeProfile(name: string, profile: PersonalityProfile) {
  const fallback = buildDefaultProfile(name)
  const next = {
    role: profile.role?.trim() || fallback.role,
    temperament: profile.temperament?.trim() || fallback.temperament,
    speakingStyle: profile.speakingStyle?.trim() || fallback.speakingStyle,
    motivations: profile.motivations?.trim() || fallback.motivations,
    values: profile.values?.trim() || fallback.values,
    knowledgeBoundary: profile.knowledgeBoundary?.trim() || fallback.knowledgeBoundary,
  }
  return next
}

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

/** 提供居民创建入口，并把成功后的世界刷新交给父级查询层。 */
export function AgentCreatePanel({ onCreate }: AgentCreatePanelProps) {
  const [open, setOpen] = useState(false)
  const [name, setName] = useState('')
  const [x, setX] = useState('')
  const [y, setY] = useState('')
  const [profile, setProfile] = useState<PersonalityProfile>(createBlankProfile())
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const resetForm = () => {
    setName('')
    setX('')
    setY('')
    setProfile(createBlankProfile())
    setError('')
  }

  const openDialog = () => {
    resetForm()
    setOpen(true)
  }

  const closeDialog = () => {
    setOpen(false)
    setSubmitting(false)
    setError('')
  }

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      const personalityValues = Object.values(profile).some((value) => value.trim())
        ? normalizeProfile(name, profile)
        : undefined

      await onCreate({
        name: name.trim() || undefined,
        x: x === '' ? undefined : Number(x),
        y: y === '' ? undefined : Number(y),
        ...(personalityValues ? { personality: personalityValues } : {}),
      })
      closeDialog()
      resetForm()
    } catch (createError) {
      setError(createError instanceof Error ? createError.message : '创建居民失败')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="panel create-resident-panel">
      <div className="panel-header">
        <BilingualText primary="Residents" secondary="居民" className="panel-title" />
        <button className="primary" type="button" onClick={openDialog}>
          <BilingualText primary="Add Resident" secondary="新增居民" />
        </button>
      </div>

      {open ? (
        <div className="resident-modal-overlay" onClick={closeDialog}>
          <div className="resident-modal" onClick={(event) => event.stopPropagation()}>
            <div className="panel-header">
              <BilingualText primary="Create Resident" secondary="创建居民" className="panel-title" />
              <button className="ghost" type="button" onClick={closeDialog}>关闭</button>
            </div>

            <form className="resident-form" onSubmit={submit}>
              <label className="full-width">
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

              <label className="full-width">
                <BilingualText primary="Role" secondary="角色" />
                <input value={profile.role} onChange={(event) => setProfile((current) => ({ ...current, role: event.target.value }))} placeholder="例如：Explorer / 管理者" />
              </label>

              <label className="full-width">
                <BilingualText primary="Temperament" secondary="性情" />
                <input value={profile.temperament} onChange={(event) => setProfile((current) => ({ ...current, temperament: event.target.value }))} placeholder="例如：Curious and cautious / 好奇且谨慎" />
              </label>

              <label className="full-width">
                <BilingualText primary="Speaking Style" secondary="表达方式" />
                <textarea value={profile.speakingStyle} onChange={(event) => setProfile((current) => ({ ...current, speakingStyle: event.target.value }))} placeholder="例如：Gentle and observant / 温和且观察入微" />
              </label>

              <label className="full-width">
                <BilingualText primary="Motivations" secondary="动机" />
                <textarea value={profile.motivations} onChange={(event) => setProfile((current) => ({ ...current, motivations: event.target.value }))} placeholder="居民在小镇中最关心的目标" />
              </label>

              <label className="full-width">
                <BilingualText primary="Values" secondary="价值" />
                <textarea value={profile.values} onChange={(event) => setProfile((current) => ({ ...current, values: event.target.value }))} placeholder="例如：Truth, safety, and helping others / 事实、安全与互助" />
              </label>

              <label className="full-width">
                <BilingualText primary="Knowledge Boundary" secondary="知识边界" />
                <textarea value={profile.knowledgeBoundary} onChange={(event) => setProfile((current) => ({ ...current, knowledgeBoundary: event.target.value }))} placeholder="这位居民能知道哪些信息，哪些是不知道的" />
              </label>

              <div className="modal-actions full-width">
                <button className="ghost" type="button" onClick={closeDialog}>
                  <BilingualText primary="Cancel" secondary="取消" />
                </button>
                <button className="primary" type="submit" disabled={submitting}>
                  <BilingualText primary={submitting ? 'Creating' : 'Create'} secondary={submitting ? '创建中' : '创建居民'} />
                </button>
              </div>
            </form>

            {error ? <div className="form-error">{error}</div> : null}
          </div>
        </div>
      ) : null}
    </section>
  )
}
