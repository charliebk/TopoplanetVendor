export interface RequirementLevelListFilters {
  coreProjectId?: number
}

export interface RequirementLevelPayload {
  coreProjectId: number
  code: string
  name: string
  description?: string | null
  factor: number
}

export interface RequirementLevelResponse {
  id: number
  coreProjectId: number
  code: string
  name: string
  description: string | null
  factor: number
  createdAt: string
  updatedAt: string
}
