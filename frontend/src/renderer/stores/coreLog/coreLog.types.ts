export interface CoreLogListFilters {
  coreProjectId?: number
}

export interface CoreLogPayload {
  coreProjectId: number
  coreTypeLogId: number
  title: string
  message: string
  comment?: string | null
  happenedAtUtc?: string | null
}

export interface CoreLogResponse {
  id: number
  coreProjectId: number
  coreTypeLogId: number
  coreTypeLogCode: string
  coreTypeLogName: string
  title: string
  message: string
  comment: string | null
  happenedAtUtc: string
  createdAt: string
}
