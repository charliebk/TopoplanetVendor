export interface CategoryListFilters {
  coreProjectId?: number
}

export interface CategoryPayload {
  coreProjectId: number
  code: string
  name: string
  description?: string | null
  factor: number
}

export interface CategoryResponse {
  id: number
  coreProjectId: number
  code: string
  name: string
  description: string | null
  factor: number
  createdAt: string
  updatedAt: string
}
