/** 轻量 fetch 封装:非 2xx 统一抛错 */

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, init)
  if (!response.ok) {
    throw new Error(`请求失败: ${path} (${response.status})`)
  }
  return response.json() as Promise<T>
}
