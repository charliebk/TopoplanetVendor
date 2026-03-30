import { defineStore } from 'pinia'
import { buildApiUrl } from '@/renderer/stores/client'
import type { ExportProjectResponse } from '@/renderer/stores/exportProject/useExportProjectStore'

export interface ImportProjectRequest {
  project: ExportProjectResponse['project']
  users: ExportProjectResponse['users']
}

export interface ImportProjectResponse {
  id: number
  code: string
  name: string
  description: string | null
  deleted: boolean
  createdAt: string
  updatedAt: string
}

export const useImportProjectStore = defineStore(
  'backend-import-project',
  () => {
    const endpoints = {
      importProject: buildApiUrl('/api/project/import')
    }

    const importProject = async (
      payload: ImportProjectRequest
    ): Promise<ImportProjectResponse> => {
      const response = await fetch(endpoints.importProject, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })

      if (!response.ok) {
        throw new Error(`Backend API error: ${response.status}`)
      }

      return (await response.json()) as ImportProjectResponse
    }

    return {
      endpoints,
      importProject
    }
  }
)
