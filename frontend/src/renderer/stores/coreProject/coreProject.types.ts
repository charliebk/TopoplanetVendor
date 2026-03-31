export interface CoreProjectPayload {
  code: string
  name: string
  description?: string | null
}

export interface CoreProjectResponse {
  id: number
  code: string
  name: string
  description: string | null
  deleted: boolean
  createdAt: string
  updatedAt: string
}
