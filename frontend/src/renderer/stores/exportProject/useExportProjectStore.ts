import { defineStore } from 'pinia'
import { buildApiUrl } from '@/renderer/stores/client'

export interface ExportProjectRequest {
  projectCode: string
}

export interface ExportProjectResponse {
  project: {
    id: number
    code: string
    name: string
    description: string | null
    deleted: boolean
    createdAt: string
    updatedAt: string
  }
  users: Array<{
    email: string
    passwordHash: string
    active: boolean
  }>
}

export const useExportProjectStore = defineStore(
  'backend-export-project',
  () => {
    const endpoints = {
      exportProject: buildApiUrl('/api/project/export')
    }

    const exportProject = async (
      payload: ExportProjectRequest
    ): Promise<ExportProjectResponse> => {
      const response = await fetch(endpoints.exportProject, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })

      if (!response.ok) {
        throw new Error(`Backend API error: ${response.status}`)
      }

      return (await response.json()) as ExportProjectResponse
    }

    return {
      endpoints,
      exportProject
    }
  }
)
