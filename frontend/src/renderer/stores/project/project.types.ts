export interface ProjectPayload {
  code: string
  name: string
  description?: string | null
}

export interface ProjectResponse {
  id: number
  code: string
  name: string
  description: string | null
  deleted: boolean
  createdAt: string
  updatedAt: string
}
